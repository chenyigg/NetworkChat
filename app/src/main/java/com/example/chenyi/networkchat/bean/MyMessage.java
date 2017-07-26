package com.example.chenyi.networkchat.bean;

/**
 * Created by chenyi on 2017/5/23.
 */

public class MyMessage<T> {
    public static final int MULTICAST_ENTER = 1;
    public static final int MULTICAST_CHANGE = -1;
    public static final int MULTICAST_LEAVE = 2;
    public static final int DATAGRAM_REENTER = 3;
    public static final int DATAGRAM_CHANGE = -3;

    public static final int CHAT_STRING = 5;
    public static final int CHAT_FILE = 6;
    public static final int GET_FILE = 7;

    private int type;
    private T head;

    public MyMessage(int type1) {
        super();
        type = type1;
        head = null;
    }

    public MyMessage(int type1, T head1) {
        super();
        type = type1;
        head = head1;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getHead() {
        return head;
    }

    public void setHead(T head) {
        this.head = head;
    }
}
