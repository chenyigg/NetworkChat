package com.example.chenyi.networkchat.friends;

import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.mvp.BasePresenter;
import com.example.chenyi.networkchat.mvp.BaseView;

import java.util.List;

/**
 * Created by chenyi on 2017/5/6.
 */

public interface FriendsContract {

    interface View extends BaseView {

        void showFriends(List<User> users);

        void showEmpty();

        void showError();

        void showLoading();

        void hideLoading();

    }

    interface Presenter extends BasePresenter<View> {

        List<String> getFriendsList();

    }

}
