package com.example.chenyi.networkchat.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.chenyi.networkchat.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyi on 2017/6/21.
 */

public class Friends implements Parcelable {

    private List<User> users;

    private Friends(Parcel in) {
        users = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Friends> CREATOR = new Creator<Friends>() {
        @Override
        public Friends createFromParcel(Parcel in) {
            return new Friends(in);
        }

        @Override
        public Friends[] newArray(int size) {
            return new Friends[size];
        }
    };

    public List<User> getFriends() {
        return users;
    }

    public User getFriend(int postion) {
        return users.get(postion);
    }

    public User getFriend(String ip) {
        for (int i = 0; i < users.size(); i++) {
            User e = users.get(i);
            if (ip.equals(e.getIp())) {
                return e;
            }
        }
        return new User();
    }

    public Friends() {
        users = new ArrayList<>();
    }

//    public void newFriend(String ip) {
//        User newUser = new User();
//        newUser.setIp(ip);
//        users.add(newUser);
//    }

    public void newFriend(User user) {
        deleteFriend(user.getIp());
        user.setPic("");
        users.add(user);
    }

    public void updateFriendPic(String ip, String path) {
        for (int i = 0; i < users.size(); i++) {
            User e = users.get(i);
            if (ip.equals(e.getIp())) {
                e.setPic(path);
                Log.i("sss", "updatePic: "+ GsonUtil.toJson(e));
                return;
            }
        }
    }

    public void deleteFriend(String ip) {
        for (int i = 0; i < users.size(); i++) {
            User e = users.get(i);
            if (ip.equals(e.getIp())) {
                users.remove(e);
                return;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(users);
    }
}
