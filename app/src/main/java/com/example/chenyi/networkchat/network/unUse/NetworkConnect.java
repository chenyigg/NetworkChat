package com.example.chenyi.networkchat.network.unUse;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.example.chenyi.networkchat.util.TransformUtil.byteMerger;
import static com.example.chenyi.networkchat.util.TransformUtil.lenToHead;
import static java.lang.Thread.sleep;

/**
 * TCP 连接的封装类，通过创建 socket 或 ServerSocket 获得 tcp 连接
 *
 * clients 是用于维护所有 tcp 连接的容器, server 是用于获取被动连接的 tcp 连接
 *
 * Created by chenyi on 2017/5/11.
 */

public abstract class NetworkConnect implements ChatClient.ReceiverListener {

    public static final int PORT = 3333;

    private Map<String, ChatClient> clients;
    private ServerSocket server;

    public NetworkConnect() throws IOException {
        clients = new HashMap<>();
        server = new ServerSocket(PORT);
    }

    public void sendString(String ip, String s, byte type) {
        byte[] data = s.getBytes();
        byte[] head = byteMerger(new byte[] {-1, type}, lenToHead(data.length));
        try {
            sendMessage(ip, byteMerger(head, data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String ip, File file, byte type) throws IOException {
        int len = 0;
        byte[] head;
        byte[] bytes = new byte[1030];
        FileInputStream fis = new FileInputStream(file);
        while (len != -1) {
            len = fis.read(bytes, 6, 1024);
            if (len != -1) {
                head = byteMerger(new byte[]{0, type}, lenToHead(len));
            } else {
                head = byteMerger(new byte[]{-1, type}, lenToHead(len));
            }
            sendMessage(ip, byteMerger(head, bytes));
        }
    }

    public void sendMessage(String ip, byte[] bytes) throws IOException {
        ChatClient client = clients.get(ip);
        if (client == null) {
            createClient(ip, bytes);
        } else {
            client.sendMessage(bytes);
            Log.i("客户端主动连接", "run: 连接成功 "+ip);
        }
    }

    private void createClient(final String ip, final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ChatClient client = new ChatClient(new Socket(ip, PORT));
                    clients.put(ip, client);
                    client.sendMessage(bytes);
                    Log.i("客户端主动连接", "run: 连接成功 "+ip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void serverStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = server.accept();
                        ChatClient client = new ChatClient(socket);
                        InetAddress connAddress = socket.getInetAddress();
                        Log.i("服务器被连接", "run: 连接成功"+connAddress);
                        clients.put(connAddress.getHostAddress(), client);
                        client.setListener(NetworkConnect.this);
                        client.receiveMessage();
                        sleep(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Object o : clients.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            ((ChatClient) entry.getValue()).closeClient();
        }
        clients.clear();
    }

    public void closeClient(String ip) {
        clients.get(ip).closeClient();
        clients.remove(ip);
    }
// tcp接收信息部分的代码
//    public boolean received(InputStream inputStream, String ip) {
//        byte[] flag = new byte[2];
//        byte[] head = new byte[4];
//        try {
//            inputStream.read(flag);
//            inputStream.read(head);
//            int len = ChangeByte2Int(head);
//            if (flag[0] == 100) {
//                socketClient.closeClient(ip);
//            } else if (flag[1] == 1) {
//                byte[] by = new byte[len];
//                inputStream.read(by);
//                Log.i("接收到信息", "received: "+new String(by, StandardCharsets.UTF_8));
//            } else if (flag[1] == 0) {
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
}
