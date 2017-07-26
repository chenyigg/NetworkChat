package com.example.chenyi.networkchat.network;

import android.os.Environment;
import android.util.Log;

import com.example.chenyi.networkchat.util.GsonUtil;
import com.example.chenyi.networkchat.util.TransformUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * Created by chenyi on 2017/5/23.
 */

public abstract class FileSocket implements FileListener {

    private static final int PORT = 33333;
    private static final String FILE_PATH = Environment.getExternalStorageDirectory()
            .getPath() +"/NC/file/";
    private static final String IMAGE_PATH = Environment.getExternalStorageDirectory()
            .getPath() +"/NC/image/";

    private String description;
    private String path;

    public FileSocket(String head, String spath) throws IOException {
        description = head;
        path = spath;

        File dir = new File(FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File image = new File(IMAGE_PATH);
        if (!image.exists()) {
            image.mkdirs();
        }
        Log.i("创建文件", "FileSocket: "+dir);
        receive();
    }

    public void send(final String host, final String descrip, final String p, final boolean response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendFile(host, descrip, p, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendFile(String host, String description, String path, boolean response) throws IOException {

        //首先, 客户端与服务端建立连接,其实就相当于显式调用connect(SocketAddress endpoint)方法
        Socket client = new Socket(host, PORT);

        OutputStream output = client.getOutputStream();
        InputStream input = client.getInputStream();

        writeHead(description, output);
        writeFile(path, output);
        output.flush();
        // 关闭输出流,这里必须关闭输出流，read()方法才能返回-1,否则,就会造成死锁
        client.shutdownOutput();

        if (response) {

            String head = readHead(input);

            Map<String, String> result = GsonUtil.toMap(head, String.class);
            if (result != null) {
                String type = result.get("type");
                String fileName = result.get("file_name");
                String rpath = readFile(input, fileName, false);

                if ("pic".equals(type)) {
                    getFile(host, rpath);
                }
                else {
                    getPic(host, rpath);
                }
            }

        }

        output.close();
        input.close();
        client.close();
    }
    private void receive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    receiveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void receiveFile() throws IOException {
        Socket client;
        OutputStream output;
        InputStream input;
        ByteArrayOutputStream byteOutput;
        //服务端在指定的端口监听客户端的连接请求
        ServerSocket server = new ServerSocket(PORT);
        while (true) {

            client = server.accept();
            output = client.getOutputStream();
            input = client.getInputStream();

            String ip = client.getInetAddress().getHostAddress();

            String head = readHead(input);

            Map<String, String> result = GsonUtil.toMap(head, String.class);
            if (result != null) {
                String type = result.get("type");
                String fileName = result.get("file_name");
                String rpath = null;
                if (type.equals("file")) {
                    rpath = readFile(input, fileName, true);
                } else {
                    rpath = readFile(input, fileName, false);
                }

                switch (type) {
                    case "pic":
                        if (!path.equals("null")) {
                            writeHead(description, output);
                            writeFile(path, output);
                        }
                        output.flush();
                        // 关闭输出流,这里必须关闭输出流，read()方法才能返回-1,否则,就会造成死锁
                        client.shutdownOutput();
                    case "cPic":
                        getPic(ip, rpath);
                        break;
                    default:
                        getFile(ip, rpath);
                        break;
                }
            }

            input.close();
            output.close();
            client.close();
        }
    }

    private void writeHead(String head, OutputStream os) throws IOException {
        byte[] hbyte = head.getBytes();
        os.write(TransformUtil.lenToHead(hbyte.length));
        os.write(hbyte);
    }
    private void writeFile(String path, OutputStream os) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        Log.i("send", "writeFile: "+path);
        // 输出文件内容
        byte[] buffer = new byte[4096];
        int size;
        while ((size = fis.read(buffer)) != -1) {
            os.write(buffer, 0, size);
        }
    }

    private String readHead(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        int len = TransformUtil.byte2Int(bytes);
        byte[] buffer = new byte[len];
        is.read(buffer);
        return new String(buffer);
    }

    private String readFile(InputStream is, String name, boolean isFile) throws IOException {
        String path;
        if (isFile) {
            path = FILE_PATH + name;
        } else {
            path = IMAGE_PATH + name;
        }
        FileOutputStream fos = new FileOutputStream(path);
        Log.i("get", "readFile: " + path);
        // 输出文件内容
        byte[] buffer = new byte[4096];
        int size;
        while ((size = is.read(buffer)) != -1) {
            fos.write(buffer, 0, size);
        }
        return path;
    }
}
