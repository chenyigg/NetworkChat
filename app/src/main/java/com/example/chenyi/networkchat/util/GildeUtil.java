package com.example.chenyi.networkchat.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.chenyi.networkchat.R;


/**
 * Created by chenyi on 2017/6/4.
 */

public class GildeUtil {

    public static void setPicture(ImageView img, String path) {
        Glide.with(MyApplication.getInstance())
                .load(path)
                .error(R.mipmap.ic_launcher_round)
                .fitCenter()
                .crossFade(500)
                .into(img);
    }

    public static void setBlurPicture(ImageView bg, String path) {
        Glide.with(MyApplication.getInstance())
                .load(path)
                .bitmapTransform(new MyBlurTransformation(MyApplication.getInstance().getApplicationContext()))
                .error(R.color.network_chat_primary)
                .crossFade(500)
                .into(bg);
    }
}
