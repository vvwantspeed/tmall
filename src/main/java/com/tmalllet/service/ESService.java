package com.tmalllet.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tmalllet.entity.Product;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ESService {
    @Autowired
    @Qualifier("restHighLevelClient")
    RestHighLevelClient client;

    @Autowired
    ProductService productService;

    public final static String ES_INDEX = "es_index";

    // 增加一条数据
    public void add(Product product) throws IOException {
        IndexRequest request = new IndexRequest(ES_INDEX);
        request.source(JSON.toJSONString(product), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    // 删除
    public void delete(int id) throws Exception {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(ES_INDEX);
        TermQueryBuilder query = QueryBuilders.termQuery("id", id);
        deleteByQueryRequest.setQuery(query);
        client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }

    // 更新
    public void update(Product product) throws Exception {
        delete(product.getId());
        add(product);
    }

    // 初始化
    public void init() throws Exception {
        SearchRequest searchRequest = new SearchRequest(ES_INDEX);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        builder.query(matchAllQueryBuilder);
        searchRequest.source(builder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (0 == searchResponse.getHits().getTotalHits().value) {
            List<Product> products = productService.list();
            bulkAdd(products);
        }
    }

    // 批量增加
    private void bulkAdd(List<Product> products) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("1s");

        for (int i = 0; i < products.toArray().length; i++) {
            bulkRequest.add(
                    new IndexRequest("es_index")
                            .source(JSON.toJSONString(products.get(i)), XContentType.JSON));
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures());
    }

    public List<Product> searchByName(String keyword, Integer start, Integer size) throws IOException {
        SearchRequest searchRequest = new SearchRequest("es_index");
        SearchSourceBuilder builder = new SearchSourceBuilder();

        MatchQueryBuilder termQueryBuilder = QueryBuilders.matchQuery("name", keyword);
        builder.query(termQueryBuilder);

        builder.from(start);
        builder.size(size);
        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(builder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Product> products = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Product product = JSONObject.parseObject(hit.getSourceAsString(), Product.class);
            products.add(product);
        }
        return products;
    }
}
