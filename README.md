# CrossLAN

Netty学习，TCP内网穿透，代码参考(抄)[whz11/QyNat](https://github.com/whz11/QyNat)大佬的仓库代码


## 使用方法
1. 克隆代码 
```git clone https://github.com/ceylog/CrossLAN.git```
2. 编译打包 
```maven clean package -DskipTests```
3. 运行服务端 
```java -jar server/target/CrossLAN-Server.jar --crosslan.server.port=8088 --crosslan.server.bindHost=0.0.0.0 --crosslan.server.token=1234abcd```
4. 运行客户端 
```java -jar client/target/CrossLAN-Client.jar --crosslan.client.serverAddress=127.0.0.1 --crosslan.client.serverPort=8088 --crosslan.client.token=abcd1234 --crosslan.client.remotePort=9999 --crosslan.client.proxyAddress=127.0.0.1 --crosslan.client.proxyPort=80```
5. 访问 http://localhost:9999 相当于访问本地的 http://localhost:80
## 参数说明
### 服务端：
- --crosslan.server.port=8088  服务端口，用于客户端注册
- --crosslan.server.bindHost=0.0.0.0 服务端绑定网卡地址,如不确定固定0.0.0.0即可
- --crosslan.server.token=1234abcd  认证token

### 客户端
- --crosslan.client.serverAddress=server.domain.com 服务端IP/域名
- --crosslan.client.serverPort=8088  服务端端口
- --crosslan.client.token=abcd1234  服务端认证token，与服务端一致
- --crosslan.client.remotePort=9999 需暴露在服务端的对外端口，跟本地内网服务一一对应 比如这里的9999对应本地的80
- --crosslan.client.proxyAddress=127.0.0.1 内网本地服务地址
- --crosslan.client.proxyPort=80 内网本地服务端口

> 再举一🌰 假设服务端网卡绑定的域名是server.domain.com则访问http://server.domain.com/9999 即可访问到本地的http://127.0.0.0:8080
