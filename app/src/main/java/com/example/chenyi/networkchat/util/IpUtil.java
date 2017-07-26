package com.example.chenyi.networkchat.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.Locale;

/**
 * Created by chenyi on 2017/6/18.
 */

public class IpUtil {

    // 通过 wifimanager 获取本机的 ip地址，并将它多播给其他设备
    public static String getIp() {
        WifiManager wm = (WifiManager) MyApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ip = wm.getConnectionInfo().getIpAddress();

        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }
}
