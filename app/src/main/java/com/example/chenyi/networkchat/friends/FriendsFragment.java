package com.example.chenyi.networkchat.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.bean.Friends;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.chat.ChatActivity;
import com.example.chenyi.networkchat.main.MainActivity;
import com.example.chenyi.networkchat.mvp.MVPBaseFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenyi on 2017/5/6.
 */

public class FriendsFragment extends MVPBaseFragment<FriendsContract.View, FriendsPresenter> implements FriendsContract.View {

    @BindView(R.id.list)
    ListView listView;
    FriendsAdapter adapter;

    public static FriendsFragment newInstance() {
        //通过 newInstance 保证 Fragment 不被重复构造，造成 fragment 重叠
        return new FriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FriendsAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("friend", adapter.getmData().get(position));
                intent.putExtra("user", ((MainActivity)getActivity()).getUser());
                startActivity(intent);
            }
        });
        return view;
    }

    public void setFriends(Friends friends) {
        adapter.setmData(friends.getFriends());
    }

    public void updateItem() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showFriends(List<User> users) {

    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showError() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
}
