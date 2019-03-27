package com.shentu.dht.process;

/**
 * Created by styb on 2019/3/15.
 */
public interface DHTProcess<T> {

    //主动处理
    void activeProcess(T t);

    //被动处理
    void passiveProcess(T t);

}
