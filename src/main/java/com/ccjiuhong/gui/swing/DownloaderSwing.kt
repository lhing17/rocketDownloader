package com.ccjiuhong.gui.swing


import com.ccjiuhong.download.DownloadManager
import com.ccjiuhong.gui.common.Configuration.*
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.HeadlessException
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.*

/**
 * Swing版本的下载器GUI
 *
 * @author G.Seinfeld
 * @date 2019/06/30
 */
class DownloaderSwing @Throws(HeadlessException::class)
constructor() : JFrame() {
    init {
        // 设置标题
        title = TITLE

        val downloadManager = DownloadManager.getInstance()

        // 设置尺寸
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT)

        //在屏幕中央显示
        setLocationRelativeTo(null)

        //窗口关闭后自动停止程序
        defaultCloseOperation = EXIT_ON_CLOSE

        val jPanel = JPanel()
        add(jPanel)


        val jButton = JButton()
        jButton.text = "新建下载"
        jButton.addActionListener {
            val fileUrl = JOptionPane.showInputDialog("请输入下载地址")
            if (fileUrl != null) startMissionForUrl(fileUrl, downloadManager)
        }
        jPanel.add(jButton)

        val configCenter = JButton()
        configCenter.text = "配置中心"
        configCenter.addActionListener {
            println("你点击了配置中心，即将开始配置")
        }
        jPanel.add(configCenter)

        isVisible = true
    }

    /**
     * 开始任务
     * @param fileUrl 文件下载地址
     */
    private fun startMissionForUrl(fileUrl: String, downloadManager: DownloadManager) {
        val targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
        val missionId = downloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName)
        downloadManager.startMission(missionId)

        val executorService = ScheduledThreadPoolExecutor(1,
                BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build())
        executorService.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                logger.info("当前下载百分比为：" + downloadManager.getReadableDownloadedPercent(missionId))
            }
        }, 0, 1, TimeUnit.SECONDS)
    }
}

var logger: Logger = LoggerFactory.getLogger(DownloaderSwing::class.java)
fun main() {

    try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
    } catch (e: ClassNotFoundException) {
        logger.error(e.message, e)
    } catch (e: InstantiationException) {
        logger.error(e.message, e)
    } catch (e: IllegalAccessException) {
        logger.error(e.message, e)
    } catch (e: UnsupportedLookAndFeelException) {
        logger.error(e.message, e)
    }

    DownloaderSwing()
}

