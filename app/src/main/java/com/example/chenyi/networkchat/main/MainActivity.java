package com.example.chenyi.networkchat.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenyi.networkchat.R;
import com.example.chenyi.networkchat.adapter.ViewPagerAdapter;
import com.example.chenyi.networkchat.bean.Friends;
import com.example.chenyi.networkchat.bean.MyMessage;
import com.example.chenyi.networkchat.bean.User;
import com.example.chenyi.networkchat.friends.FriendsFragment;
import com.example.chenyi.networkchat.person.PersonActivity;
import com.example.chenyi.networkchat.util.GildeUtil;
import com.example.chenyi.networkchat.util.GsonUtil;
import com.example.chenyi.networkchat.util.IpUtil;
import com.example.chenyi.networkchat.util.TransitionHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    public static final String POSITION = "tabLayout CurrentItem";
    private static final int EDIT_USER = 1;

    private User user;

    private AnimatedVectorDrawable line2arrows;

    private AnimatedVectorDrawable arrows2line;
    private boolean isClick;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.tab)
    TabLayout tabLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.page)
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    CircleImageView headIv;

    TextView name;
    TextView phone;
    TextView mail;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ((FriendsFragment) adapter.getFragment("好友")).updateItem();
                    break;
            }
        }
    };

    ChatService mService;

    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // 服务绑定成功并获取服务的实例
            ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
            mService = binder.getService();
            mService.setUser(getUser());
            mBound = true;
            Log.i("connect", "onServiceConnected: success");
            // 绑定成功后将自己的 ip 多播给其他用户
            mService.setListener1(new ChatService.ServiceListener1() {

                @Override
                public void updateFriends(Friends friends) {
                    FriendsFragment ff = ((FriendsFragment) adapter.getFragment("好友"));
                    if (ff.isResumed()) {
                        ff.setFriends(friends);
                        handler.sendEmptyMessage(1);
                    }
                }
            });
            MyMessage<User> message = new MyMessage<>(MyMessage.MULTICAST_ENTER, user);
            String request = GsonUtil.toJson(message);
            mService.multicastMessage(request);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupWindowAnimations();

        initAnim();
        initView();
        if (!initUser(user)) {
            user = new User();
            user.setIp(IpUtil.getIp());
        }
    }

    private boolean initUser(User user) {
        if (user == null) {
            return false;
        }
        if (user.getName() != null)
            name.setText(user.getName());
        if (user.getPhone() != null)
            phone.setText("电话："+user.getPhone());
        if (user.getMail() != null)
            mail.setText("邮箱："+user.getMail());
        GildeUtil.setPicture(headIv, user.getPic());
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            Intent intent = new Intent(this, ChatService.class);
            intent.putExtra("user", user);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void setupWindowAnimations() {
        // Re-enter transition is executed when returning to this activity
        Slide slideTransition = new Slide();
        slideTransition.setSlideEdge(Gravity.START);
        slideTransition.setDuration(500);
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    private void initAnim() {
        arrows2line = (AnimatedVectorDrawable) getDrawable(R.drawable.anim_arrows2line);
        line2arrows = (AnimatedVectorDrawable) getDrawable(R.drawable.anim_line2arrows);
        isClick = false;
    }

    private void animLine2Arrows(boolean open) {
        AnimatedVectorDrawable drawable = open ? line2arrows : arrows2line;
        toolbar.setNavigationIcon(drawable);
        drawable.start();
    }

    /**
     * 初始化界面UI
     */
    private void initView() {
        ButterKnife.bind(this);
        setupToolbar();
        setupDrawer();
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager, true);
    }

    private void setupToolbar() {
        //设置右上角菜单
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setNavigationIcon(R.drawable.line);
        //设置toolbar菜单的监听
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        // TODO: 查询页面2017/5/2
                        Toast.makeText(MainActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpen = drawer.isDrawerOpen(GravityCompat.START);
                animLine2Arrows(!isOpen);
                isClick = true;
                if (!isOpen)
                    drawer.openDrawer(GravityCompat.START);
                else
                    drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void setupDrawer() {
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (!isClick) {
                    animLine2Arrows(true);
                }
                isClick = false;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (!isClick) {
                    animLine2Arrows(false);
                }
                isClick = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        View navHeaderView = navigationView.getHeaderView(0);
        headIv = (CircleImageView) navHeaderView.findViewById(R.id.pic);
        name = (TextView) navHeaderView.findViewById(R.id.name);
        phone = (TextView) navHeaderView.findViewById(R.id.phone);
        mail = (TextView) navHeaderView.findViewById(R.id.mail);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.person:
                        MainActivity.this.startMyActivity();
                        break;
                    case R.id.tool:
                        break;
                    case R.id.edit:
                        MyMessage m = new MyMessage(MyMessage.MULTICAST_LEAVE);
                        String leave = GsonUtil.toJson(m);
                        mService.multicastMessage(leave);
                        finish();
                        break;
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(FriendsFragment.newInstance(), "信息");
        adapter.addFragment(FriendsFragment.newInstance(), "好友");
        viewPager.setAdapter(adapter);
    }

    public void startMyActivity() {
        final Pair<View, String>[] pairs = TransitionHelper.createSafeTransitionParticipants(this,
                                false, new Pair<>(headIv, "myPicture"), new Pair<>(name, "myName"));
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("type", PersonActivity.REVISABILITY);
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairs);
        startActivityForResult(intent, EDIT_USER, transitionActivityOptions.toBundle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存tabLayout的当前项，便于恢复
        outState.putInt(POSITION, tabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        viewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_USER:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    User newUser = bundle.getParcelable("user");
                    if (newUser != null) {
                        user = newUser;
                        initUser(user);
                        mService.setUser(user);
                        MyMessage<User> message = new MyMessage<>(MyMessage.MULTICAST_CHANGE, user);
                        String request = GsonUtil.toJson(message);
                        mService.multicastMessage(request);
                    }
                }
                break;
        }
    }

    public User getUser() {
        return user;
    }
}
