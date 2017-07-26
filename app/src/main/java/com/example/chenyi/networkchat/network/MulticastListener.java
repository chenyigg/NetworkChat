package com.example.chenyi.networkchat.network;

/**
 * Created by chenyi on 2017/5/17.
 */

public interface MulticastListener {
    void hello(String ip, Object result, boolean response);

    void change(String ip, Object result);

    void changeRespone(String ip);

    void bye(String ip);

    void resolved(String ip, String s);
}
