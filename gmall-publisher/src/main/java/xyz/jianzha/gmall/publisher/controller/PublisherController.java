package xyz.jianzha.gmall.publisher.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.jianzha.gmall.publisher.service.PublisherService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Y_Kevin
 * @date 2020-06-17 23:26
 */
@RestController
public class PublisherController {

    @Autowired
    PublisherService publisherService;

    @GetMapping("realtime-total")
    public String getTotal(@RequestParam("date") String date) {
        List<Map<Object, Object>> totalList = new ArrayList<>();
        Map<Object, Object> dauMap = new HashMap<>();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");

        Integer dauTotal = publisherService.getDauTotal(date);

        dauMap.put("value", dauTotal);
        totalList.add(dauMap);

        // 假数据：新增设备
        Map<Object, Object> newMidMap = new HashMap<>();
        newMidMap.put("id", "newMid");
        newMidMap.put("name", "新增设备");
        newMidMap.put("value", 233);
        totalList.add(newMidMap);

        // 新增交易额
        Map<Object, Object> orderAmountMap = new HashMap<>();
        orderAmountMap.put("id", "orderAmount");
        orderAmountMap.put("name", "新增交易额");
        Double orderAmount = publisherService.getOrderAmount(date);
        orderAmountMap.put("value", orderAmount);
        totalList.add(orderAmountMap);

        return JSON.toJSONString(totalList);
    }

    @GetMapping("realtime-hour")
    public String getDauHourTotal(@RequestParam("id") String id, @RequestParam("date") String today) {
        if ("dau".equals(id)) {
            // 今天
            Map<Object, Object> dauHourTDMap = publisherService.getDauHourMap(today);

            // 昨天
            String yesterday = getYesterday(today);
            Map<Object, Object> dauHourYDMap = publisherService.getDauHourMap(yesterday);

            Map<Object, Object> hourMap = new HashMap<>();
            hourMap.put("today", dauHourTDMap);
            hourMap.put("yesterday", dauHourYDMap);

            return JSON.toJSONString(hourMap);
        } else if ("orderAmount".equals(id)) {
            // 今天
            Map<String, Double> orderAmountHourTDMap = publisherService.getOrderAmountHourMap(today);

            // 求昨天的分时明细
            String yesterday = getYesterday(today);
            Map<String, Double> orderAmountHourYDMap = publisherService.getOrderAmountHourMap(yesterday);

            Map<Object, Object> hourMap = new HashMap<>();
            hourMap.put("today", orderAmountHourTDMap);
            hourMap.put("yesterday", orderAmountHourYDMap);

            return JSON.toJSONString(hourMap);
        }
        return null;
    }

    private String getYesterday(String today) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = "";
        try {
            Date todayDate = simpleDateFormat.parse(today);
            Date yesterdayDate = DateUtils.addDays(todayDate, -1);
            yesterday = simpleDateFormat.format(yesterdayDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return yesterday;
    }

}
