package com.ccjiuhong.util;

import bt.metainfo.TorrentId;
import lombok.Data;

import java.util.BitSet;
import java.util.List;

/**
 * 用于持久化存储BT下载状态
 *
 * @author G. Seinfeld
 * @since 2020/01/09
 */
@Data
public class BtInfo {
    /**
     * BT下载任务ID
     */
    private TorrentId torrentId;

    /**
     * 种子文件位置
     */
    private String dotTorrentFilePath;

    /**
     * 总piece数
     */
    private int piecesTotal;

    /**
     * 当前有哪些piece下载完成
     */
    private BitSet bitMask;

    /**
     * 每个piece中block的数量
     */
    private List<Integer> blockCounts;

    /**
     * 每个piece中block的状态
     */
    private List<BitSet> blockBitmasks;
}
