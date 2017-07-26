package com.example.chenyi.networkchat.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.util.GildeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 好友列表的adapter
 * Created by chenyi on 2017/5/3.
 */

public class FriendsAdapter extends BaseAdapter {

    private List<User> mData;
    private LayoutInflater mInflater;

    public FriendsAdapter(Context context) {
        mData = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    public FriendsAdapter(Context context, List<User> data) {
        mData = data;
        mInflater = LayoutInflater.from(context);
    }

    public List<User> getmData() {
        return mData;
    }

    public void setmData(List<User> data) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            // 通过 LayoutInflater 加载布局
            convertView = mInflater.inflate(R.layout.item_friends, null);
            holder = new ViewHolder(convertView);
            // TODO: 2017/5/4  
            convertView.setTag(holder);
        } else {
            // 通过 tag 找到缓存的布局
            holder = (ViewHolder) convertView.getTag();
        }
        // 设置布局中控件要显示的视图
        holder.name.setText(mData.get(position).getUserName());

        GildeUtil.setPicture(holder.image, mData.get(position).getPic());
        // TODO: 2017/5/4
        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.name)
        TextView name;

        ViewHolder(View itemView) {
            super();
            ButterKnife.bind(this, itemView);
        }
    }
}
