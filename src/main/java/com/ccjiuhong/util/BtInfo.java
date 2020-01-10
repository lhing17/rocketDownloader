package com.ccjiuhong.util;

import bt.metainfo.TorrentId;
import bt.protocol.Protocols;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<BitSet> blockBitMasks;

    /**
     * 从JSON Object中解析出BtInfo
     * @param jsonObject 存储到进度文件中的jsonObject
     * @return 解析出的btInfo
     */
    public static BtInfo fromJSONObject(JSONObject jsonObject) {
        BtInfo btInfo = new BtInfo();
        btInfo.setTorrentId(TorrentId.fromBytes(Protocols.fromHex(jsonObject.getString("torrentId"))));
        btInfo.setDotTorrentFilePath(jsonObject.getString("dotTorrentFilePath"));

        int piecesTotal = jsonObject.getInteger("piecesTotal");
        btInfo.setPiecesTotal(piecesTotal);
        btInfo.setBitMask(getBitMaskFromString(jsonObject.getString("bitMask"), piecesTotal));

        List<Integer> blockCounts = new ArrayList<>();
        List<BitSet> blockBitMasks = new ArrayList<>();
        JSONArray blockBitMaskArray = jsonObject.getJSONArray("blockBitMasks");
        JSONArray blockCountArray = jsonObject.getJSONArray("blockCounts");
        for (int i = 0; i < blockCountArray.size(); i++) {
            int blockCount = (int) blockCountArray.get(i);
            blockCounts.add(blockCount);
            blockBitMasks.add(getBitMaskFromString(blockBitMaskArray.getString(i), blockCount));
        }
        btInfo.setBlockCounts(blockCounts);
        btInfo.setBlockBitMasks(blockBitMasks);


        return btInfo;
    }

    private static BitSet getBitMaskFromString(String bitMaskString, int total) {
        BitSet bitSet = new BitSet(total);
        String[] arr = bitMaskString.substring(1, bitMaskString.length() - 1).split(",");
        for (String s : arr) {
            int bitIndex = Integer.parseInt(s.trim());
            bitSet.set(bitIndex);
        }
        return bitSet;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("torrentId", torrentId.toString());
        jsonObject.put("dotTorrentFilePath", dotTorrentFilePath);
        jsonObject.put("piecesTotal", piecesTotal);
        jsonObject.put("bitMask", bitMask.toString());
        jsonObject.put("blockCounts", blockCounts);
        jsonObject.put("blockBitMarks", blockBitMasks.stream().map(BitSet::toString).collect(Collectors.toList()));

        return jsonObject.toString();
    }
}
