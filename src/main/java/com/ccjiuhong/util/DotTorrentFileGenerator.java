package com.ccjiuhong.util;

import bt.bencoding.model.*;
import bt.metainfo.IMetadataService;
import bt.metainfo.MetadataService;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author G. Seinfeld
 * @since 2019/12/26
 */
public final class DotTorrentFileGenerator {
    private DotTorrentFileGenerator() {

    }

    private static final String ANNOUNCE_KEY = "announce";
    private static final String ANNOUNCE_LIST_KEY = "announce-list";
    private static final String INFOMAP_KEY = "info";
    private static final String TORRENT_NAME_KEY = "name";
    private static final String CHUNK_SIZE_KEY = "piece length";
    private static final String CHUNK_HASHES_KEY = "pieces";
    private static final String TORRENT_SIZE_KEY = "length";
    private static final String FILES_KEY = "files";
    private static final String FILE_SIZE_KEY = "length";
    private static final String FILE_PATH_ELEMENTS_KEY = "path";
    private static final String PRIVATE_KEY = "private";
    private static final String CREATION_DATE_KEY = "creation date";
    private static final String CREATED_BY_KEY = "created by";

    private static final int CHUNK_HASH_LENGTH = 20;

    public static void generate(Torrent torrent, OutputStream outputStream) throws IOException {
        Map<String, BEObject<?>> root = buildRootMap(torrent);
        BEMap rootBE = new BEMap(null, root);
        rootBE.writeTo(outputStream);
    }

    @NotNull
    private static Map<String, BEObject<?>> buildRootMap(Torrent torrent) {
        Map<String, BEObject<?>> root = new HashMap<>();
        putInfoMap(torrent, root);
        putCreateDateIfPresent(torrent, root);
        putCreateByIfPresent(torrent, root);
        putAnnounceListIfPresent(torrent, root);
        return root;
    }

    private static void putAnnounceListIfPresent(Torrent torrent, Map<String, BEObject<?>> root) {
        torrent.getAnnounceKey().ifPresent(
                announceKey -> {
                    if (announceKey.getTrackerUrl() != null) {
                        root.put(ANNOUNCE_KEY, new BEString(announceKey.getTrackerUrl().getBytes(StandardCharsets.UTF_8)));
                    }
                    if (announceKey.getTrackerUrls() != null && announceKey.getTrackerUrls().size() > 0) {
                        List<BEList> tierList = new ArrayList<>();
                        for (List<String> tierTrackerUrls : announceKey.getTrackerUrls()) {
                            List<BEString> trackUrlList = new ArrayList<>();
                            for (String tierTrackerUrl : tierTrackerUrls) {
                                trackUrlList.add(new BEString(tierTrackerUrl.getBytes(StandardCharsets.UTF_8)));
                            }
                            tierList.add(new BEList(null, trackUrlList));
                        }
                        root.put(ANNOUNCE_LIST_KEY, new BEList(null, tierList));
                    }
                }
        );
    }

    private static void putCreateByIfPresent(Torrent torrent, Map<String, BEObject<?>> root) {
        torrent.getCreatedBy().ifPresent(
                createBy -> root.put(CREATED_BY_KEY, new BEString(createBy.getBytes(StandardCharsets.UTF_8)))
        );
    }

    private static void putCreateDateIfPresent(Torrent torrent, Map<String, BEObject<?>> root) {
        torrent.getCreationDate().ifPresent(
                createDate -> root.put(CREATION_DATE_KEY, new BEInteger(null, BigInteger.valueOf(
                        createDate.toEpochMilli() / 1000L
                )))
        );
    }

    private static void putInfoMap(Torrent torrent, Map<String, BEObject<?>> root) {
        Map<String, BEObject<?>> infoMap = buildInfoMap(torrent);
        BEMap infoMapBE = new BEMap(null, infoMap);
        root.put(INFOMAP_KEY, infoMapBE);
    }

