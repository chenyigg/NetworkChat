package com.example.chenyi.networkchat.mvp;

/**
 * Created by chenyi on 2017/5/6.
 */

public class BasePresenterImpl<V extends BaseView> implements BasePresenter<V> {

    public V mView;

    @Override
    public void attachView(V view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
