package com.example.chenyi.networkchat.chat;

import com.example.chenyi.networkchat.mvp.BasePresenter;
import com.example.chenyi.networkchat.mvp.BaseView;

/**
 * Created by chenyi on 2017/5/18.
 */

public interface ChatContract {

    interface View extends BaseView {

        void showEmpty();

        void showError();

        void showLoading();

        void hideLoading();

    }

    interface Presenter extends BasePresenter<View> {


    }
}
