package com.ccjiuhong.util;

import bt.bencoding.model.*;
import bt.metainfo.IMetadataService;
import bt.metainfo.MetadataService;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import org.apache.commons.lang3.StringUtils;
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

    private static final String DEFAULT_TRACKER = "http://tracker.trackerfix.com:80/announce";

    private static final List<String> DEFAULT_TRACKERS = Arrays.asList(
            "http://tracker.trackerfix.com:80/announce",
            "udp://9.rarbg.me:2710/announce",
            "udp://9.rarbg.to:2710/announce",
            "udp://62.138.0.158:6969/announce",
            "udp://94.158.213.92:1337/announce",
            "udp://186.225.17.100:1337/announce",
            "udp://152.80.120.112:2710/announce",
            "udp://152.80.120.114:2710/announce",
            "udp://186.19.107.254:80/announce",
            "udp://209.83.20.20:6969/announce",
            "udp://6.206.27.172:6969/announce",
            "udp://177.31.241.153:80/announce",
            "udp://38.235.174.46:2710/announce",
            "udp://96.211.168.204:2710/announce",
            "udp://160.100.245.181:6969/announce",
            "http://52.68.122.172:80/announce",
            "udp://90.234.156.205:451/announce",
            "udp://185.105.151.164:6969/announce",
            "udp://52.15.40.114:80/announce",
            "http://83.209.230.66:80/announce",
            "udp://186.83.215.123:6969/announce",
            "udp://196.154.52.99:80/announce",
            "http://52.38.230.101:80/announce",
            "udp://tracker.coppersurfer.tk:6970/announce",
            "udp://tracker.opentrackr.org:1338/announce",
            "udp://tracker.internetwarriors.net:1338/announce",
            "udp://10.rarbg.to:2710/announce",
            "udp://10.rarbg.me:2710/announce",
            "udp://tracker.openbittorrent.com:81/announce",
            "udp://exodus.desync.com:6970/announce",
            "udp://tracker.tiny-vps.com:6970/announce",
            "udp://thetracker.org:81/announce",
            "udp://retracker.lanta-net.ru:2711/announce",
            "udp://bt.xxx-tracker.com:2711/announce",
            "udp://tracker.cyberia.is:6970/announce",
            "http://open.acgnxtracker.com:81/announce",
            "udp://tracker.torrent.eu.org:452/announce",
            "udp://explodie.org:6970/announce",
            "udp://ipv5.tracker.harry.lu:80/announce",
            "http://retracker.mgts.by:81/announce",
            "udp://tracker.uw1.xyz:6969/announce",
            "udp://open.stealth.si:81/announce",
            "http://t.nyaatracker.com:80/announce"
    );

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

        String[] tracker = {""};
        List<List<String>> trackers = new ArrayList<>();
        torrent.getAnnounceKey().ifPresent(
                announceKey -> {
                    if (announceKey.getTrackerUrl() != null) {
                        tracker[0] = announceKey.getTrackerUrl();
                    }
                    if (announceKey.getTrackerUrls() != null && announceKey.getTrackerUrls().size() > 0) {
                        trackers.addAll(announceKey.getTrackerUrls());

                    }
                });
        if (StringUtils.isEmpty(tracker[0])){
            tracker[0] = DEFAULT_TRACKER;
        }
        if (trackers.size() == 0) {
            trackers.add(DEFAULT_TRACKERS);
        }
        root.put(ANNOUNCE_KEY, new BEString(tracker[0].getBytes(StandardCharsets.UTF_8)));

        List<BEList> tierList = new ArrayList<>();
        for (List<String> tierTrackerUrls : trackers) {
            List<BEString> trackUrlList = new ArrayList<>();
            for (String tierTrackerUrl : tierTrackerUrls) {
                trackUrlList.add(new BEString(tierTrackerUrl.getBytes(StandardCharsets.UTF_8)));
            }
            tierList.add(new BEList(null, trackUrlList));
        }
        root.put(ANNOUNCE_LIST_KEY, new BEList(null, tierList));

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
