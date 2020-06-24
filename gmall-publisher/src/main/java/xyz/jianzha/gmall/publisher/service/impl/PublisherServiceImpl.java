package xyz.jianzha.gmall.publisher.service.impl;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.jianzha.gmall.common.constant.GmallConstant;
import xyz.jianzha.gmall.publisher.service.PublisherService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Y_Kevin
 * @date 2020-06-17 23:27
 */
@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    JestClient jestClient;

    @Override
    public Integer getDauTotal(String date) {
        String query;
//        query = "{\n" +
//                "  \"query\": {\n" +
//                "    \"bool\": {\n" +
//                "      \"filter\": {\n" +
//                "        \"term\": {\n" +
//                "          \"logDate\": \"2020-06-17\"\n" +
//                "        }\n" +
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("logDate", date));
        searchSourceBuilder.query(boolQueryBuilder);
        query = searchSourceBuilder.toString();

        System.out.println(query);

        Search search = new Search.Builder(query).addIndex(GmallConstant.ES_INDEX_DAU).addType("_doc").build();
        Integer total = 0;
        try {
            SearchResult searchResult = jestClient.execute(search);
            total = searchResult.getTotal();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }

    @Override
    public Map<Object, Object> getDauHourMap(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("logDate", date));
        searchSourceBuilder.query(boolQueryBuilder);
        // 聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_logHour").field("logHour").size(24);
        searchSourceBuilder.aggregation(aggsBuilder);
        String query = searchSourceBuilder.toString();

        Search search = new Search.Builder(query).addIndex(GmallConstant.ES_INDEX_DAU).addType("_doc").build();

        Map<Object, Object> dauHourMap = new HashMap<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_logHour").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                Long value = bucket.getCount();
                dauHourMap.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dauHourMap;
    }

    @Override
    public Double getOrderAmount(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));
        searchSourceBuilder.query(boolQueryBuilder);

        // 聚合
        SumBuilder aggsBuilder = AggregationBuilders.sum("sum_totalAmount").field("totalAmount");
        searchSourceBuilder.aggregation(aggsBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstant.ES_INDEX_ORDER).addType("_doc").build();
        Double sumTotalAmount = 0D;
        try {
            SearchResult searchResult = jestClient.execute(search);
            sumTotalAmount = searchResult.getAggregations().getSumAggregation("sum_totalAmount").getSum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sumTotalAmount;
    }

    @Override
    public Map<String, Double> getOrderAmountHourMap(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));
        searchSourceBuilder.query(boolQueryBuilder);

        // 聚合
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_createHour").field("createHour").size(24);
        SumBuilder sumBuilder = AggregationBuilders.sum("sum_totalAmount").field("totalAmount");

        // 子聚合
        termsBuilder.subAggregation(sumBuilder);
        searchSourceBuilder.aggregation(termsBuilder);

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(GmallConstant.ES_INDEX_ORDER).addType("_doc").build();

        Map<String, Double> hourMap = new HashMap<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_createHour").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String hourKey = bucket.getKey();
                Double hourAmount = bucket.getSumAggregation("sum_totalAmount").getSum();
                hourMap.put(hourKey, hourAmount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hourMap;
    }
}
