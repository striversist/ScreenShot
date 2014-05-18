package com.tw.screenshot.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.R;
import com.tw.screenshot.adapter.ImagePagerAdapter;
import com.tw.screenshot.data.Constant;

public class ImagePagerActivity extends SherlockFragmentActivity {

    private static final String STATE_POSITION = "STATE_POSITION";
    private ViewPager mPager;
    private ImagePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;

        String[] imageUrls = bundle.getStringArray(Constant.IMAGE_URLS);
        if (imageUrls == null)
            return;

        int pagerPosition = bundle.getInt(Constant.IMAGE_POSITION, 0);
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(this, imageUrls);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(pagerPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.image_grid_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        case R.id.action_delete:
            break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }
}
