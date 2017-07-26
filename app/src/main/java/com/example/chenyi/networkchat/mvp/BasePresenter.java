package com.example.chenyi.networkchat.mvp;

/**
 * Created by chenyi on 2017/5/6.
 */

public interface BasePresenter <V extends BaseView>{
    void attachView(V view);

    void detachView();
}
