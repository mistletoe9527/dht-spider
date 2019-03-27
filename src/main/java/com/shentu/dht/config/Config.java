package com.shentu.dht.config;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by styb on 2019/3/12.
 */
@Component
@Accessors(chain = true)
@ConfigurationProperties(prefix = "config")
@Data
@Slf4j
@Validated
public class Config {


    @Valid
    private Address address=new Address();

    private String token="styb";

    private Integer port;//监听端口
    @Data
    public static class Address{


        private List<String> superAddressList=new ArrayList<>();
    }

}
