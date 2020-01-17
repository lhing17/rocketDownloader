# rocketDownloader



<h1 align="center">欢迎使用rocketDownloader 👋</h1>
<p>
  <img src="https://img.shields.io/badge/version-0.1.0-blue.svg?cacheSeconds=2592000" />
  <img src="https://img.shields.io/badge/java-%3E%3D11.0.0-blue.svg" />
  <a href="https://github.com/lhing17/rocketDownloader#readme">
    <img alt="Documentation" src="https://img.shields.io/badge/documentation-yes-brightgreen.svg" target="_blank" />
  </a>
  <a href="https://github.com/lhing17/rocketDownloader/graphs/commit-activity">
    <img alt="Maintenance" src="https://img.shields.io/badge/Maintained%3F-yes-green.svg" target="_blank" />
  </a>
  <a href="https://github.com/lhing17/rocketDownloader/blob/master/LICENSE">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" target="_blank" />
  </a>
</p>

> 火箭下载器是基于Java实现的多线程下载工具，支持http、https、ftp、thunder、magnet等多种协议，支持BT下载

### 🏠 [项目主页](https://github.com/lhing17/rocketDownloader)

## 开发环境

- java(jdk) &gt;= 11.0.0
- 构建工具使用maven或gradle
    - maven &gt;= 3.3.9
    - gradle &gt;= 6.1

## 项目构建

maven: 
```sh
mvn clean package -Dmaven.test.skip=true
```
gradle: 
```sh
gradle build -x test
```

## 执行单元测试
maven: 
```sh
mvn test
```

gradle:
```sh
gradle test 
```
## 用法
```java
    // 创建下载管理器
    DownloadManager defaultDownloadManager = DefaultDownloadManager.getInstance();
    // 添加下载任务
    int missionId = defaultDownloadManager.addMission(url, "/home/lhing17/rocketDownloader", "a");
    // 开始下载任务
    defaultDownloadManager.startOrResumeMission(missionId);
```

## 规划特性
- [x] 多线程
- [x] 断点续传
- [x] 支持BT下载
- [x] 支持magnet link
- [ ] electron GUI
- [ ] SWING GUI （初步）
- [ ] 多语言支持 （初步）

## 项目作者

👤 [**G_Seinfeld**](https://github.com/lhing17)

👤 [**dagerer**](https://github.com/dagerer) 


## 🤝 贡献代码

欢迎提[issue](https://github.com/lhing17/rocketDownloader/issues)和[PR](https://github.com/lhing17/rocketDownloader/pulls)。

## 支持项目发展

如果你觉得项目还不错，欢迎Star和Fork。

## 📝 许可证

版权所有 © 2019-2020 [G_Seinfeld](https://github.com/lhing17)。<br />
本项目使用[MIT](https://github.com/lhing17/rocketDownloader/blob/master/LICENSE)许可证。

***

本README文档是由 [readme-md-generator](https://github.com/kefranabg/readme-md-generator)生成的。