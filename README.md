# 电商实时计算

## 技术
| 框架 | 版本 |
| ---- | ---- |
| zookeeper | 3.4.14 |
| kafka | 0.11.0.0 |
| spark | 2.1.3 |
| nginx | 1.18.0 |
| elasticsearch | 6.6.0 |
| kibana | 6.6.0 |
| SpringBoot | 1.5.17.RELEASE |
| canal | 1.1.4 |

**环境**
1. 3台阿里云 云服务器ECS
2. JDK-1.8+
3. MySQL-5.6
5. Redis-5.0.8

## 模块：gmall-common
公共模块；存放常量名；连接ES集群的工具类

## 模块：gmall-mock
模拟实时产生数据

> 在 xyz.jianzha.gmall.mock.util.LogUploader 设置数据发送出口： http://logserver/log
> 这里的 logserver 在 hosts 配置映射到 服务器1

模块主启动类为：xyz.jianzha.gmall.mock.JsonMocker

## 模块：gmall-logger
一个 SpringBoot 工程，唯一接口：/log ,接收模拟数据通过nginx转发到这里，然后进行日志落盘 和 发送到 Kafka

> 使用：通过Maven工具插件 install 进行打包，将jar包发送到3台服务器里

nginx配置
```conf
.....
#gzip  on;
    upstream logserver{
      server    hadoop101:8080 weight=1;
      server    hadoop102:8080 weight=1;
      server    hadoop103:8080 weight=1;
    }

    server {
        listen       80;
        server_name  logserver;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
            proxy_pass http://logserver;
            proxy_connect_timeout 10;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
.....
```
启动nginx和logger工程脚本 （工程里的配置文件设置端口为80了，但是nginx转发为8080，所以下面启动时设置端口为8080）
```shell script
#!/bin/bash
JAVA_BIN=/opt/module/jdk1.8.0_241/bin/java
PROJECT=gmall
APPNAME=gmall-logger-0.0.1-SNAPSHOT.jar
SERVER_PORT=8080

case $1 in
 "start")
   {
    for i in hadoop101 hadoop102 hadoop103
    do
     echo "========启动日志服务: $i==============="
    ssh $i  "source /etc/profile;java -Xms32m -Xmx64m -jar /opt/module/datas/$PROJECT/$APPNAME --server.port=$SERVER_PORT >/dev/null 2>&1 &"
    done

    echo "===============启动NGINX=================="
    /opt/module/nginx/sbin/nginx
  };;
  "stop")
  {
    echo "===============关闭NGINX====================="
    /opt/module/nginx/sbin/nginx -s stop

    for i in hadoop101 hadoop102 hadoop103
    do
     echo "========关闭日志服务: $i==============="
     ssh $i "ps -ef|grep $APPNAME |grep -v grep|awk '{print \$2}'|xargs kill" >/dev/null 2>&1
    done
  };;
   esac
```
启动
```shell script
./脚本名字 start
```

##  模块：gmall-realtime
数据处理模块

通过消费Kafka的获取数据，利用SparkStream和Redis进行处理，最后保存进ES中

模块主启动类为：xyz.jianzha.gmall.realtime.app.DauApp

##  模块：gmall-publisher
一个 SpringBoot 工程，获取ES中的数据，进行处理，提供接口

模块主启动类为：xyz.jianzha.gmall.publisher.GmallPublisherApplication

##  模块：dw-chart
UI页面工程；从 gmall-publisher 模块提供的接口获取数据
> 注意：src\main\resources\config\config.properties

模块主启动类为：xyz.jianzha.gmall.DemoApplication

##  模块：gmall-canal
监听Mysql的数据变化，将变化发送到Kafka

模块主启动类为：xyz.jianzha.gmall.canal.app.CanalApp

## 总结操作：

1.  启动 Zookeeper、Kafka、ES、Redis、MySQL
2.  创建 3个 Kafka主题 GMALL_STARTUP、GMALL_EVENT、GMALL_ORDER
3.  定义 2个 ES  索引 gmall_dau、gmall_order
4.  启动 nginx和logger.jar（./脚本名字 start）
5.  启动 gmall-realtime 模块的主启动类
6.  启动 gmall-mock 模块的主启动类
7.  启动 gmall-canal 模块的主启动类
8.  启动 gmall-publisher 模块的主启动类
9.  启动 dw-chart 模块的主启动类
10. 进入 dw-chart 模块的UI页面查看 localhost:8089/index


### 备注

创建主题
```shell script
bin/kafka-topics.sh --zookeeper hadoop101:2181 --create --replication-factor 3 --partitions 1 --topic GMALL_STARTUP
bin/kafka-topics.sh --zookeeper hadoop101:2181 --create --replication-factor 3 --partitions 1 --topic GMALL_EVENT
bin/kafka-topics.sh --zookeeper hadoop101:2181 --create --replication-factor 3 --partitions 1 --topic GMALL_ORDER
```
定义索引-gmall_dau
```shell script
PUT gmall_dau
{
  "mappings": {
    "_doc":{
      "properties":{
         "mid":{
           "type":"keyword" 
         },
         "uid":{
           "type":"keyword"
         },
         "area":{
           "type":"keyword"
         },
         "os":{
           "type":"keyword"
         },
         "ch":{
           "type":"keyword"
         },
         "vs":{
           "type":"keyword"
         },
         "logDate":{
           "type":"keyword"
         },
         "logHour":{
           "type":"keyword"
         },
         "logHourMinute":{
           "type":"keyword"
         },
         "ts":{
           "type":"long"
         } 
      }
    }
  }
}
```
定义索引-gmall_order
```shell script
 PUT gmall_order 
  {
    "mappings" : {
      "_doc" : {
        "properties" : {
          "provinceId" : {
            "type" : "keyword"
          },
          "consignee" : {
            "type" : "keyword",
            "index":false
          },
          "consigneeTel" : {
            "type" : "keyword",
            "index":false
          },
          "createDate" : {
            "type" : "keyword"
          },
          "createHour" : {
            "type" : "keyword"
          },
          "createHourMinute" : {
            "type" : "keyword"
          },
          "createTime" : {
            "type" : "keyword"
          },
          "deliveryAddress" : {
            "type" : "keyword"
          },
          "expireTime" : {
            "type" : "keyword"
          },
          "id" : {
            "type" : "keyword"
          },
          "imgUrl" : {
            "type" : "keyword",
            "index":false
          },
          "operateTime" : {
            "type" : "keyword"
          },
          "orderComment" : {
            "type" : "keyword",
            "index":false
          },
          "orderStatus" : {
            "type" : "keyword"
          },
          "outTradeNo" : {
            "type" : "keyword",
            "index":false 
          },
          "parentOrderId" : {
            "type" : "keyword" 
          },
          "paymentWay" : {
            "type" : "keyword"
          },
          "totalAmount" : {
            "type" : "double"
          },
          "trackingNo" : {
            "type" : "keyword"
          },
          "tradeBody" : {
            "type" : "keyword",
            "index":false
          },
          "userId" : {
            "type" : "keyword"
          }
        }
      }
    }
  }
```