package com.example.chenyi.networkchat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置主界面viewpager的adapter
 * Created by chenyi on 2017/5/2.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragList;
    private List<String> tabTitles;
    private Map<String, Fragment> fragmentMap;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragList = new ArrayList<>();
        tabTitles = new ArrayList<>();
        fragmentMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    @Override
    public int getCount() {
        return fragList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        if (!tabTitles.contains(title)) {
            fragList.add(fragment);
            tabTitles.add(title);
            fragmentMap.put(title, fragment);
        }
    }

    public Fragment getFragment(String title) {
        return fragmentMap.get(title);
    }
}
