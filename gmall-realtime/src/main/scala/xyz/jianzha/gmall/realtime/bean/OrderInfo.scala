package xyz.jianzha.gmall.realtime.bean

/**
 * @author Y_Kevin
 * @date 2020-06-21 1:10
 */
/**
 *
 * @param area             地区
 * @param consignee        收件人
 * @param orderComment     订单描述
 * @param consigneeTel     收件人电话
 * @param operateTime      下单时间
 * @param orderStatus      订单状态
 * @param paymentWay       支付方式
 * @param userId           用户ID
 * @param imgUrl           订单商品图片
 * @param totalAmount      订单总额
 * @param expireTime       订单支付超时时间
 * @param deliveryAddress  收货地址
 * @param createTime       创建时间
 * @param trackingNo       物流编号
 * @param parentOrderId    上级ID
 * @param outTradeNo       支付单号
 * @param id               id
 * @param tradeBody        订单描述
 * @param createDate       日期
 * @param createHour       小时
 * @param createHourMinute 小时分秒
 */
case class OrderInfo(
                      area: String,
                      consignee: String,
                      orderComment: String,
                      var consigneeTel: String,
                      operateTime: String,
                      orderStatus: String,
                      paymentWay: String,
                      userId: String,
                      imgUrl: String,
                      totalAmount: Double,
                      expireTime: String,
                      deliveryAddress: String,
                      createTime: String,
                      trackingNo: String,
                      parentOrderId: String,
                      outTradeNo: String,
                      id: String,
                      tradeBody: String,
                      var createDate: String,
                      var createHour: String,
                      var createHourMinute: String
                    )