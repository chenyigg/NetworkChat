package com.example.chenyi.networkchat.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.ChatMessage;
import com.example.chenyi.networkchat.bean.Friends;
import com.example.chenyi.networkchat.bean.MyFile;
import com.example.chenyi.networkchat.bean.MyMessage;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.chat.ChatActivity;
import com.example.chenyi.networkchat.network.FileSocket;
import com.example.chenyi.networkchat.network.NetworkDatagram;
import com.example.chenyi.networkchat.network.NetworkMulticast;
import com.example.chenyi.networkchat.util.GsonUtil;
import com.example.chenyi.networkchat.util.IpUtil;
import com.example.chenyi.networkchat.util.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyi on 2017/5/9.
 */

public class ChatService extends Service {

    private NetworkMulticast multicast = null;
    private NetworkDatagram client = null;
    private FileSocket fileSocket = null;

    public Friends friends;
    public List<ChatMessage> messages;
    User me;

    private ServiceListener1 listener1;

    public void setListener1(ServiceListener1 listener) {
        this.listener1 = listener;
    }

    public void setUser(User user) {
        this.me = user;
    }

    public interface ServiceListener1 {
        void updateFriends(Friends friends);
    }

    private String currentFriend;
    private ReceiveListener2 listener2;

    public void setListener2(ReceiveListener2 listener, String friend) {
        this.listener2 = listener;
        currentFriend = friend;
    }

    public interface ReceiveListener2 {
        void receiveMessage(List<ChatMessage> messages);

        void receiveFile(String ip, String path);
    }

    private final IBinder mBinder = new ChatBinder();

