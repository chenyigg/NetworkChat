package com.example.chenyi.networkchat.network;

import android.util.Log;

import com.example.chenyi.networkchat.bean.MyMessage;
import com.example.chenyi.networkchat.util.GsonUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * p2p 该广播的封装类，可使用多播将 iP 发送给其他设备
 *
 * 使用：创建 NetworkMulticast 类后，为其设置接收信息的处理监听器 MulticastListener ，
 * 通过 receive() 方法开启线程进行接收多播
 * Created by chenyi on 2017/5/10.
 */

public abstract class NetworkMulticast implements MulticastListener {

    // 使用该常量作为 app 的多播地址
    public static final String MULTICAST_IP = "230.0.0.1";
    // 使用该常量作为 app 的多播端口
    public static final int MULTICAST_PORT = 3333;
    // 定义每个数据报的大小为 1500
    public static final int DATA_LEN = 1500;
    // 定义 app 多播的 MulticastSocket、DatagramPacket、多播IP地址、字节数组
    private MulticastSocket socket = null;
    private InetAddress address = null;
    private byte[] bytes = new byte[DATA_LEN];
    private DatagramPacket outPacket;
    private DatagramPacket inPacket;

    private boolean isconn = false;

    public NetworkMulticast() throws IOException {
        super();
        isconn = true;
        // 初始化 app 多播的各种需要的信息
        socket = new MulticastSocket(MULTICAST_PORT);
        address = InetAddress.getByName(MULTICAST_IP);
        socket.joinGroup(address);
        // 设置本 MulticastSocket 发送的数据报不被回送到自身
        //socket.setLoopbackMode(true);
        // 初始化 多播 发送和接收的 DatagramPacket
        outPacket = new DatagramPacket(new byte[0], 0, address, MULTICAST_PORT);
        inPacket = new DatagramPacket(bytes, bytes.length);
        receive();
    }

    // 接收多播的线程
    public void receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isconn) {
                        // UDP 的 socket 接收数据报并对其进行处理
                        socket.receive(inPacket);

                        // 获取数据报的源ip 和其中携带的数据
                        String ip = inPacket.getAddress().getHostAddress();
                        byte[] buff = Arrays.copyOfRange(inPacket.getData(), inPacket.getOffset(),
                                inPacket.getLength() + inPacket.getOffset());
                        String result = new String(buff, StandardCharsets.UTF_8);
                        Log.i("receive", "run: "+ip+": "+result);
                        MyMessage message = GsonUtil.fromJson(result, MyMessage.class);

                        switch (message.getType()) {
                            case MyMessage.MULTICAST_ENTER:
                                // 接收多播信息的处理
                                hello(ip, message.getHead(), true);
                                break;
                            case MyMessage.DATAGRAM_REENTER:
                                // 返回自身的信息
                                hello(ip, message.getHead(), false);
                                break;
                            case MyMessage.MULTICAST_CHANGE:
                                // 接收信息修改的处理
                                change(ip, message.getHead());
                                break;
                            case MyMessage.DATAGRAM_CHANGE:
                                // 已接受修改后的信息
                                changeRespone(ip);
                                break;
                            case MyMessage.MULTICAST_LEAVE:
                                // 用户退出的多播处理
                                bye(ip);
                                break;
                            default:
                                // 通过该接口的方法对接收的自定义信息进行处理
                                resolved(ip, result);
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 发送多播的线程
    public void send(byte[] datas) {
        outPacket.setData(datas);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isconn) {
                        socket.send(outPacket);
                        Log.i("多播", "run: "+outPacket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 关闭多播的 socket
    public void close() {
        isconn = false;
        if (socket != null) {
            socket.close();
        }
    }
}
