package com.tw.screenshot.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.tw.screenshot.R;
import com.tw.screenshot.data.GlobalData;

import android.os.Bundle;
import android.os.Parcelable;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ImagePagerActivity extends SherlockFragmentActivity {
    
    private static final String STATE_POSITION = "STATE_POSITION";
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        
        String[] imageUrls = bundle.getStringArray(GlobalData.IMAGE_URLS);
        if (imageUrls == null)
            return;
        
        int pagerPosition = bundle.getInt(GlobalData.IMAGE_POSITION, 0);
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }

        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_error)
            .resetViewBeforeLoading(true)
            .cacheOnDisc(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(true)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ImagePagerAdapter(imageUrls));
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
    
    private class ImagePagerAdapter extends PagerAdapter {

        private String[] images;
        private LayoutInflater inflater;

        ImagePagerAdapter(String[] images) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            mImageLoader.displayImage(images[position], imageView, mOptions, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
