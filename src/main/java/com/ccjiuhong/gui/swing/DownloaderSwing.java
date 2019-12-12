package com.ccjiuhong.gui.swing;

import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.gui.coqimpdevmmon.Configuration;
import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;
import com.ccjiuhong.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 * Swing版本的下载器GUI
 *
 * @author G. Seinfeld
 * @since 2019/12/10
 */
@Slf4j
public class DownloaderSwing extends JFrame {
    // 读取配置文件
    File configFile = new File("config/config.json");
    JSONObject jsonConfig = JSONObject.parseObject(FileUtil.readText(configFile));
    // 读取语言包配置
    String lang = jsonConfig.getString("language");

    JSONObject languageConfig = JSONObject.parseObject(FileUtil.readText(new File("config/i18n/" + lang + ".json")));

    DownloaderSwing() {
        // 设置标题
        setTitle(Configuration.TITLE);
        // 设置尺寸
        setSize(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGHT);
        // 在屏幕中央显示
        setLocationRelativeTo(null);
        // 窗口关闭后自动停止程序
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // 获取下载管理器的实例
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();

        // 创建JPanel作为容器，所有其他组件都添加到JPanel中
        GridBagLayout gb = new GridBagLayout();
        JPanel jPanel = new JPanel(gb);
        add(jPanel);

        JPanel leftPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.3;
        c.weighty = 1.0;
        gb.setConstraints(leftPanel, c);
        jPanel.add(leftPanel);

        JFreeChart chart = createChart();
        ChartPanel rightPanel = new ChartPanel(chart);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.7;
        c.weighty = 1.0;
        gb.setConstraints(rightPanel, c);
        jPanel.add(rightPanel);
        // 菜单条
        JMenuBar jMenuBar = new JMenuBar();
        setJMenuBar(jMenuBar);

        JMenu jMenu = new JMenu(languageConfig.getString("task"));
        jMenuBar.add(jMenu);

        // 添加新建下载任务的菜单项
        addNewTaskMenuItem(jMenu, defaultDownloadManager, chart);

        // 添加修改配置的菜单项
        addConfigCenterMenuItem(jMenu);

        setVisible(true);
    }

    private void addConfigCenterMenuItem(JMenu jMenu) {
        JMenuItem item1 = new JMenuItem(languageConfig.getString("configCenter"));
        jMenu.add(item1);
        item1.addActionListener(e -> {
            JDialog frame = new JDialog(this, languageConfig.getString("configCenter"), true);
            frame.setSize(600, 480);
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            String s = FileUtil.readText(configFile);
            JTextArea configText = new JTextArea(s);
            configText.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));

            configText.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    System.out.println("inserted");
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    System.out.println("removed");
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    System.out.println("changed");
                }
            });
            frame.getContentPane().add(configText);

            JMenuBar jMenuBar = new JMenuBar();
            frame.setJMenuBar(jMenuBar);

            JMenu menu = new JMenu("file");
            jMenuBar.add(menu);

            JMenuItem saveItem = new JMenuItem("save");
            menu.add(saveItem);
            saveItem.addActionListener(e1 -> {
                try {
                    JSONObject.parseObject(configText.getText());
                    FileUtil.writeText(configFile, configText.getText());
                    jsonConfig = JSONObject.parseObject(FileUtil.readText(configFile));

                    // 读取语言包配置
                    lang = jsonConfig.getString("language");
                    languageConfig = JSONObject.parseObject(FileUtil.readText(new File("config/i18n/" + lang + ".json")));
                    revalidate();
                } catch (Exception ex) {
                    log.error("当前内容不是正确的json，无法保存", ex);
                    JOptionPane.showMessageDialog(null, "当前内容不是正确的json，无法保存", "警告", WARNING_MESSAGE);
                }
            });

            frame.setVisible(true);
        });
    }

    private void addNewTaskMenuItem(JMenu jMenu, DownloadManager defaultDownloadManager, JFreeChart chart) {
        JMenuItem item0 = new JMenuItem(languageConfig.getString("newTask"));
        jMenu.add(item0);
        item0.addActionListener(e -> {
            String fileUrl = JOptionPane.showInputDialog(languageConfig.getString("inputAddress"));
            if (fileUrl != null) startMissionForUrl(fileUrl, defaultDownloadManager, chart);
        });
    }

    /**
     * 创建数据集合
     *
     * @return dataSet
     */
    private XYDataset createDataSet() {
        // 实例化DefaultCategoryDataset对象
        TimeSeriesCollection dataSet = new TimeSeriesCollection();

        // 向数据集合中添加数据
        TimeSeries speedSeries = new TimeSeries(languageConfig.getString("time"));
        dataSet.addSeries(speedSeries);
        return dataSet;
    }

    /**
     * 创建JFreeChart对象
     *
     * @return chart
     */
    private JFreeChart createChart() {
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN"); //创建主题样式
        standardChartTheme.setExtraLargeFont(new Font("隶书", Font.BOLD, 20)); //设置标题字体
        standardChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));//设置图例的字体
        standardChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15)); //设置轴向的字体
        ChartFactory.setChartTheme(standardChartTheme);//设置主题样式
        // 通过ChartFactory创建JFreeChart
        return ChartFactory.createTimeSeriesChart(
                languageConfig.getString("speed"),
                languageConfig.getString("time"),
                "",
                createDataSet(),
                true,
                false,
                false);

    }

    /**
     * 开始任务
     *
     * @param fileUrl 文件下载地址
     */
    private void startMissionForUrl(String fileUrl, DownloadManager defaultDownloadManager, JFreeChart chart) {
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        int missionId = defaultDownloadManager.addMission(fileUrl, jsonConfig.getString("downloadDirectory"), targetFileName);
        defaultDownloadManager.startOrResumeMission(missionId);

        TimeSeries series = ((TimeSeriesCollection) ((XYPlot) chart.getPlot()).getDataset()).getSeries(0);
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        final Second[] second = new Second[]{new Second()};
        executorService.scheduleAtFixedRate(() -> {
            log.info("当前下载百分比为：" + defaultDownloadManager.getReadableDownloadedPercent(missionId));

            series.add(second[0], defaultDownloadManager.getTotalSpeed());
            second[0] = (Second) second[0].next();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            new DownloaderSwing();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            log.error(e.getMessage(), e);
        }

    }
}




