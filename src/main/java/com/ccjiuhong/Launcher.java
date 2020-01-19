package com.ccjiuhong;

import com.ccjiuhong.web.AppServlet;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * 前端UI访问后台逻辑的入口
 *
 * @author G. Seinfeld
 * @since 2020/01/17
 */
@Slf4j
public final class Launcher {

    public static void main(String[] args) {

        // 内置服务器，用于前后端交互 TODO 改为查询可用端口
        Server server = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler(null, "/", true, false);
        server.setHandler(contextHandler);

        SessionHandler sessionHandler = new SessionHandler();
        contextHandler.setSessionHandler(sessionHandler);

        ServletHolder servletHolder = new ServletHolder(AppServlet.class);
        contextHandler.addServlet(servletHolder, "/*");

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("服务器错误", e);
        }

        log.info("服务器已经停止");
    }
}
