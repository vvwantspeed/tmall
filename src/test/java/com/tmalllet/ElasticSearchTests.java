// package com.tmalllet;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONObject;
// import com.tmalllet.entity.User;
// import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
// import org.elasticsearch.action.bulk.BulkRequest;
// import org.elasticsearch.action.bulk.BulkResponse;
// import org.elasticsearch.action.delete.DeleteRequest;
// import org.elasticsearch.action.delete.DeleteResponse;
// import org.elasticsearch.action.get.GetRequest;
// import org.elasticsearch.action.get.GetResponse;
// import org.elasticsearch.action.index.IndexRequest;
// import org.elasticsearch.action.index.IndexResponse;
// import org.elasticsearch.action.search.SearchRequest;
// import org.elasticsearch.action.search.SearchResponse;
// import org.elasticsearch.action.support.master.AcknowledgedResponse;
// import org.elasticsearch.action.update.UpdateRequest;
// import org.elasticsearch.action.update.UpdateResponse;
// import org.elasticsearch.client.RequestOptions;
// import org.elasticsearch.client.RestHighLevelClient;
// import org.elasticsearch.client.indices.CreateIndexRequest;
// import org.elasticsearch.client.indices.CreateIndexResponse;
// import org.elasticsearch.client.indices.GetIndexRequest;
// import org.elasticsearch.common.xcontent.XContentType;
// import org.elasticsearch.core.TimeValue;
// import org.elasticsearch.index.query.MatchAllQueryBuilder;
// import org.elasticsearch.index.query.QueryBuilders;
// import org.elasticsearch.index.query.TermQueryBuilder;
// import org.elasticsearch.search.SearchHit;
// import org.elasticsearch.search.builder.SearchSourceBuilder;
// import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.concurrent.TimeUnit;
//
// @SpringBootTest
// class ElasticSearchTests {
//
//     @Autowired
//     @Qualifier("restHighLevelClient")
//     private RestHighLevelClient client;
//
//     // 创建索引 Request
//     @Test
//     void testCreateIndex() throws IOException {
//         // 创建索引请求
//         CreateIndexRequest request = new CreateIndexRequest("es_index");
//         // 客户端执行请求 IndicesClient
//         CreateIndexResponse createIndexResponse =
//                 client.indices().create(request, RequestOptions.DEFAULT);
//
//         System.out.println(createIndexResponse);
//     }
//
//     // 索引是否存在
//     @Test
//     void testExistIndex() throws IOException {
//         GetIndexRequest request = new GetIndexRequest("es_index");
//         boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
//         System.out.println(exists);
//     }
//
//     // 删除
//     @Test
//     void testDeleteIndex() throws  Exception {
//         DeleteIndexRequest request = new DeleteIndexRequest("es_index");
//         AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
//         System.out.println(delete.isAcknowledged());
//     }
//
//     // 添加文档
//     @Test
//     void testAddDocument() throws Exception {
//         User user = new User(3, "dd", "gg", "", 2, "");
//         IndexRequest request = new IndexRequest("es_index");
//         request.id("1");
//         request.timeout(TimeValue.timeValueSeconds(1));
//         request.timeout("1s");
//
//         // 将数据放入请求
//         request.source(JSON.toJSONString(user), XContentType.JSON);
//         // 客户端发送请求
//         IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
//
//         System.out.println(indexResponse.toString());
//         System.out.println(indexResponse.status());
//     }
//
//     @Test
//     void testIsExists() throws Exception {
//         GetRequest getRequest = new GetRequest("es_index", "1");
//         getRequest.fetchSourceContext(new FetchSourceContext(false));
//         getRequest.storedFields("_none_");
//
//         boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
//         System.out.println(exists);
//     }
//
//     @Test
//     void testGetDocument() throws Exception {
//         GetRequest getRequest = new GetRequest("es_index", "1");
//         GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
//         System.out.println(getResponse.getSourceAsString());    // 打印文档内容
//         System.out.println(getResponse);
//     }
//
//     @Test
//     void testUpdateDocument() throws Exception {
//         UpdateRequest updateRequest = new UpdateRequest("es_index", "1");
//         updateRequest.timeout("1s");
//         User user = new User(3, "mikoto", "gg", "", 2, "");
//         updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
//
//         UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
//         System.out.println(updateResponse);
//     }
//
//     @Test
//     void testDeleteDocument() throws Exception {
//         DeleteRequest deleteRequest = new DeleteRequest("es_index", "1");
//         deleteRequest.timeout("1s");
//
//         DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
//         System.out.println(deleteResponse);
//     }
//
//     // 批量
//     @Test
//     void testBulkRequest() throws IOException {
//         BulkRequest bulkRequest = new BulkRequest();
//         bulkRequest.timeout("1s");
//
//         List<User> users = new ArrayList<>();
//         users.add(new User(11, "mikot", "gg", "", 2, ""));
//         users.add(new User(111, "miko", "gg", "", 2, ""));
//         users.add(new User(1111, "mik", "gg", "", 2, ""));
//         users.add(new User(11111, "mi", "gg", "", 2, ""));
//
//         for (int i = 0; i < users.toArray().length; i++) {
//             bulkRequest.add(
//                     new IndexRequest("es_index")
//                     .id("" + (i + 1))  // 不写就是随机id
//                     .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
//         }
//
//         BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
//         System.out.println(bulkResponse.hasFailures());
//     }
//
//     @Test
//     void testSearch() throws IOException {
//         SearchRequest searchRequest = new SearchRequest("es_index");
//         SearchSourceBuilder builder = new SearchSourceBuilder();
//
//         TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "miko");
//         MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
//         builder.query(termQueryBuilder);
//
//         builder.from(0);
//         builder.size(10);
//         builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//
//         searchRequest.source(builder);
//
//         SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//
//         System.out.println(JSON.toJSONString(searchResponse.getHits()));
//         System.out.println("================================");
//         for (SearchHit hit : searchResponse.getHits().getHits()) {
//             System.out.println(hit);
//         }
//
//         List<User> users = new ArrayList<>();
//         for (SearchHit hit : searchResponse.getHits().getHits()) {
//             // System.out.println(hit.getSourceAsString());
//             // System.out.println(JSON.parse(hit.getSourceAsString()));
//             User user = JSONObject.parseObject(hit.getSourceAsString(), User.class);
//             System.out.println(user);
//             // User user = (User) JSON.parse(hit.getSourceAsString());
//             // users.add(user);
//         }
//         // System.out.println(users);
//     }
// }
