package com.example.chenyi.networkchat.bean;

/**
 * Created by chenyi on 2017/6/24.
 */

public class MyFile {

    private boolean isFinish;
    private String fPath;

    public MyFile(String f) {
        super();
        isFinish = false;
        fPath = f;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public void setfPath(String fPath) {
        this.fPath = fPath;
    }

    public String getfPath() {
        return fPath;
    }

}
