package com.example.chenyi.networkchat.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.ChatMessage;
import com.example.chenyi.networkchat.bean.MyFile;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.main.ChatService;
import com.example.chenyi.networkchat.mvp.MVPBaseActivity;
import com.example.chenyi.networkchat.person.PersonActivity;
import com.example.chenyi.networkchat.util.InputModeUtil;
import com.example.chenyi.networkchat.util.TransformUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenyi on 2017/5/18.
 */

public class ChatActivity extends MVPBaseActivity<ChatContract.View, ChatPresenter> implements ChatContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.name)
    TextView title;
    @BindView(R.id.listView)
    ListView listView;
    ChatAdapter adapter;
    @BindView(R.id.edit)
    EditText edit;
    @BindView(R.id.send)
    Button send;
    @OnClick(R.id.send)
    public void send() {
        String s = edit.getText().toString();
        if (mBound && !s.equals("")) {
            mService.sendString(friend.getIp(), s);
            edit.setText("");
        }
    }
    @OnClick(R.id.add)
    public void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。

        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//        Intent intent = new Intent(Media.RECORD_SOUND_ACTION);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1 :
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = TransformUtil.uri2Path(uri);
                    mService.sendFilePath(friend.getIp(), new MyFile(path));
                }
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private User friend;
    private User user;

    ChatService mService;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // 服务绑定成功并获取服务的实例
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.i("connect", "onServiceConnected: success");
            adapter.setmData(getChatMessage(mService.messages));
            handler.sendEmptyMessage(1);

            mService.setListener2(new ChatService.ReceiveListener2() {
                @Override
                public void receiveMessage(List<ChatMessage> ms) {
                    adapter.setmData(getChatMessage(ms));
                    handler.sendEmptyMessage(1);
                }

                @Override
                public void receiveFile(String ip, String path) {

                }
            }, friend.getIp());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public List<ChatMessage> getChatMessage(List<ChatMessage> ms) {
        List<ChatMessage> cm = new ArrayList<>();
        for (int i=0; i<ms.size(); i++) {
            if (friend.getIp().equals(ms.get(i).getFrom()) ||
                    friend.getIp().equals(ms.get(i).getTo())) {
                cm.add(ms.get(i));
            }
        }
        return cm;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        friend = intent.getParcelableExtra("friend");
        user = intent.getParcelableExtra("user");
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            Intent intent = new Intent(this, ChatService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            mService.setListener2(null, null);
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void initView () {
        ButterKnife.bind(this);
        InputModeUtil.assistActivity(this);
        setupToolbar();
        setupListView();
    }

    private void setupToolbar() {
        title.setText(friend.getUserName());
        toolbar.setNavigationIcon(R.drawable.arrows);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //设置toolbar菜单的监听
        toolbar.inflateMenu(R.menu.menu_chat);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.info:
                        Intent intent = new Intent(ChatActivity.this, PersonActivity.class);
                        intent.putExtra("user", friend);
                        intent.putExtra("type", PersonActivity.UNREVISABILITY);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    private void setupListView() {
        adapter = new ChatAdapter(this, user, friend);
        adapter.setGfListener(new ChatAdapter.GetFileListener() {
            @Override
            public void findFile() {
                File file = new File(Environment.getExternalStorageDirectory()
                        .getPath() +"/NC/file/");
                if(null == file || !file.exists()){
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "*/*");

                startActivity(intent);
            }

            @Override
            public void getFile(String ip, String path) {
                mService.getFile(ip, path);
            }
        });
        listView.setAdapter(adapter);
    }


    @Override
    public void showEmpty() {

    }

    @Override
    public void showError() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
}
