package xyz.jianzha.gmall.realtime.util

import java.io.InputStreamReader
import java.util.Properties

/**
 * @author Y_Kevin
 * @date 2020-06-15 0:35
 */
object PropertiesUtil {

  /**
   * 测试使用
   */
  def main(args: Array[String]): Unit = {
    val properties: Properties = PropertiesUtil.load("config.properties")
    println(properties.getProperty("kafka.broker.list"))
  }

  def load(propertieName: String): Properties = {
    val prop = new Properties()
    prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertieName), "UTF-8"))
    prop
  }

}
