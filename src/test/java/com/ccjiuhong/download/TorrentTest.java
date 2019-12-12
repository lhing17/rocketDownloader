package com.ccjiuhong.download;

import bt.metainfo.IMetadataService;
import bt.metainfo.MetadataService;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author G. Seinfeld
 * @since 2019/12/11
 */
public class TorrentTest {
    @Test
    public void torrentMetaData() throws IOException {
        String dotTorrentFilePath = "/home/lhing17/a.torrent";
        IMetadataService service = new MetadataService();
        Torrent torrent = service.fromInputStream(new FileInputStream(dotTorrentFilePath));
        List<TorrentFile> files = torrent.getFiles();
        for (TorrentFile file : files) {
            System.out.println(file.getPathElements());
        }
        System.out.println(torrent.getName());
        System.out.println(torrent.getCreationDate());
        System.out.println(torrent.getCreatedBy());
        System.out.println(torrent.getTorrentId());
        System.out.println(torrent.getChunkSize());
        System.out.println(torrent.getSize());
    }
}
