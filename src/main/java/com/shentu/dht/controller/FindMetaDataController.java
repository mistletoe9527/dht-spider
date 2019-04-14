package com.shentu.dht.controller;

import com.shentu.dht.client.ESClient;
import com.shentu.dht.process.dto.MetaData;
import com.shentu.dht.task.FindNodeTask;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by styb on 2019/4/4.
 */
@RestController
@RequestMapping("/metaData")
public class FindMetaDataController {

    @Autowired
    private ESClient esClient;

    @ResponseBody
    @RequestMapping("/findMetaData")
    public String findMetaData(String infoHash){
        FindNodeTask.queue.offer(infoHash);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/findTorrent")
    @SneakyThrows
    public Map<String,Object> findTorrent(String name){
        Map<String,Object> map=new HashMap<>();
        List<MetaData> ss = esClient.search(name);
        if(CollectionUtils.isNotEmpty(ss)){
            for(MetaData m:ss){
                map.put("magnet:?xt=urn:btih:"+m.getInfoHash(),m.getNameInfo());
            }
        }
        return map;
    }

}
