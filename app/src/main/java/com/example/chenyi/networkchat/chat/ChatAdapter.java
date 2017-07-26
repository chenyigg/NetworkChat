package com.example.chenyi.networkchat.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.ChatMessage;
import com.example.chenyi.networkchat.bean.MyFile;
import com.example.chenyi.networkchat.bean.MyMessage;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.util.GildeUtil;
import com.example.chenyi.networkchat.util.GsonUtil;
import com.example.chenyi.networkchat.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenyi on 2017/5/20.
 */

public class ChatAdapter extends BaseAdapter {

    public static final int OUT_MESSAGE = 0;
    public static final int IN_MESSAGE = 1;
    public static final int OUT_FILE = 3;
    public static final int IN_FILE = 4;

    private User mUser;
    private User mFriend;
    private List<ChatMessage> mData;
    private LayoutInflater mInflater;

    public ChatAdapter(Context context, User user, User friend) {
        mData = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mUser = user;
        mFriend = friend;
    }

    public ChatAdapter(Context context, List<ChatMessage> data, User user, User friend) {
        mData = data;
        mInflater = LayoutInflater.from(context);
        mUser = user;
        mFriend = friend;
    }

    public List<ChatMessage> getmData() {
        return mData;
    }

    public void setmData(List<ChatMessage> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage cm = mData.get(position);
        if (cm.getData().getType() == MyMessage.CHAT_STRING) {
            return cm.getFrom().equals(mUser.getIp())?OUT_MESSAGE:IN_MESSAGE;
        } else {
            return cm.getFrom().equals(mUser.getIp())?OUT_FILE:IN_FILE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            switch (getItemViewType(position)) {
                // 通过 LayoutInflater 加载布局
                case OUT_MESSAGE:
                    convertView = mInflater.inflate(R.layout.item_chat_out, null);
                    holder = new MessageViewHolder(convertView);
                    break;
                case IN_MESSAGE:
                    convertView = mInflater.inflate(R.layout.item_chat_in, null);
                    holder = new MessageViewHolder(convertView);
                    break;
                case OUT_FILE:
                    convertView = mInflater.inflate(R.layout.item_chat_out_file, null);
                    holder = new FileViewHolder(convertView);
                    break;
                case IN_FILE:
                default:
                    convertView = mInflater.inflate(R.layout.item_chat_in_file, null);
                    holder = new FileViewHolder(convertView);
                    break;
            }
            convertView.setTag(holder);
        } else {
            // 通过 tag 找到缓存的布局
            holder = (ViewHolder) convertView.getTag();
        }
        switch (getItemViewType(position)) {
            case OUT_MESSAGE:
            case OUT_FILE:
                GildeUtil.setPicture(holder.pic, mUser.getPic());
                break;
            case IN_MESSAGE:
            case IN_FILE:
                GildeUtil.setPicture(holder.pic, mFriend.getPic());
                break;
        }
        switch (getItemViewType(position)) {
            case OUT_MESSAGE:
            case IN_MESSAGE:
                // 设置布局中控件要显示的视图
                ((MessageViewHolder)holder).message.setText((CharSequence) mData.get(position).getData().getHead());
                break;
            case OUT_FILE:
            case IN_FILE:
                final MyMessage myMessage = mData.get(position).getData();
                String s = GsonUtil.toJson(myMessage.getHead());
                final MyFile mf = GsonUtil.fromJson(s, MyFile.class);

                ImageView imageView = ((FileViewHolder)holder).image;
                imageView.setImageResource(R.drawable.defalute);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mf.isFinish()) {
                            gfListener.findFile();
                            ToastUtil.showToast("下载成功");
                        } else if (!mf.isFinish()){
                            gfListener.getFile(mFriend.getIp(), mf.getfPath());
                            mf.setFinish(true);
                            myMessage.setHead(mf);
                            String path = mf.getfPath();
                            String fName = path.trim();
                            String fileName = fName.substring(fName.lastIndexOf("/")+1);
                            ToastUtil.showToast(fileName+" 正在下载");
                        }
                    }
                });
                break;
        }

        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.pic)
        ImageView pic;

        ViewHolder(View itemView) {
            super();
            ButterKnife.bind(this, itemView);
        }
    }

    class MessageViewHolder extends ViewHolder {

        @BindView(R.id.message)
        TextView message;

        MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FileViewHolder extends ViewHolder {

        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.progress)
        ProgressBar progressBar;

        FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    GetFileListener gfListener;

    public void setGfListener(GetFileListener gfListener) {
        this.gfListener = gfListener;
    }

    interface GetFileListener {
        void findFile();
        void getFile(String ip, String path);
    }
}
