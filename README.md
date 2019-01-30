# ProxyHttpServer
基于Java 原生TCPSocket实现的小型HttpProxy代理服务器Demo，能够代理http。   
文件大小约1M大小哦。


## 配置
配置从同级目录下的```app.config```中加载。

| 属性  | 值 | 默认值 |
| ------------- | ------------- |------------- |
| nicelee.server.port  | 服务器监听端口  | 7777  |
| nicelee.server.fixedPoolSize  |目前使用fixedThreadPool管理Socket处理线程，可以看作是最大TCP并发连接数  | 30  |
| nicelee.server.socketTimeout  | 最大socket连接时长，单位ms(针对TCP端口被长连接长时间占用) | 120000  |

## 简介
* 运行   
运行```run.bat```即可  
![](https://raw.githubusercontent.com/nICEnnnnnnnLee/ProxyHttpServer/master/source/run.png)  

后台界面如下:  
![](https://raw.githubusercontent.com/nICEnnnnnnnLee/ProxyHttpServer/master/source/cmd-console.png)  

* 功能概览   
客户端代理访问http页面正常(https代理功能实现中哦...)  
![](https://raw.githubusercontent.com/nICEnnnnnnnLee/ProxyHttpServer/master/source/http-page.png)  

目的服务器端Headers如下,其中```123.123.123.123```为伪IP  
![](https://raw.githubusercontent.com/nICEnnnnnnnLee/ProxyHttpServer/master/source/proxy.png)  


## 其它  
* **下载地址**: [https://github.com/nICEnnnnnnnLee/ProxyHttpServer/releases](https://github.com/nICEnnnnnnnLee/ProxyHttpServer/releases)
* **GitHub**: [https://github.com/nICEnnnnnnnLee/ProxyHttpServer](https://github.com/nICEnnnnnnnLee/ProxyHttpServer)  
* **Gitee码云**: [https://gitee.com/NiceLeee/ProxyHttpServer](https://gitee.com/NiceLeee/ProxyHttpServer)  
* **LICENSE**: [Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0.html)


## 更新日志
* v1.1(当前版本)
    * 支持Https代理
    * 修正了一个headers的解析问题, 该问题会导致不限于Referer标签值获取失败等问题
    * 修正了某些bug, 该bug会导致post访问时代理失败
    * 与客户端/服务器端输入输出全部采用字节流
* v1.0
    * 支持http代理(已完成,（づ￣3￣）づ╭❤～)
    * 目前仅作中转,尚未支持https的代理,待继续完成.. (￣ε(#￣)☆╰╮o(￣皿￣///)




