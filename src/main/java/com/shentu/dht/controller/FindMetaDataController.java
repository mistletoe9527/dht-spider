package com.shentu.dht.controller;

import com.shentu.dht.task.FindNodeTask;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by styb on 2019/4/4.
 */
@RestController
@RequestMapping("/metaData")
public class FindMetaDataController {

    @ResponseBody
    @RequestMapping("/findMetaData")
    public String findMetaData(String infoHash){
        FindNodeTask.queue.offer(infoHash);
        return "success";
    }

}
