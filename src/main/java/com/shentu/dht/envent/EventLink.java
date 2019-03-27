package com.shentu.dht.envent;

import java.util.EventObject;

/**
 * Created by styb on 2019/3/13.
 */
public interface EventLink<T extends EventObject> {

    void onFindNode(T t);
}
