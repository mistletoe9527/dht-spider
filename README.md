# 介绍
主要分为两部分
- 爬虫
- 搜索

爬虫主要是通过BitTorrent dht协议
搜索使用es
# 使用
只需要配置一下esHost 和esPort
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

[具体介绍](https://www.cnblogs.com/mistletoe9527/p/10734762.html)
