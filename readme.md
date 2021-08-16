 
## restful
+ 资源名称用复数，而非单数。  
    使用 /categories 而不是用 /category
+ 如果是传递id,一般用 /  
    `get categories?start=1`
+ 如果是非id参数，一般用 ?  
    `delete categories/1`
  


2. CRUD 分别对应：
增加： post
删除： delete
修改： put
查询： get

3. id 参数的传递都用 /id方式。
如编辑和修改：
/categories/123

4. 其他参数采用?name=value的形式
如分页参数 /categories?start=5

5. 返回数据
查询多个返回 json 数组
增加，查询一个，修改 都返回当前 json 数组
删除 返回空

## redis
+ [Redis 5.0.10 for Windows](https://github.com/tporadowski/redis/releases)
+ [Another Redis Desktop Manager V 1.4.8](https://github.com/qishibo/AnotherRedisDesktopManager/releases)
+ [Try Redis](https://try.redis.io/)

## ElasticSearch
[ElasticSearch 7.14.0](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
+ 127.0.0.1:9200
[elasticsearch-head](https://github.com/mobz/elasticsearch-head)
+ http://localhost:9100
[Kibana]
+  http://localhost:5601
[ik分词器](https://github.com/medcl/elasticsearch-analysis-ik)