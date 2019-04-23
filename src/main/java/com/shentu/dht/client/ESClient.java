package com.shentu.dht.client;

import com.shentu.dht.config.Config;
import com.shentu.dht.process.dto.MetaData;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by styb on 2019/4/12.
 */
@Component
public class ESClient implements InitializingBean{

    @Autowired
    private Config config;
    private  RestHighLevelClient client=null;

    public  void createIndex() throws Exception{
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("name");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "ik_max_word");
                    builder.field("search_analyzer", "ik_max_word");

                }
                builder.endObject();

                builder.startObject("infoHash");
                {
                    builder.field("type", "keyword");

                }
                builder.endObject();

                builder.startObject("length");
                {
                    builder.field("type", "long");

                }
                builder.endObject();

                builder.startObject("nameInfo");
                {
                    builder.field("type", "text");

                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        CreateIndexRequest request = new CreateIndexRequest("torrent");
        request.mapping(builder);
        client.indices().create(request, RequestOptions.DEFAULT);

    }

    public  void index(MetaData metaData) throws Exception{
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", metaData.getName());
        jsonMap.put("infoHash", metaData.getInfoHash());
        jsonMap.put("length", metaData.getLength());
        jsonMap.put("nameInfo",metaData.getNameInfo());
        IndexRequest indexRequest = new IndexRequest("torrent", "_doc",String.valueOf(System.currentTimeMillis())+new Random().nextInt(1000000)).opType("create")
                .source(jsonMap);
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    public List<MetaData> search(String searchValue) throws Exception{
        List<MetaData> list=new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", searchValue));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("torrent");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            list.add(new MetaData((String)sourceAsMap.get("infoHash"),
                    Long.parseLong(String.valueOf(sourceAsMap.get("length"))),
                    (String)sourceAsMap.get("name"),(String)sourceAsMap.get("nameInfo")));
        }
        return list;
    }

    public static void main(String[] args) throws Exception{
//        ESClient.esClient.createIndex();
//        List<MetaData> ss = ESClient.esClient.search("SIS001影视联盟");
//        System.out.println(JSONObject.toJSONString(ss));
//        JSONArray ja=(JSONArray) JSONArray.parse(ss.get(0).getNameInfo());
//        ja.stream().forEach(v->{
//            System.out.println(v);
//        });
//

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(config.getEsHost(), config.getEsPort(), "http")));
    }


}
