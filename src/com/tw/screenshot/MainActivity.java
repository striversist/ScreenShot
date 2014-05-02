package com.tw.screenshot;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.tw.screenshot.adapter.MainFragmentAdapter;
import com.tw.screenshot.fragment.HomeFragment;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity {

    private ViewPager mPager;
    private MainFragmentAdapter mPagerAdapter;
    private PageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPagerAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new HomeFragment(), "首页");
        mPagerAdapter.addFragment(new HomeFragment(), "截图");
        mPagerAdapter.addFragment(new HomeFragment(), "推荐");
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
