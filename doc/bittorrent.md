BT下载实现原理：

## Bencoding编码解析
bencoding现有四种类型的数据：srings(字符串)，integers(整数)，lists(列表)，dictionaries(字典)

编码规则如下：
### 字符串类型
strings(字符串)编码为：<字符串长度>：<字符串>，字符串长度单位为字节，没开始或结束标记

例如： 
- 4:test 表示为字符串"test"
- 4:例子 表示为字符串“例子”

### 整数类型
integers(整数)编码为：i<整数>e，开始标记i，结束标记为e

例如： 
- i1234e 表示为整数1234
- i-1234e 表示为整数-1234

整数没有大小限制，i0e 表示为整数0，i-0e 为非法，以0开头的为非法如： i01234e 为非法

### 列表类型
lists(列表)编码为：l<bencoding编码类型>e，开始标记为l,结束标记为e

列表里可以包含任何bencoding编码类型，包括整数，字符串，列表，字典。

例如：
- l4:test5:abcdee 表示为二个字符串["test","abcde"]

### 字典类型
dictionaries(字典)编码为d<bencoding字符串><bencoding编码类型>e，开始标记为d,结束标记为e
关键字必须为bencoding字符串，值可以为任何bencoding编码类型

例如： 
- d3:agei20ee 表示为{"age"=20}
- d4:path3:C:/8:filename8:test.txte 表示为{"path"="C:/","filename"="test.txt"}

## BT种子文件结构
BT种子文件（.torrent）的具体文件结构如下：
全部内容必须都为Bencoding编码类型。整个文件为一个字典结构，包含如下关键字：
- announce： tracker 服务器的 URL（字符串）；
- announce-list（可选）：备用 tracker 服务器列表（列表）；
- creation date（可选）：种子创建的时间，Unix 标准时间格式，从 1970 1 月1 日 00：00：00 到创建时间的秒数（整数）；
- comment（可选）：备注（字符串） created by（可选）：创建人或创建程序的信息（字符串）；
- info：一个字典结构，包含文件的主要信息。分为二种情况，单文件结构或多文件结构。
- 单文件info结构如下：
    - length：文件长度，单位字节（整数）；
    - md5sum（可选）：长 32 个字符的文件的 MD5 校验和，BT 不使用这个值，只是为了兼容一些程序所保留!（字符串）；
    - name：文件名（字符串）；
    - piece length：每个块的大小，单位字节（整数）， 块长一般来说是 2 的权值；
    - pieces：每个块的 20 个字节的 SHA1 Hash 的值（二进制格式）。
- 多文件info结构如下：
    - files：一个字典结构；
    - length：文件长度，单位字节（整数）；
    - md5sum（可选）：与单文件结构中相同；
    - path：文件的路径和名字，是一个列表结构，如\test\test。txt 列表为l4：test8test。txte；
    - name：最上层的目录名字（字符串）；
    - piece length：与单文件结构中相同；
    - pieces：与单文件结构中相同。

## BT下载的实现
初始化构造五大服务组件：
- MessageDispatcher
    - 遍历Consumer和Supplier，处理消息分发（与Peer进行交互）
- ConnectionSource
    - 使用IncomingConnectionListener监听外面的Peer连入的情况，建立连接
- PeerConnectionPool
    - 维护与Peer连接的情况
    - 每秒钟清理一次连接，关闭失效的连接
- PeerRegistry
    - 每隔一段时间访问并收集Peer
- DataReceivingLoop
    - 每秒收集数据

构造核心组件：ChainProcessor

对于BT下载：
- 获取种子阶段
    - 将torrentSupplier提供的种子放置到context中
- 创建会话阶段
    - 为context中的torrent注册一个TorrentDescriptor
    - 创建MessageRouter和PeerWorkerFactory
    - 创建TorrentWorker
    - 创建TorrentSessionState并保存到context中
- 初始化下载文件处理的阶段
    - 从TorrentDescriptor中获取到Bitfield信息并创建统计信息
    - 创建数据工人，用于处理阻塞请求
    - 路由器注册各种消息代理，可以作为消息消费者或者生产者    
- 选择文件的阶段
    - 根据FileSelector选择文件
    - 获取选中文件的有效Piece
    - 创建Assignments
    - 更新跳过的piece，将Assignments放到context中
- 处理下载文件的阶段
    - 如果有Tracker服务器，先获取Tracker服务器
    - 通过TorrentDescriptor发送请求，开始下载
        - 通过EventBus触发开始下载的事件
    - 每秒钟检查Descriptor的状态，如果剩余piece为0，则结束下载
- 做种阶段
    - 每秒钟检查Descriptor的状态，无限循环

对于Magnet下载：
- 创建会话阶段
- 获取元信息阶段
- 初始化Magnet下载文件处理的阶段
- 选择文件的阶段
- 处理Magnet下载文件的阶段
- 做种阶段