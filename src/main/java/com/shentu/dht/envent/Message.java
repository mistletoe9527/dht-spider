package com.shentu.dht.envent;

import java.util.EventObject;

/**
 * Created by styb on 2019/3/13.
 */
public class Message extends EventObject{


    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public Message(Object source) {
        super(source);
    }
}
