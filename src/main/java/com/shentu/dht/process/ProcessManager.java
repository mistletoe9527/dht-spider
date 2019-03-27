package com.shentu.dht.process;

import com.shentu.dht.process.dto.ProcessDto;
import lombok.SneakyThrows;

/**
 * Created by styb on 2019/3/15.
 */
public class ProcessManager extends AbstractProcessManager{

    @SneakyThrows
    @Override
    public void process(ProcessDto processDto){
        linkPorcessMethod(processDto.getMessageInfo())
                .invoke(linkPorcess(processDto.getMessageInfo()).get(), processDto);
    }
}
