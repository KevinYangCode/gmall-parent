package xyz.jianzha.gmall.common.util

import java.util.Objects

import io.searchbox.client.JestClientFactory
import org.apache.http.util.EntityUtils
import org.apache.http.{Header, HttpHost, RequestLine}
import org.elasticsearch.client.{Request, Response, RestClient, RestClientBuilder}

/**
 * @author Y_Kevin
 * @date 2020-06-17 15:46
 */
object MyEsUtil2 {
  private val ES_HOST = "hadoop101"
  private val ES_HTTP_PORT = 9200
  private var factory: JestClientFactory = null

  /**
   * 获取客户端
   */
  private def getClient: RestClient = {
    val restClientBuilder: RestClientBuilder = RestClient.builder(new HttpHost(ES_HOST, ES_HTTP_PORT, "http"))
    restClientBuilder.setMaxRetryTimeoutMillis(10000)
    val restClient: RestClient = restClientBuilder.build()
    restClient
  }

  def close(client: RestClient): Unit = {
    if (!Objects.isNull(client))
      try
        client.close()
      catch {
        case e: Exception =>
          e.printStackTrace()
      }
  }

  private def indexDoc(): Unit = {
    val request: Request = new Request("POST", "/gmall_test/_doc")

    val restClient: RestClient = getClient

    val source = "{\n \"name\":\"zhang3\",\n \"age\":123,\n \"amount\": 260.1,\n \"phone_num\":\"138***2123\"}"
    request.setJsonEntity(source)

    //    val params = new mutable.HashMap[String, String]
    //    val entity: HttpEntity = new NStringEntity(source, ContentType.APPLICATION_JSON);
    //    val response: Response = restClient.performRequest("GET", "/gmall_test/_doc", params, entity)

    val response: Response = restClient.performRequest(request)

    val requestLine: RequestLine = response.getRequestLine
    val host: HttpHost = response.getHost
    val statusCode: Int = response.getStatusLine.getStatusCode
    val headers: Array[Header] = response.getHeaders
    val responseBody: String = EntityUtils.toString(response.getEntity)

    println("================================")
    println(" requestLine===>" + requestLine)
    println("host===>" + host)
    println("statusCode===>" + statusCode)
    println("headers===>" + headers.map(println))
    println()
    println("responseBody===>" + responseBody)
    println("================================")

    close(restClient)
  }

  def main(args: Array[String]): Unit = {
    indexDoc()
  }

}
