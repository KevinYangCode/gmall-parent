package xyz.jianzha.gmall.canal.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.CaseFormat;
import xyz.jianzha.gmall.canal.util.MyKafkaSender;
import xyz.jianzha.gmall.common.constant.GmallConstant;

import java.util.List;

/**
 * @author Y_Kevin
 * @date 2020-06-20 16:29
 */
public class CanalHandler {

    public static void handle(String tableName, CanalEntry.EventType entryType, List<CanalEntry.RowData> rowDatasList) {
        if ("order_info".equals(tableName) && CanalEntry.EventType.INSERT.equals(entryType)) {
            // 下单操作
            // 行集展开
            for (CanalEntry.RowData rowData : rowDatasList) {
                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                JSONObject jsonObject = new JSONObject();
                // 列集展开
                for (CanalEntry.Column column : afterColumnsList) {
                    System.out.println(column.getName() + ":::" + column.getValue());
                    // 小写转为首字母小写的驼峰
                    String propertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.getName());
                    jsonObject.put(propertyName, column.getValue());
                }
                MyKafkaSender.send(GmallConstant.KAFKA_TOPIC_ORDER, jsonObject.toJSONString());
            }
        }
    }
}
