package xyz.jianzha.gmall.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import xyz.jianzha.gmall.common.constant.GmallConstant
import xyz.jianzha.gmall.common.util.MyEsUtil
import xyz.jianzha.gmall.realtime.bean.StartUpLog
import xyz.jianzha.gmall.realtime.util.{MyKafkaUtil, RedisUtil}

/**
 * @author Y_Kevin
 * @date 2020-06-15 0:29
 */
object DauApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("dau_app").setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(5))

    val inputDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_STARTUP, streamingContext)

    //    inputDStream.foreachRDD(
    //      rdd =>
    //        println(rdd.map(_.value()).collect().mkString("\n"))
    //    )

    // 转换处理
    val startUpLogStream: DStream[StartUpLog] = inputDStream.map {
      record =>
        val jsonStr: String = record.value()
        val startUpLog: StartUpLog = JSON.parseObject(jsonStr, classOf[StartUpLog])
        val date = new Date(startUpLog.ts)
        val dateStr: String = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
        val dateArr: Array[String] = dateStr.split(" ")
        startUpLog.logDate = dateArr(0)
        startUpLog.logHour = dateArr(1).split(":")(0)
        startUpLog.logHourMinute = dateArr(1)

        startUpLog
    }
    //    startUpLogStream.foreachRDD(rdd => rdd.foreach(println))

    // 利用redis进行去重过滤
    val filterDStream: DStream[StartUpLog] = startUpLogStream.transform {
      rdd =>
        println("过滤前：" + rdd.count())
        // driver  周期性执行
        val curDate: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
        val jedisClient: Jedis = RedisUtil.getJedisClient
        val key: String = "dau:" + curDate
        val dauSet: util.Set[String] = jedisClient.smembers(key)
        val dauBC: Broadcast[util.Set[String]] = streamingContext.sparkContext.broadcast(dauSet)
        val filteredRDD: RDD[StartUpLog] = rdd.filter {
          startUpLog =>
            // executor
            val dauSet: util.Set[String] = dauBC.value
            !dauSet.contains(startUpLog.mid)
        }
//        println("过滤后：" + filteredRDD.count())
        filteredRDD
    }

    // 去重思路：把相同的mid的数据分成一组，每组取第一个
    val groupByMidDStream: DStream[(String, Iterable[StartUpLog])] = filterDStream.map {
      startUpLog => (startUpLog.mid, startUpLog)
    }.groupByKey()
    val distinctDStream: DStream[StartUpLog] = groupByMidDStream.flatMap {
      case (mid, startUpLogItr) =>
        startUpLogItr.take(1)
    }

    // 保存到redis中
    distinctDStream.foreachRDD {
      rdd =>
        // driver
        // redis type: set
        // key dau:2020-06-15  value : mids
        rdd.foreachPartition {
          startUpLogItr =>
            // executor
            val jedisClient: Jedis = RedisUtil.getJedisClient
            // Iterable迭代器的数据只能用一次
            val list: List[StartUpLog] = startUpLogItr.toList
            for (startUpLog <- startUpLogItr) {
              val key: String = "dau:" + startUpLog.logDate
              val value: String = startUpLog.mid
              jedisClient.sadd(key, value)
              println(startUpLog) // 往es中保存
            }

            MyEsUtil.indexBulk(GmallConstant.ES_INDEX_DAU,list)

            jedisClient.close()
        }
    }
    distinctDStream.foreachRDD(println(_))


    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
