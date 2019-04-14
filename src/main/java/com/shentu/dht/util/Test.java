package com.shentu.dht.util;


import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by styb on 2019/3/28.
 */
public class Test {
    private  static RestHighLevelClient client=null;
    public static void main(String[] args) throws Exception{
         client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("39.98.247.199", 9200, "http")));
//        openIndex();
//        createIndex();
//            index(null);
        anylyze();
//        createIndex();
//        index(null);
//        search(null);
    }

    public static void index(Map<String, Object> m) throws Exception{
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("message", "我是真的好气哦是真的");
        IndexRequest indexRequest = new IndexRequest("twitter", "_doc", "1").opType("create")
                .source(jsonMap);
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    public static void createIndex() throws Exception{
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("message");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "ik_max_word");
                    builder.field("search_analyzer", "ik_max_word");

                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        CreateIndexRequest request = new CreateIndexRequest("twitter");
        request.mapping(builder);
        client.indices().create(request, RequestOptions.DEFAULT);

    }

    public static void get(Map<String, Object> m) throws Exception{
        GetRequest getRequest = new GetRequest(
                "haha",
                "doc",
                "2");
        String[] includes = new String[]{"message","user","*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        String index = getResponse.getIndex();
        String type = getResponse.getType();
        String id = getResponse.getId();
        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            System.out.println(sourceAsMap);
        } else {

        }
    }

    public static void search(Map<String, Object> m) throws Exception{

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.query(QueryBuilders.termQuery("message", "视频"));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("torrent");
        searchRequest.source(searchSourceBuilder);


        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        RestStatus status = searchResponse.status();
        TimeValue took = searchResponse.getTook();
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();
        boolean timedOut = searchResponse.isTimedOut();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.print(sourceAsMap+"===");
        }


    }

    public static void closeIndex() throws Exception{
        CloseIndexRequest request = new CloseIndexRequest("haha");

        AcknowledgedResponse closeIndexResponse = client.indices().close(request, RequestOptions.DEFAULT);
        System.out.println(closeIndexResponse.isAcknowledged());
    }

    public static void openIndex() throws Exception{
        OpenIndexRequest request = new OpenIndexRequest("haha");

        AcknowledgedResponse closeIndexResponse = client.indices().open(request, RequestOptions.DEFAULT);
        System.out.println(closeIndexResponse.isAcknowledged());
    }

    public static void anylyze() throws Exception{
        AnalyzeRequest request = new AnalyzeRequest();
        request.text("ReadMe.txt Screenshots,ReadMe.txt Screenshots,1.jpg COVER.jpg Screenshots,4.jpg Screenshots,2.jpg Screenshots,3.jpg FIFA.Street.2012 - RELOADED.rar");
        request.analyzer("ik_max_word");

        AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
        List<AnalyzeResponse.AnalyzeToken> tokens =
                response.getTokens();
        for(AnalyzeResponse.AnalyzeToken to:tokens){
            System.out.println(to.getTerm());
        }
        System.out.println(response.getTokens().get(0).getAttributes());
    }

    public static void deleteIndex() throws Exception{
        DeleteIndexRequest request = new DeleteIndexRequest("torrent");
        AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteIndexResponse.isAcknowledged());
    }


}


