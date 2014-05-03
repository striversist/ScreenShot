package com.tw.screenshot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.adapter.MainFragmentAdapter;
import com.tw.screenshot.fragment.HomeFragment;
import com.tw.screenshot.fragment.HomeFragment.OnStartListener;
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
        
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setOnStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                finish();
            }
        });
        
        mPagerAdapter.addFragment(homeFragment, "首页");
        mPagerAdapter.addFragment(new HomeFragment(), "截图");
        mPagerAdapter.addFragment(new HomeFragment(), "推荐");
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.action_help:
                break;
            case R.id.action_feedback:
                break;
            case R.id.action_about:
                break;
            case R.id.action_exit:
                break;
        }
        return true;
    }

}
