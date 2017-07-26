package com.example.chenyi.networkchat.friends;

import com.example.chenyi.networkchat.mvp.BasePresenterImpl;

import java.util.List;

/**
 * Created by chenyi on 2017/5/6.
 */

public class FriendsPresenter extends BasePresenterImpl<FriendsContract.View> implements FriendsContract.Presenter {

    @Override
    public List<String> getFriendsList() {
        return null;
    }
}
