# 断点续传实现原理

- 启动线程时，创建临时配置文件记录当前线程读取字节的位置以及线程读取字节的结束位置。
- 线程下载过程中，每隔若干秒更新一次配置文件。
- 停止线程中，更新一次配置文件。
- 重新下载时，程序读取配置文件中的内容，同时判断临时下载的文件是否存在。如存在，根据配置文件中的内容重新建立连接并进行下载。


# BT下载实现断点续传
