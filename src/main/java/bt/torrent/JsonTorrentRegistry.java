package bt.torrent;

import bt.data.ChunkDescriptor;
import bt.data.DataDescriptor;
import bt.data.IDataDescriptorFactory;
import bt.data.Storage;
import bt.event.EventSink;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentId;
import bt.runtime.Config;
import bt.service.IRuntimeLifecycleBinder;
import com.alibaba.fastjson.JSON;
import com.ccjiuhong.mission.PeerToPeerMission;
import com.ccjiuhong.util.BtInfo;
import com.ccjiuhong.util.FileUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author G. Seinfeld
 * @since 2019/12/24
 */
@Slf4j
public class JsonTorrentRegistry implements TorrentRegistry, TorrentPersist {

    private IDataDescriptorFactory dataDescriptorFactory;
    private IRuntimeLifecycleBinder lifecycleBinder;
    private EventSink eventSink;

    private Set<TorrentId> torrentIds;
    private ConcurrentHashMap<TorrentId, Torrent> torrents;
    private ConcurrentHashMap<TorrentId, DefaultTorrentDescriptor> descriptors;
    private File descriptorsJson;

    @Inject
    public JsonTorrentRegistry(IDataDescriptorFactory dataDescriptorFactory,
                               IRuntimeLifecycleBinder lifecycleBinder,
                               EventSink eventSink,
                               Config config) {
        File workingDirectory = new File(config.getWorkDirectory());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }
        this.descriptorsJson = new File(workingDirectory, "descriptors.rd");
        this.dataDescriptorFactory = dataDescriptorFactory;
        this.lifecycleBinder = lifecycleBinder;
        this.eventSink = eventSink;

        this.torrentIds = ConcurrentHashMap.newKeySet();
        this.torrents = new ConcurrentHashMap<>();
        this.descriptors = new ConcurrentHashMap<>();
    }


    public void serializeDescriptors(TorrentId torrentId, String dotTorrentFilePath) {
        Map<String, String> map = PeerToPeerMission.btInfoMap.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().toString(), e -> e.getValue().toString()
        ));


        for (Map.Entry<TorrentId, DefaultTorrentDescriptor> entry : descriptors.entrySet()) {
            DataDescriptor dataDescriptor = entry.getValue().getDataDescriptor();
            if (dataDescriptor != null) {
                BtInfo btInfo = new BtInfo();
                btInfo.setTorrentId(entry.getKey());
                btInfo.setBitMask(dataDescriptor.getBitfield().getBitmask());
                btInfo.setPiecesTotal(dataDescriptor.getBitfield().getPiecesTotal());

                List<ChunkDescriptor> chunkDescriptors = dataDescriptor.getChunkDescriptors();
                List<Integer> blockCounts = new ArrayList<>();
                List<BitSet> blockBitmasks = new ArrayList<>();
                for (ChunkDescriptor chunkDescriptor : chunkDescriptors) {
                    blockCounts.add(chunkDescriptor.blockCount());
                    blockBitmasks.add(chunkDescriptor.getBitmask());
                }
                btInfo.setBlockCounts(blockCounts);
                btInfo.setBlockBitMasks(blockBitmasks);
                if (entry.getKey().toString().equals(torrentId.toString()) && StringUtils.isNotEmpty(dotTorrentFilePath)) {
                    btInfo.setDotTorrentFilePath(dotTorrentFilePath);
                }
                map.put(entry.getKey().toString(), btInfo.toString());
            }
        }
        if (!map.isEmpty())
            FileUtil.writeText(descriptorsJson, JSON.toJSONString(map));
    }


    @Override
    public Collection<Torrent> getTorrents() {
        return Collections.unmodifiableCollection(torrents.values());
    }

    @Override
    public Collection<TorrentId> getTorrentIds() {
        return Collections.unmodifiableCollection(torrentIds);
    }

    @Override
    public Optional<Torrent> getTorrent(TorrentId torrentId) {
        Objects.requireNonNull(torrentId, "Missing torrent ID");
        return Optional.ofNullable(torrents.get(torrentId));
    }

    @Override
    public Optional<TorrentDescriptor> getDescriptor(Torrent torrent) {
        return Optional.ofNullable(descriptors.get(torrent.getTorrentId()));
    }


    @Override
    public Optional<TorrentDescriptor> getDescriptor(TorrentId torrentId) {
        Objects.requireNonNull(torrentId, "Missing torrent ID");
        return Optional.ofNullable(descriptors.get(torrentId));
    }

    @Override
    public TorrentDescriptor getOrCreateDescriptor(Torrent torrent, Storage storage) {
        return register(torrent, storage);
    }

    @Override
    public TorrentDescriptor register(Torrent torrent, Storage storage) {
        TorrentId torrentId = torrent.getTorrentId();

        DefaultTorrentDescriptor descriptor = descriptors.get(torrentId);
        if (descriptor != null) {
            if (descriptor.getDataDescriptor() == null) {
                descriptor.setDataDescriptor(dataDescriptorFactory.createDescriptor(torrent, storage));
            }
        } else {
            descriptor = getDefaultTorrentDescriptor(torrentId);
        }

        torrents.putIfAbsent(torrentId, torrent);
        return descriptor;
    }

    @Override
    public TorrentDescriptor register(TorrentId torrentId) {
        return getDescriptor(torrentId).orElseGet(() -> getDefaultTorrentDescriptor(torrentId));
    }

    @NotNull
    private DefaultTorrentDescriptor getDefaultTorrentDescriptor(TorrentId torrentId) {
        DefaultTorrentDescriptor descriptor = new DefaultTorrentDescriptor(torrentId, eventSink);

        DefaultTorrentDescriptor existing = descriptors.putIfAbsent(torrentId, descriptor);
        if (existing != null) {
            descriptor = existing;
        } else {
            torrentIds.add(torrentId);
            addShutdownHook(torrentId, descriptor);
        }
        return descriptor;
    }

    @Override
    public boolean isSupportedAndActive(TorrentId torrentId) {
        Optional<TorrentDescriptor> descriptor = getDescriptor(torrentId);
        // it's OK if descriptor is not present -- torrent might be being fetched at the time
        return getTorrentIds().contains(torrentId)
                && (descriptor.isEmpty() || descriptor.get().isActive());
    }

    private void addShutdownHook(TorrentId torrentId, TorrentDescriptor descriptor) {
        lifecycleBinder.onShutdown("Closing data descriptor for torrent ID: " + torrentId, () -> {
            if (descriptor.getDataDescriptor() != null) {
                try {
                    descriptor.getDataDescriptor().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
