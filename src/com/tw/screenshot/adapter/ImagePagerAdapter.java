package com.tw.screenshot.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.StringUtil;

public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private ArrayList<String> mImagePathList = new ArrayList<String>();

    public ImagePagerAdapter(Context context, String[] imagePaths) {
        assert (context != null);
        assert (imagePaths != null);
        mContext = context;
        for (String path : imagePaths) {
            mImagePathList.add(path);
        }
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300)).build();
    }
    
    public void removeImage(int position) {
        mImagePathList.remove(position);
        notifyDataSetChanged();
    }
    
    public void removeImage(String path) {
        mImagePathList.remove(path);
        notifyDataSetChanged();
    }
    
    public String getImagePath(int position) {
        return mImagePathList.get(position);
    }
    
    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImagePathList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = LayoutInflater.from(mContext).inflate(
                R.layout.item_pager_image, view, false);
        assert imageLayout != null;
        ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
        final ProgressBar spinner = (ProgressBar) imageLayout
                .findViewById(R.id.loading);

        String imageUrl = StringUtil.getFileUrl(mImagePathList.get(position));
        mImageLoader.displayImage(imageUrl, imageView, mOptions,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        spinner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                            FailReason failReason) {
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
                        Toast.makeText(mContext.getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();

                        spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                            Bitmap loadedImage) {
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
