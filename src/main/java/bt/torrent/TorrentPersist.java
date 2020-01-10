package bt.torrent;

import bt.metainfo.TorrentId;

/**
 * @author G. Seinfeld
 * @since 2019/12/25
 */
public interface TorrentPersist {

    void serializeDescriptors(TorrentId torrentId, String dotTorrentFilePath);
}
