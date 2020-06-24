package xyz.jianzha.gmall.logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.jianzha.gmall.common.constant.GmallConstant;

/**
 * @author Y_Kevin
 * @date 2020-06-13 16:14
 */
@RestController
public class LoggerController {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(LoggerController.class);

    @PostMapping("/log")
    public String dolog(@RequestParam("log") String logJson) {

        // 补时间戳
        JSONObject jsonObject = JSON.parseObject(logJson);
        jsonObject.put("ts", System.currentTimeMillis());

        // 落盘到logfile  log4j
        logger.info(jsonObject.toJSONString());

        // 发送Kafka
        if ("startup".equals(jsonObject.getString("type"))) {
            kafkaTemplate.send(GmallConstant.KAFKA_TOPIC_STARTUP, jsonObject.toJSONString());
        } else {
            kafkaTemplate.send(GmallConstant.KAFKA_TOPIC_EVENT, jsonObject.toJSONString());
        }

//        System.out.println(logJson);
        return "success";
    }

}
