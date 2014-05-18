package com.tw.screenshot.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.tw.screenshot.R;
import com.tw.screenshot.adapter.ImagePagerAdapter;
import com.tw.screenshot.data.Constant;
import com.tw.screenshot.utils.FileUtil;

public class ImagePagerActivity extends SherlockFragmentActivity {

    private static final String STATE_POSITION = "STATE_POSITION";
    private ViewPager mPager;
    private ImagePagerAdapter mAdapter;
    private ArrayList<String> mDeletedImagePathList = new ArrayList<String>();
    private Intent mShareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;

        String[] imagePaths = bundle.getStringArray(Constant.IMAGE_PATHS);
        if (imagePaths == null)
            return;

        int pagerPosition = bundle.getInt(Constant.IMAGE_POSITION, 0);
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(this, imagePaths);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(pagerPosition);
        mShareIntent = createShareIntent(getCurrentImagePath());
        
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Uri uri = Uri.fromFile(new File(getCurrentImagePath()));
                mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.image_pager_menu, menu);
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(mShareIntent);
        return super.onCreateOptionsMenu(menu);
    }
    
    private String getCurrentImagePath() {
        return mAdapter.getImagePath(mPager.getCurrentItem());
    }
    
    private Intent createShareIntent(String imagePath) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(new File(imagePath));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        case R.id.action_delete:
            new AlertDialog.Builder(this).setTitle(R.string.dialog_title_delete_image)
                .setMessage(R.string.dialog_msg_delete_image)
                .setPositiveButton(R.string.dialog_positive_btn_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = mPager.getCurrentItem();
                        String path = mAdapter.getImagePath(position);
                        FileUtil.deleteFile(path);
                        mDeletedImagePathList.add(path);
                        mAdapter.removeImage(position);
                        
                        Intent data = new Intent();
                        data.putStringArrayListExtra(Constant.DELETED_IMAGE_PATHS, mDeletedImagePathList);
                        setResult(100, data);
                    }
                })
                .setNegativeButton(R.string.dialog_negative_btn_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
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
