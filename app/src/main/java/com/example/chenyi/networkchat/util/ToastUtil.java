package com.example.chenyi.networkchat.util;

import android.widget.Toast;

/**
 * Created by chenyi on 2017/5/23.
 */

public class ToastUtil {

    private static Toast toast;

    public static void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(MyApplication.getInstance(), content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
