package com.ccjiuhong.gui.swing

import java.awt.Font
import java.awt.HeadlessException
import javax.swing.*

/**
 * @author G. Seinfeld
 * @date 2019/07/01
 */
class ConfigWindow @Throws(HeadlessException::class)
constructor() : JFrame() {
    init {
        title = "配置中心"

        setLocationRelativeTo(null)

        setSize(600, 480)

        defaultCloseOperation = EXIT_ON_CLOSE

        val jPanel = JPanel()
        add(jPanel)

        val downloadDirLabel = JLabel("下载地址：")
        downloadDirLabel.font = Font("Microsoft YaHei", Font.PLAIN, 16)
        jPanel.add(downloadDirLabel)


        val downloadDirInput = JTextField(20)
        downloadDirInput.font = Font("Microsoft YaHei", Font.PLAIN, 16)
        jPanel.add(downloadDirInput)

        val downloadDirButton = JButton("选择")
        downloadDirButton.font = Font("Microsoft YaHei", Font.PLAIN, 16)
        downloadDirButton.addActionListener {
            val frame = JFrame()
            val downloadDir = JFileChooser()
            downloadDir.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            frame.contentPane.add(downloadDir)
            frame.setSize(600, 480)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.isVisible = true
            frame.requestFocus()
        }
        jPanel.add(downloadDirButton)

        val submit = JButton("保存更改")
        jPanel.add(submit)

        isVisible = true
    }
}

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
    ConfigWindow()
}