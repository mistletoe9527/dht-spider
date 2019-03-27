package com.shentu.dht.process;

import com.alibaba.fastjson.JSONObject;
import com.shentu.dht.enmu.MessageTypeEnmu;
import com.shentu.dht.process.constant.MethodConstant;
import com.shentu.dht.process.dto.MessageInfo;
import com.shentu.dht.process.dto.ProcessDto;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created by styb on 2019/3/15.
 */
public abstract class AbstractProcessManager implements ApplicationContextAware{

    private ApplicationContext applicationContext;


    protected abstract void  process(ProcessDto processDto) throws Exception;
    
    public Optional<DHTProcess> linkPorcess(MessageInfo messageInfo){
        return Optional.ofNullable(applicationContext.getBean(messageInfo.getMethod().getKey(), DHTProcess.class));
    }

    public Optional<Class> linkPorcessClass(MessageInfo messageInfo){
        return Optional.ofNullable(applicationContext.getBean(messageInfo.getMethod().getKey(), DHTProcess.class).getClass());
    }

    public Method linkPorcessMethod(MessageInfo messageInfo){
        return ReflectionUtils.findMethod(
                linkPorcessClass(messageInfo).orElseThrow(()->new RuntimeException("process is null " + JSONObject.toJSONString(messageInfo.getMethod()))),
                linkProcessMethodName(messageInfo.getStatus()),
                ProcessDto.class);
    }

    public String linkProcessMethodName(MessageTypeEnmu messageTypeEnmu){
        if(MessageTypeEnmu.QUERY.getKey().equals(messageTypeEnmu.getKey())){
           return MethodConstant.PASSIVE_PROCESS;
        }else if(MessageTypeEnmu.RESPONSE.getKey().equals(messageTypeEnmu.getKey())){
            return MethodConstant.ACTIVE_PROCESS;
        }else{
            throw new RuntimeException("miss linkProcessMethodName messageTypeEnmu="+messageTypeEnmu);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

}
