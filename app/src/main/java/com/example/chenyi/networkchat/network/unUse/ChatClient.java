package com.example.chenyi.networkchat.network.unUse;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * Created by chenyi on 2017/5/13.
 */

public class ChatClient {

    private Socket client = null;
    private SendThread send = null;
    // 判断 socket 是否持续连接
    private boolean isConn = true;

    public static final byte CLOSE = 100;

    private ReceiverListener listener;

    public interface ReceiverListener {
        boolean received(InputStream inputStream, String ip);
    }

    public void setListener(ReceiverListener listener) {
        this.listener = listener;
    }

    public ChatClient(Socket socket) {
        super();
        client = socket;
    }

    public void sendMessage(byte[] b) {
        try {
            if (send == null) {
                send = new SendThread();
                send.start();
            }
            send.setBytes(b);
        } catch (IOException e) {
            e.printStackTrace();
            isConn = false;
        }
    }

    public void receiveMessage() {
        try {
            new ReceiveThread().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeClient() {
        isConn = false;
    }

    private class SendThread extends Thread {
        OutputStream os;
        // 需要发送的数据信息
        Queue<byte[]> datas;

        SendThread() throws IOException {
            super();
            os = client.getOutputStream();
            datas = new LinkedList<>();
        }

        @Override
        public void run() {
            try {
                // socket 连接时，线程不断询问信息并发送
                while (isConn) {
                    if (!datas.isEmpty()) {
                        os.write(datas.poll());
                        os.flush();
                        Log.i("socket", "sendMessage: 信息开始发送");
                    } else {
                        sleep(1000);
                    }
                }
                os.write(new byte[]{CLOSE, 0, 0, 0, 0, 0});
                client.shutdownOutput();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                isConn = false;
            } finally {
                try {
                    os.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 线程开启后，将信息设置给 bytes 即可发送
        void setBytes(byte[] bytes) throws IOException {
            datas.offer(bytes);
        }
    }

    private class ReceiveThread extends Thread {
        InputStream is;
        // 接收数据的缓冲区
        byte[] buffer = new byte[2053];
        int len = buffer.length;
        // 报文包的真实长度
        int dataLen = 0;

        ReceiveThread() throws IOException {
            super();
            is = client.getInputStream();
        }

        @Override
        public void run() {
            try {
                while (isConn) {
                    // TODO: 将这个退出可以做得更好 2017/5/13
                    if (listener.received(is, client.getLocalAddress().getHostAddress()))
                        break;
                }
                // 对方已经退出连接，无法再向它发送信息了
                client.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
                isConn = false;
            } finally {
                try {
                    is.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
