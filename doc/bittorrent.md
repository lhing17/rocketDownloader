BT下载实现原理：

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