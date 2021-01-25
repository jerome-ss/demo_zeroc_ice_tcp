# demo_zeroc_ice_tcp
Zeroc Ice TCP长连接 实现推送功能

## 系统配置
IDE:Eclipse
ICE Library:ice-3.6.2.jar

## 运行例子步骤
1. 运行Main.java 启动服务端
	里面有启动一个线程 二十秒后会往注册的客户端发送数据
2. 运行SwitchClient.java启动客户端对服务端进行心跳