    @NotNull
    private static Map<String, BEObject<?>> buildInfoMap(Torrent torrent) {
        Map<String, BEObject<?>> infoMap = new HashMap<>();

        // 文件名
        if (torrent.getName() != null) {
            putName(torrent, infoMap);
        }

        BigInteger chunkSize = putChunkSize(torrent, infoMap);

        putChunkHashes(torrent, infoMap, chunkSize);

        if (torrent.getFiles() == null || torrent.getFiles().size() <= 1) {
            // 单文件
            putTorrentSize(torrent, infoMap);
        } else {
            // 多文件
            putTorrentFiles(torrent, infoMap);
        }
        putPrivateKeyIfExists(torrent, infoMap);
        return infoMap;
    }

    private static void putPrivateKeyIfExists(Torrent torrent, Map<String, BEObject<?>> infoMap) {
        if (torrent.isPrivate()) {
            infoMap.put(PRIVATE_KEY, new BEInteger(null, BigInteger.ONE));
        }
    }

    private static void putTorrentFiles(Torrent torrent, Map<String, BEObject<?>> infoMap) {
        List<BEMap> filesBE = new ArrayList<>();
        for (TorrentFile file : torrent.getFiles()) {
            Map<String, BEObject<?>> fileMap = new HashMap<>();
            // 文件大小
            fileMap.put(FILE_SIZE_KEY, new BEInteger(null, BigInteger.valueOf(file.getSize())));
            // 文件路径
            List<BEString> pathElements = file.getPathElements().stream()
                    .map(s -> new BEString(s.getBytes(StandardCharsets.UTF_8))).collect(Collectors.toList());
            BEList pathElementsBE = new BEList(null, pathElements);
            fileMap.put(FILE_PATH_ELEMENTS_KEY, pathElementsBE);
            filesBE.add(new BEMap(null, fileMap));
        }
        infoMap.put(FILES_KEY, new BEList(null, filesBE));
    }

    private static void putTorrentSize(Torrent torrent, Map<String, BEObject<?>> infoMap) {
        BigInteger torrentSize = BigInteger.valueOf(torrent.getSize());
        infoMap.put(TORRENT_SIZE_KEY, new BEInteger(null, torrentSize));
    }

    private static void putChunkHashes(Torrent torrent, Map<String, BEObject<?>> infoMap, BigInteger chunkSize) {
        Iterable<byte[]> iterableChunkHashes = torrent.getChunkHashes();
        byte[] total = new byte[CHUNK_HASH_LENGTH * chunkSize.intValueExact()];
        int length = 0;
        for (byte[] chunkHash : iterableChunkHashes) {
            int originLength = length;
            length += Math.min(chunkHash.length, CHUNK_HASH_LENGTH);
            System.arraycopy(chunkHash, 0, total, originLength, length - originLength);
        }
        byte[] realTotal = Arrays.copyOfRange(total, 0, length);
        infoMap.put(CHUNK_HASHES_KEY, new BEString(realTotal));
    }

    private static BigInteger putChunkSize(Torrent torrent, Map<String, BEObject<?>> infoMap) {
        BigInteger chunkSize = BigInteger.valueOf(torrent.getChunkSize());
        BEInteger chunkSizeBE = new BEInteger(null, chunkSize);
        infoMap.put(CHUNK_SIZE_KEY, chunkSizeBE);
        return chunkSize;
    }

    private static void putName(Torrent torrent, Map<String, BEObject<?>> infoMap) {
        byte[] bytes = torrent.getName().getBytes(StandardCharsets.UTF_8);
        BEString nameBE = new BEString(bytes);
        infoMap.put(TORRENT_NAME_KEY, nameBE);
    }

    public static void main(String[] args) throws IOException {
        String dotTorrentFilePath = "/home/lhing17/b.torrent";
        IMetadataService service = new MetadataService();
        Torrent torrent = service.fromInputStream(new FileInputStream(dotTorrentFilePath));
        generate(torrent, System.out);

    }
}
