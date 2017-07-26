package com.example.chenyi.networkchat.network;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Thread.sleep;

/**
 * Created by chenyi on 2017/5/17.
 */

public abstract class NetworkDatagram implements DatagramListener{

    // 使用该常量作为 app 的发送信息的端口
    private static final int UDP_PORT = 4444;
    // 定义每个数据报的大小为 1500
    private static final int DATA_LEN = 1500;
    // 定义 app 发送信息的 DatagramSocket、DatagramPacket、字节数组
    private DatagramSocket socket = null;
    private byte[] bytes = new byte[DATA_LEN];
    private DatagramPacket outPacket;
    private DatagramPacket inPacket;

    private boolean isconn = false;

    // 需要发送的数据信息
    private Queue<Pair> datas;
    private boolean first = true;

    public NetworkDatagram() throws SocketException {
        super();
        isconn = true;
        // 初始化 app 的 udp socket 的各种需要的信息
        socket = new DatagramSocket(UDP_PORT);
        datas = new LinkedList<>();
        // 初始化信息发送和接收的 DatagramPacket
        outPacket = new DatagramPacket(new byte[0], 0);
        outPacket.setPort(UDP_PORT);
        inPacket = new DatagramPacket(bytes, bytes.length);
        receive();
    }

    // 接收多播的线程
    private void receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isconn) {
                        // UDP 的 socket 接收数据报并对其进行处理
                        socket.receive(inPacket);

                        byte[] buff = Arrays.copyOfRange(inPacket.getData(), inPacket.getOffset(),
                                inPacket.getLength() + inPacket.getOffset());
                        String result = new String(buff, StandardCharsets.UTF_8);
                        String from = inPacket.getAddress().getHostAddress();
                        Log.i("聊天接收", "run: "+inPacket.getAddress()+": "+result);
                        resolved(from, result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendOthers(String ip, int port, String data) {
        byte[] buffer = data.getBytes();
        final DatagramPacket dp;
        try {
            dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isconn) {
                        socket.send(dp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String ip, String message) {
        datas.offer(new Pair<>(ip, message));
        if (first) {
            send();
            first = false;
        }
    }

    // 发送信息的线程
    private void send() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isconn) {
                        if (!datas.isEmpty()) {
                            Pair pair = datas.poll();
                            outPacket.setAddress(InetAddress.getByName((String) pair.first));
                            outPacket.setData(((String) pair.second).getBytes());
                            Log.i("聊天发送", "sendMessage: "+pair.first+":"+pair.second);
                            socket.send(outPacket);
                        } else {
                            sleep(1000);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 关闭发送信息的 udpSocket
    public void close() {
        isconn = false;
        if (socket != null) {
            socket.close();
        }
    }
}
