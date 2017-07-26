package com.example.chenyi.networkchat.bean;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.chenyi.networkchat.util.TransformUtil;

/**
 * 好友的实体类
 * Created by chenyi on 2017/5/4.
 */

public class User implements Parcelable {

    private String ip;
    private String pic;
    private String name;
    private String phone;
    private String mail;

    public User() {
        super();
    }

    protected User(Parcel in) {
        ip = in.readString();
        pic = in.readString();
        name = in.readString();
        phone = in.readString();
        mail = in.readString();
    }

    public String getUserName() {
        String name = getName();
        if (name == null || name.equals("")) {
            name = getIp();
        }
        return name;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(Uri pic) {
        this.pic = TransformUtil.uri2Path(pic);
    }

    public void setPic(String path) {
        this.pic = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeString(pic);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(mail);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final User other = (User) obj;
        return this.getIp().equals(other.getIp());
    }
}
