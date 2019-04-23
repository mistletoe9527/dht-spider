# 介绍
主要分为两部分
- 爬虫
- 搜索

实现[bep_0005](http://www.bittorrent.org/beps/bep_0005.html)

实现[bep_0009](http://www.bittorrent.org/beps/bep_0009.html)

实现[bep_0010](http://www.bittorrent.org/beps/bep_0010.html)

爬虫主要是通过BitTorrent dht协议
搜索使用es
# 使用

只需要配置一下es的两个参数esHost 和esPort
server：port表示web端口
config:port 表示udp开启的端口
threadCount：线程数
如果threadCount是20，表示会有20个node加入dht，也就是会开放20个udp端口从6905~6924
```
server:
  port: 8082
config:
    port: 6905
    threadCount: 20
    esHost: xx
    esPort: 9200
    address:
      super-address-list:
        - dht.transmissionbt.com:6881
```

为了方便，最后数据的落地没有存数据库，存了文件，如果有需要存数据库的只需要把一下FileUtil的write方法改成保存数据库就可以。
如果还是使用存文件的话，需要配置一个文件目录,就把FileUtil的path参数改成自己保存的目录即可。
```
public class FileUtil {
    private static String filePath="infohash.txt";

    private static String nodePath="node.txt";

    private static String dataPath="data.txt";

    private static final String path="";
}
```
从概念到实现分成了四篇,点击可看[具体介绍](https://www.cnblogs.com/mistletoe9527/p/10734762.html)
# License
See the [License](https://github.com/mistletoe9527/dht-spider/blob/master/LICENSE) file for details.
