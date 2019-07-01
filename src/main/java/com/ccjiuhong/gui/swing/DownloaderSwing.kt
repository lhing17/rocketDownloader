package com.ccjiuhong.gui.swing


import com.alibaba.fastjson.JSONObject
import com.ccjiuhong.download.DownloadManager
import com.ccjiuhong.gui.common.Configuration.*
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Font
import java.awt.HeadlessException
import java.io.File
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

        // 读取配置文件
        val jsonConfig = JSONObject.parseObject(File("config/config.json").readText())

        // 读取语言包配置
        val lang = jsonConfig["language"]
        val languageConfig = JSONObject.parseObject(File("config/i18n/$lang.json").readText())

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

        // 新建下载的按钮，点击按钮，在弹出框输入URL后点击确定即可开启下载
        val jButton = JButton()
        jButton.text = languageConfig["newTask"] as String?
        jButton.addActionListener {
            val fileUrl = JOptionPane.showInputDialog(languageConfig["inputAddress"] as String?)
            if (fileUrl != null) startMissionForUrl(fileUrl, downloadManager)
        }
        jPanel.add(jButton)

        val configCenter = JButton()
        configCenter.text = languageConfig["configCenter"] as String?
        configCenter.addActionListener {
            val file = File("config/config.json")
            val s = file.readText()
            val frame = JDialog(this, languageConfig["configCenter"] as String?, true)
            frame.setSize(600, 480)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val configText = JTextArea(s)
            configText.font = Font("Microsoft YaHei", Font.PLAIN, 16)
            frame.contentPane.add(configText)

//            frame.pack()
            frame.isVisible = true


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

