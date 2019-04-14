package com.shentu.dht.process.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/4/12.
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class MetaData {


    private String infoHash;

    private Long length;

    private String name;


    private String nameInfo;





}
