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

[具体介绍](https://www.cnblogs.com/mistletoe9527/p/10734762.html)
