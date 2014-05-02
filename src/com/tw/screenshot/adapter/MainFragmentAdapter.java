package com.tw.screenshot.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentAdapter extends FragmentPagerAdapter {

    private List<TitleFragment> mFragmentList = new ArrayList<TitleFragment>();
    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }
    
    public void addFragment(Fragment fragment, String title) {
        if (fragment != null && title != null) {
            mFragmentList.add(new TitleFragment(fragment, title));
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position >= mFragmentList.size()) {
            return null;
        }
        return mFragmentList.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).title;
    }

    private class TitleFragment {
        public TitleFragment(Fragment fragment, String title) {
            this.title = title;
            this.fragment = fragment;
        }
        String title = "";
        Fragment fragment;
    }
}
