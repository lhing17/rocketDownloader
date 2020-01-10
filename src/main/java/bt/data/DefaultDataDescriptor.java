/*
 * Copyright (c) 2016—2017 Andrei Tomashpolskiy and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bt.data;

import bt.BtException;
import bt.data.range.BlockRange;
import bt.data.range.MutableBlockSet;
import bt.data.range.Ranges;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import com.ccjiuhong.mission.PeerToPeerMission;
import com.ccjiuhong.util.BtInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p><b>Note that this class is not a part of the public API and is a subject to change.</b></p>
 */
class DefaultDataDescriptor implements DataDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataDescriptor.class);

    private Storage storage;

    private Torrent torrent;
    private List<ChunkDescriptor> chunkDescriptors;
    private Bitfield bitfield;

    private Map<Integer, List<TorrentFile>> filesForPieces;
    private Set<StorageUnit> storageUnits;
    private DataReader reader;

    private ChunkVerifier verifier;

    public DefaultDataDescriptor(Storage storage,
                                 Torrent torrent,
                                 ChunkVerifier verifier,
                                 DataReaderFactory dataReaderFactory,
                                 int transferBlockSize) {
        this.storage = storage;
        this.torrent = torrent;
        this.verifier = verifier;

        init(transferBlockSize);

        this.reader = dataReaderFactory.createReader(torrent, this);
    }

    private void init(long transferBlockSize) {
        // 从缓存中读取当前下载的持久化信息
        Optional<BtInfo> btInfo = Optional.ofNullable(PeerToPeerMission.btInfoMap.get(torrent.getTorrentId()));


        List<TorrentFile> files = torrent.getFiles();

        long totalSize = torrent.getSize();
        long chunkSize = torrent.getChunkSize();

        // 每次传输的块大小 默认为8KB
        if (transferBlockSize > chunkSize) {
            transferBlockSize = chunkSize;
        }

        // 总Piece数量
        int chunksTotal = btInfo.map(BtInfo::getPiecesTotal).orElse((int) Math.ceil((double) totalSize / chunkSize));

        // 每个Piece的文件列表
        Map<Integer, List<TorrentFile>> filesForPieces = new HashMap<>((int) (chunksTotal / 0.75d) + 1);

        // 每个Piece的Descriptor
        List<ChunkDescriptor> chunks = new ArrayList<>(chunksTotal + 1);

        // 每个Piece的签名
        Iterator<byte[]> chunkHashes = torrent.getChunkHashes().iterator();

        // 存储单元（实体文件单元）和种子中文件的映射
        Map<StorageUnit, TorrentFile> storageUnitsToFilesMap = new LinkedHashMap<>((int) (files.size() / 0.75d) + 1);
        files.forEach(f -> storageUnitsToFilesMap.put(storage.getUnit(torrent, f), f));

        // filter out empty files (and create them at once)
        List<StorageUnit> nonEmptyStorageUnits = new ArrayList<>();
        for (StorageUnit unit : storageUnitsToFilesMap.keySet()) {
            if (unit.capacity() > 0) {
                nonEmptyStorageUnits.add(unit);
            } else {
                try {
                    // TODO: think about adding some explicit "initialization/creation" method
                    unit.writeBlock(new byte[0], 0);
                } catch (Exception e) {
                    LOGGER.warn("Failed to create empty storage unit: " + unit, e);
                }
            }
        }

        if (nonEmptyStorageUnits.size() > 0) {
            long limitInLastUnit = nonEmptyStorageUnits.get(nonEmptyStorageUnits.size() - 1).capacity();
            DataRange data = new ReadWriteDataRange(nonEmptyStorageUnits, 0, limitInLastUnit);

            long off, lim;
            long remaining = totalSize;
            int[] index = {0};
            while (remaining > 0) {
                off = chunks.size() * chunkSize;
                lim = Math.min(chunkSize, remaining);

                DataRange subrange = data.getSubrange(off, lim);

                if (!chunkHashes.hasNext()) {
                    throw new BtException("Wrong number of chunk hashes in the torrent: too few");
                }

                List<TorrentFile> chunkFiles = new ArrayList<>();
                subrange.visitUnits((unit, off1, lim1) -> chunkFiles.add(storageUnitsToFilesMap.get(unit)));
                filesForPieces.put(chunks.size(), chunkFiles);

                BitSet bitSet = btInfo.map(BtInfo::getBlockBitMasks).map(bitSets -> bitSets.get(index[0])).orElse(null);
                DefaultChunkDescriptor chunkDescriptor = buildChunkDescriptor(subrange, transferBlockSize, chunkHashes.next(), bitSet);

                chunks.add(chunkDescriptor);

                remaining -= chunkSize;
                index[0]++;
            }
        }

        if (chunkHashes.hasNext()) {
            throw new BtException("Wrong number of chunk hashes in the torrent: too many");
        }

        BitSet bitMask = btInfo.map(BtInfo::getBitMask).orElse(null);
        this.bitfield = buildBitfield(chunks, bitMask);
        this.chunkDescriptors = chunks;
        this.storageUnits = storageUnitsToFilesMap.keySet();
        this.filesForPieces = filesForPieces;
    }

    private DefaultChunkDescriptor buildChunkDescriptor(DataRange data, long blockSize, byte[] checksum, BitSet bitSet) {
        BlockRange<DataRange> blockData = Ranges.blockRange(data, blockSize);
        DataRange synchronizedData = Ranges.synchronizedDataRange(blockData);
        MutableBlockSet blockSet = (MutableBlockSet) blockData.getBlockSet();
        if (bitSet != null) {
            blockSet.getBitmask().or(bitSet);
        }
        BlockSet synchronizedBlockSet = Ranges.synchronizedBlockSet(blockData.getBlockSet());

        return new DefaultChunkDescriptor(synchronizedData, synchronizedBlockSet, checksum);
    }

    private Bitfield buildBitfield(List<ChunkDescriptor> chunks, BitSet bitMask) {
        Bitfield bitfield = new Bitfield(chunks.size());
        if (bitMask != null) {
            for (int i = 0; i < chunks.size(); i++) {
                if (bitMask.get(i)) {
                    bitfield.markVerified(i);
                }
            }
        }
        verifier.verify(chunks, bitfield);
        return bitfield;
    }

    @Override
    public List<ChunkDescriptor> getChunkDescriptors() {
        return chunkDescriptors;
    }

    @Override
    public Bitfield getBitfield() {
        return bitfield;
    }

    @Override
    public List<TorrentFile> getFilesForPiece(int pieceIndex) {
        if (pieceIndex < 0 || pieceIndex >= bitfield.getPiecesTotal()) {
            throw new IllegalArgumentException("Invalid piece index: " + pieceIndex +
                    ", expected 0.." + bitfield.getPiecesTotal());
        }
        return filesForPieces.get(pieceIndex);
    }

    @Override
    public DataReader getReader() {
        return reader;
    }

    @Override
    public void close() {
        storageUnits.forEach(unit -> {
            try {
                unit.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close storage unit: " + unit);
            }
        });
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " <" + torrent.getName() + ">";
    }
}
