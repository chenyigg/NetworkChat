package com.example.chenyi.networkchat.bean;

import com.example.chenyi.networkchat.util.TimeUtil;

/**
 * Created by chenyi on 2017/5/20.
 */

public class ChatMessage<T> {

    private long time;
    private String from;
    private String to;
    private MyMessage<T> data;

    public ChatMessage() {
        super();
        time = 0;
        from = "127.0.0.1";
        to = "127.0.0.1";
        data = new MyMessage<>(MyMessage.CHAT_STRING);
    }

    public ChatMessage(long time1, String from1, String to1, MyMessage data1) {
        super();
        time = time1;
        from = from1;
        to = to1;
        data = data1;
    }

    public ChatMessage(String from1, String to1, MyMessage data1) {
        super();
        time = TimeUtil.getCurrentTime();
        from = from1;
        to = to1;
        data = data1;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public MyMessage getData() {
        return data;
    }

    public void setData(MyMessage<T> data) {
        this.data = data;
    }

}
