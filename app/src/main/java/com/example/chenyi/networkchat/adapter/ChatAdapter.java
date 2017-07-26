package com.example.chenyi.networkchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by chenyi on 2017/5/7.
 */

public class ChatAdapter extends BaseAdapter {

    public static final int TYPR_ME = 0;
    public static final int TYPE_OTHER = 1;

    List<String> mData;
    private LayoutInflater mInflater;

    public ChatAdapter(Context context, List<String> data) {
        mData = data;
        mInflater = LayoutInflater.from(context);
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
    public int getItemViewType(int position) {
        // TODO: 2017/5/7
        if (position == 1) {
            return TYPE_OTHER;
        } else {
            return TYPR_ME;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if (getItemViewType(position) == TYPR_ME) {
                // TODO: 2017/5/7
            } else {
                // TODO: 2017/5/7
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    class ViewHolder {
        ImageView image;
        TextView name;
    }
}