    public class ChatBinder extends Binder {
        public ChatService getService() {
            // 返回服务的实例
            return ChatService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        friends = new Friends();
        messages = new ArrayList<>();

        try {
            initMulticast();
            initDatagram();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "网络连接错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        MyMessage m = new MyMessage(MyMessage.MULTICAST_LEAVE);
        String leave = GsonUtil.toJson(m);
        multicastMessage(leave);
        multicast.close();
        client.close();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (fileSocket == null) {
            me = intent.getParcelableExtra("user");
            try {
                fileSocket = new FileSocket(getHeads("pic"), getPicPath()) {
                    @Override
                    public void getPic(String ip, String path) {
                        friends.updateFriendPic(ip, path);
                        listener1.updateFriends(friends);
                        Log.i("file", "getPic: " + ip + ":" + path);
                    }

                    @Override
                    public void getFile(String ip, String path) {

                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mBinder;
    }

    public void multicastMessage(String s) {
        multicast.send(s.getBytes());
    }

    public void sendString(String ip, String s) {
        MyMessage<String> message = new MyMessage<>(MyMessage.CHAT_STRING, s);
        if (!ip.equals(me.getIp())) {
            messages.add(new ChatMessage(IpUtil.getIp(), ip, message));
            listener2.receiveMessage(messages);
        }
        client.sendMessage(ip, GsonUtil.toJson(message));
    }

    public void sendFilePath(String ip, MyFile path) {
        MyMessage<MyFile> message = new MyMessage<>(MyMessage.CHAT_FILE, path);
        if (!ip.equals(me.getIp())) {
            messages.add(new ChatMessage(IpUtil.getIp(), ip, message));
            listener2.receiveMessage(messages);
        }
        client.sendMessage(ip, GsonUtil.toJson(message));
    }

    public void getFile(String ip, String path) {
        MyMessage<String> message = new MyMessage<>(MyMessage.GET_FILE, path);
        client.sendMessage(ip, GsonUtil.toJson(message));
    }

    public void sendFile(String host, String description, String path, boolean response) {
        try {
            fileSocket.sendFile(host, description, path, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHeads(String type) {

        Map<String, String> head = new HashMap<>();
        head.put("type", type);
        if (me != null && me.getIp() != null) {
            head.put("file_name", me.getIp() + "_" + TimeUtil.getCurrentTime() + ".jpg");
        } else  {
            head.put("file_name", "default.jpg");
        }

        return GsonUtil.toJson(head);
    }

    public String getHeads(String type, String name) {

        Map<String, String> head = new HashMap<>();
        head.put("type", type);
        head.put("file_name", name);

        return GsonUtil.toJson(head);
    }

    public String getPicPath() {
        return me != null && me.getPic() != null ? me.getPic() : "null";
    }

    private void initMulticast() throws IOException {
        // 配置多播的socket，使 socket 处于异步接收多播的状态
        multicast = new NetworkMulticast() {
            @Override
            public void hello(String ip, Object result, boolean response) {
                if (response) {
                    /**
                     * 接收到用户的多播信息后需要进行单播响应
                     */
                    MyMessage<User> m = new MyMessage<>(MyMessage.DATAGRAM_REENTER, me);
                    String rp = GsonUtil.toJson(m);
                    client.sendOthers(ip, NetworkMulticast.MULTICAST_PORT, rp);

                    if (!getPicPath().equals("null")) {
                        sendFile(ip, getHeads("pic"), getPicPath(), true);
                    }
                }
                Log.i("enter", "hello: " + ip);
                changeFriends(ip, result);
            }

            @Override
            public void change(String ip, Object result) {
                /**
                 * 接收信息修改后单播响应，请求接收图片的修改
                 */
                Log.i("info", "change: " + ip);
                MyMessage<String> m = new MyMessage<>(MyMessage.DATAGRAM_CHANGE);
                String cp = GsonUtil.toJson(m);
                client.sendOthers(ip, NetworkMulticast.MULTICAST_PORT, cp);
                changeFriends(ip,  result);
            }

            @Override
            public void changeRespone(String ip) {
                /**
                 * 接收到对方回复的响应后将文件发送给对方
                 */
                Log.i("pic", "changeRespone: " + ip);
                if (!getPicPath().equals("null")) {
                    sendFile(ip, getHeads("cPic"), getPicPath(), false);
                }
            }

            @Override
            public void bye(String ip) {
                friends.deleteFriend(ip);
                listener1.updateFriends(friends);
            }

            @Override
            public void resolved(String ip, String s) {
                // TODO: 2017/5/15
            }
        };
    }

    private void initDatagram() throws IOException {
        // 配置 p2p 的客户端，用于发送信息和接收信息
        client = new NetworkDatagram() {
            @Override
            public void resolved(String from, String data) {
                MyMessage message = GsonUtil.fromJson(data, MyMessage.class);
                if (message.getType() == MyMessage.GET_FILE) {
                    String path = (String) message.getHead();
                    String fName = path.trim();
                    String fileName = fName.substring(fName.lastIndexOf("/")+1);

                    sendFile(from, getHeads("file", fileName), path, false);
                } else {
                    ChatMessage result = new ChatMessage(from, IpUtil.getIp(), message);
                    messages.add(result);
                    if (listener2 != null && currentFriend.equals(from)) {
                        listener2.receiveMessage(messages);
                    } else {
                        if (message.getHead() instanceof String) {
                            bulidNotification(from, (String) message.getHead());
                        } else {
                            bulidNotification(from, "给你发送了文件");

                        }
                    }
                }
            }
        };
    }

    private void changeFriends(String ip, Object result) {
        String data = GsonUtil.toJson(result);
        User friend = GsonUtil.fromJson(data, User.class);
        if (friend == null) {
            friend = new User();
        }
        friend.setIp(ip);
        friends.newFriend(friend);
        listener1.updateFriends(friends);
        // TODO: 2017/6/1
    }

    private void bulidNotification(String ip, String text) {
        Log.i("notification", "bulidNotification: 收到信息"+ip+text);
        User friend = friends.getFriend(ip);
        // 在API11之后构建Notification的方式
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, ChatActivity.class);
        nfIntent.putExtra("friend", friend);
        nfIntent.putExtra("user", me);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, nfIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = builder.setContentIntent(pIntent) // 设置PendingIntent
        .setSmallIcon(R.drawable.sms) // 设置下拉列表中的图标
        .setContentTitle(friend.getUserName()) // 设置下拉列表里的标题
        .setContentText(text) // 设置上下文内容
        .setWhen(TimeUtil.getCurrentTime())
        .setAutoCancel(true)
        .build(); // 设置该通知发生的时间
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, notification);
    }
}
