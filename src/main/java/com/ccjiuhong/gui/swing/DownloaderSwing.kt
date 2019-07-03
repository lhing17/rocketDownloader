package com.ccjiuhong.gui.swing


import com.alibaba.fastjson.JSONObject
import com.ccjiuhong.download.DownloadManager
import com.ccjiuhong.gui.common.Configuration.*
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.StandardChartTheme
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.Second
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYDataset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.HeadlessException
import java.io.File
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.JOptionPane.WARNING_MESSAGE
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


/**
 * Swing版本的下载器GUI
 *
 * @author G.Seinfeld
 * @date 2019/06/30
 */
// 读取配置文件
val configFile = File("config/config.json")
var jsonConfig: JSONObject = JSONObject.parseObject(configFile.readText())

// 读取语言包配置
var lang = jsonConfig["language"]
var languageConfig: JSONObject = JSONObject.parseObject(File("config/i18n/$lang.json").readText())

class DownloaderSwing @Throws(HeadlessException::class)
constructor() : JFrame() {
    init {


        // 设置标题
        title = TITLE

        // 设置尺寸
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT)

        // 在屏幕中央显示
        setLocationRelativeTo(null)

        // 窗口关闭后自动停止程序
        defaultCloseOperation = EXIT_ON_CLOSE

        // 获取下载管理器的实例
        val downloadManager = DownloadManager.getInstance()


        // 创建JPanel作为容器，所有其他组件都添加到JPanel中
        val gb = GridBagLayout()

        val jPanel = JPanel(gb)
        add(jPanel)


        val leftPanel = JPanel()
        var c = GridBagConstraints()
        c.fill = GridBagConstraints.BOTH
        c.weightx = 0.3
        c.weighty = 1.0
        gb.setConstraints(leftPanel, c)
        jPanel.add(leftPanel)


        val chart = createChart()

        val rightPanel = ChartPanel(chart)
        c = GridBagConstraints()
        c.fill = GridBagConstraints.BOTH
        c.weightx = 0.7
        c.weighty = 1.0
        gb.setConstraints(rightPanel, c)
        jPanel.add(rightPanel)


        // 菜单条
        val jMenuBar = JMenuBar()
        setJMenuBar(jMenuBar)

        val jMenu = JMenu(languageConfig["task"] as String?)
        jMenuBar.add(jMenu)

        // 添加新建下载任务的菜单项
        addNewTaskMenuItem(jMenu, downloadManager, chart)

        // 添加修改配置的菜单项
        addConfigCenterMenuItem(jMenu)

        isVisible = true
    }

    private fun addConfigCenterMenuItem(jMenu: JMenu) {
        val item1 = JMenuItem(languageConfig["configCenter"] as String?)
        jMenu.add(item1)
        item1.addActionListener {
            val s = configFile.readText()
            val frame = JDialog(this, languageConfig["configCenter"] as String?, true)
            frame.setSize(600, 480)
            frame.defaultCloseOperation = DISPOSE_ON_CLOSE

            val configText = JTextArea(s)
            configText.font = Font("Microsoft YaHei", Font.PLAIN, 16)
            configText.document.addDocumentListener(object : DocumentListener {
                override fun changedUpdate(e: DocumentEvent?) {
                    println("changed")
                }

                override fun insertUpdate(e: DocumentEvent?) {
                    println("inserted")
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    println("removed")
                }

            })

            frame.contentPane.add(configText)

            val jMenuBar = JMenuBar()
            frame.jMenuBar = jMenuBar

            val menu = JMenu("file")
            jMenuBar.add(menu)

            val saveItem = JMenuItem("save")
            menu.add(saveItem)
            saveItem.addActionListener {
                try {
                    JSONObject.parseObject(configText.text)
                    configFile.writeText(configText.text)
                    jsonConfig = JSONObject.parseObject(configFile.readText())

                    // 读取语言包配置
                    lang = jsonConfig["language"]
                    languageConfig= JSONObject.parseObject(File("config/i18n/$lang.json").readText())
                    revalidate()
                    //repaint()
                } catch (e: Exception) {
                    logger.error("当前内容不是正确的json，无法保存", e)
                    JOptionPane.showMessageDialog(null, "当前内容不是正确的json，无法保存", "警告", WARNING_MESSAGE);
                }
            }

            frame.isVisible = true

        }
    }

    private fun addNewTaskMenuItem(jMenu: JMenu, downloadManager: DownloadManager, chart: JFreeChart) {
        val item0 = JMenuItem(languageConfig["newTask"] as String?)
        jMenu.add(item0)
        item0.addActionListener {
            val fileUrl = JOptionPane.showInputDialog(languageConfig["inputAddress"] as String?)
            if (fileUrl != null) startMissionForUrl(fileUrl, downloadManager, chart)
        }
    }

    /**
     * 创建数据集合
     * @return dataSet
     */
    private fun createDataSet(): XYDataset {
        // 实例化DefaultCategoryDataset对象
        val dataSet = TimeSeriesCollection()

        // 向数据集合中添加数据
        val speedSeries = TimeSeries(languageConfig["time"] as String?)
        dataSet.addSeries(speedSeries)
        return dataSet
    }

    /**
     * 创建JFreeChart对象
     * @return chart
     */
    private fun createChart(): JFreeChart {
        val standardChartTheme = StandardChartTheme("CN") //创建主题样式
        standardChartTheme.extraLargeFont = Font("隶书", Font.BOLD, 20) //设置标题字体
        standardChartTheme.regularFont = Font("宋体", Font.PLAIN, 15) //设置图例的字体
        standardChartTheme.largeFont = Font("宋体", Font.PLAIN, 15) //设置轴向的字体
        ChartFactory.setChartTheme(standardChartTheme)//设置主题样式
        // 通过ChartFactory创建JFreeChart
        return ChartFactory.createTimeSeriesChart(
                languageConfig["speed"] as String?,
                languageConfig["time"] as String?,
                "",
                createDataSet(),
                true,
                false,
                false)

    }

    /**
     * 开始任务
     * @param fileUrl 文件下载地址
     */
    private fun startMissionForUrl(fileUrl: String, downloadManager: DownloadManager, chart: JFreeChart) {
        val targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
        val missionId = downloadManager.addMission(fileUrl, "F:\\rocketDownloader", targetFileName)
        downloadManager.startMission(missionId)

        var second = Second()
        val series = ((chart.plot as XYPlot).dataset as TimeSeriesCollection).series[0] as TimeSeries

        val executorService = ScheduledThreadPoolExecutor(1,
                BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build())
        executorService.scheduleAtFixedRate({
            logger.info("当前下载百分比为：" + downloadManager.getReadableDownloadedPercent(missionId))

            series.add(second, downloadManager.totalSpeed)
            second = second.next() as Second
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

