package com.ccjiuhong.web;

import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 全局Servlet
 *
 * @author G. Seinfeld
 * @since 2020/01/17
 */
@Slf4j
public class AppServlet extends HttpServlet {

    private DownloadManager manager = DefaultDownloadManager.getInstance();

    /**
     * 将ajax结果写入响应
     * @param res http响应
     * @param result ajax返回结果
     */
    private void writeResponse(HttpServletResponse res, AjaxResult result) {
        JSONObject responseJSONObject = (JSONObject) JSONObject.toJSON(result);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = res.getWriter()) {
            out.append(responseJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDownloading(HttpServletRequest req, HttpServletResponse res) {
        String links = req.getParameter("links");
        String savePath = req.getParameter("savePath");
        int missionId = manager.addMission(links, savePath, "a");
        try {
            boolean started = manager.startOrResumeMission(missionId);
            if (started) {
                writeResponse(res, AjaxResult.success());
            } else {
                writeResponse(res, AjaxResult.error("开启下载失败"));
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            writeResponse(res, AjaxResult.error(e.getMessage()));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/startDownloading".equals(req.getRequestURI())) {
            startDownloading(req, resp);
        } else {
            super.service(req, resp);
        }
    }
}
