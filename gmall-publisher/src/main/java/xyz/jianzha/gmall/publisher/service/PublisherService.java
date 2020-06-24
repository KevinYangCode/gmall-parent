package xyz.jianzha.gmall.publisher.service;

import java.util.Map;

/**
 * @author Y_Kevin
 * @date 2020-06-17 23:26
 */
public interface PublisherService {

    Integer getDauTotal(String date);

    Map<Object, Object> getDauHourMap(String date);

    Double getOrderAmount(String date);

    Map<String, Double> getOrderAmountHourMap(String date);
}
