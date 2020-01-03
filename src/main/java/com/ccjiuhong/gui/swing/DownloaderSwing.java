package com.ccjiuhong.gui.swing;

import com.alibaba.fastjson.JSONObject;
import com.ccjiuhong.gui.swing.frame.MainFrame;
import com.ccjiuhong.gui.swing.frame.MainPanel;
import com.ccjiuhong.mgt.DefaultDownloadManager;
import com.ccjiuhong.mgt.DownloadManager;
import com.ccjiuhong.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 * Swing版本的下载器GUI
 *
 * @author G. Seinfeld
 * @since 2019/12/10
 */
@Slf4j
public class DownloaderSwing {
    // 读取配置文件
    public static File configFile = new File("config/config.json");
    public static JSONObject jsonConfig = JSONObject.parseObject(FileUtil.readText(configFile));
    // 读取语言包配置
    public static String lang = jsonConfig.getString("language");

    public static JSONObject languageConfig = JSONObject.parseObject(FileUtil.readText(new File("config/i18n/" + lang + ".json")));

    private MainFrame mainFrame;

    DownloaderSwing() {
        // 获取下载管理器的实例
        DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();

        mainFrame = MainFrame.getInstance();

        // 创建JPanel作为容器，所有其他组件都添加到JPanel中
        MainPanel mainPanel = MainPanel.getInstance();
        mainFrame.add(mainPanel);


        // 菜单条
        JMenuBar jMenuBar = new JMenuBar();
        mainFrame.setJMenuBar(jMenuBar);

        JMenu jMenu = new JMenu(languageConfig.getString("task"));
        jMenuBar.add(jMenu);


        JFreeChart chart = createChart();
        // 添加新建下载任务的菜单项
        addNewTaskMenuItem(jMenu, defaultDownloadManager, chart);

        // 添加修改配置的菜单项
        addConfigCenterMenuItem(jMenu);

        mainFrame.setVisible(true);
    }

    private void addConfigCenterMenuItem(JMenu jMenu) {
        JMenuItem item1 = new JMenuItem(languageConfig.getString("configCenter"));
        jMenu.add(item1);
        item1.addActionListener(e -> {
            JDialog frame = new JDialog(mainFrame, languageConfig.getString("configCenter"), true);
            frame.setSize(600, 480);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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
                    mainFrame.revalidate();
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
            if (fileUrl != null) startMissionForUrl(fileUrl, defaultDownloadManager);
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
    private void startMissionForUrl(String fileUrl, DownloadManager defaultDownloadManager) {
        String targetFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        int missionId = defaultDownloadManager.addMission(fileUrl, jsonConfig.getString("downloadDirectory"), targetFileName);
        defaultDownloadManager.startOrResumeMission(missionId);


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





