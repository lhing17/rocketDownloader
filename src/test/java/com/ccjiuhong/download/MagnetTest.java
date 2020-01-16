package com.ccjiuhong.download;

import bt.magnet.MagnetUri;
import bt.magnet.MagnetUriParser;
import bt.metainfo.IMetadataService;
import bt.metainfo.MetadataService;
import bt.runtime.Config;
import org.junit.Test;

/**
 * @author G. Seinfeld
 * @since 2019/12/13
 */
public class MagnetTest {
    @Test
    public void magnetMetadata() {
        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return 5;
            }
        };
        IMetadataService service = new MetadataService();
        String magentUrl = "magnet:?xt=urn:btih:0031573652B8B29ED0B6A636BF3987887F332989";
        MagnetUri magnetUri = MagnetUriParser.lenientParser().parse(magentUrl);
        System.out.println(magnetUri.getTorrentId());
//        MetadataConsumer metadataConsumer = new MetadataConsumer(service, magnetUri.getTorrentId(), config);
//        Torrent torrent = metadataConsumer.waitForTorrent();
//        System.out.println(torrent.getName());
//        System.out.println(torrent.getCreationDate());
//        System.out.println(torrent.getCreatedBy());
//        System.out.println(torrent.getTorrentId());
//        System.out.println(torrent.getChunkSize());
//        System.out.println(torrent.getSize());
    }
}
