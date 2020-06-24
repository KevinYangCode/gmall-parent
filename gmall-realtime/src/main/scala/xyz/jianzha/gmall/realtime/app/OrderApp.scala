package xyz.jianzha.gmall.realtime.app

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import xyz.jianzha.gmall.common.constant.GmallConstant
import xyz.jianzha.gmall.common.util.MyEsUtil
import xyz.jianzha.gmall.realtime.bean.OrderInfo
import xyz.jianzha.gmall.realtime.util.MyKafkaUtil

/**
 * @author Y_Kevin
 * @date 2020-06-21 1:04
 */
object OrderApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("order_app").setMaster("local[*]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(5))

    // 保存到ES
    // 数据脱敏  补充时间戳
    val inputDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_ORDER, streamingContext)


    val orderInfoDStream: DStream[OrderInfo] = inputDStream.map {
      record =>
        val jsonStr: String = record.value()
        val orderInfo: OrderInfo = JSON.parseObject(jsonStr, classOf[OrderInfo])
        val telSplit: (String, String) = orderInfo.consigneeTel.splitAt(4)
        orderInfo.consigneeTel = telSplit._1 + "**************"

        val dateTimeArr: Array[String] = orderInfo.createTime.split(" ")
        orderInfo.createDate = dateTimeArr(0)
        val timeArr: Array[String] = dateTimeArr(1).split(":")
        orderInfo.createHour = timeArr(0)
        orderInfo.createHourMinute = timeArr(0) + ":" + timeArr(1)
        orderInfo
    }
    orderInfoDStream

    // 增加字段  0或者1 标识该订单是否是该用户首次下单
    orderInfoDStream.foreachRDD {
      rdd =>
        rdd.foreachPartition {
          orderItr =>
            MyEsUtil.indexBulk(GmallConstant.ES_INDEX_ORDER, orderItr.toList)
        }
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
