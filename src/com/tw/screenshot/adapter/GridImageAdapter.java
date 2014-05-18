package com.tw.screenshot.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.StringUtil;

public class GridImageAdapter extends BaseAdapter {

    private Context mContext;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private List<ImageItem> mImageItemList = new ArrayList<ImageItem>();
    private boolean mMultiChoiceMode = false;

    private class ImageItem {
        String path = "";
        boolean isSelected = false;
    }
    
    public GridImageAdapter(Context context) {
        assert (context != null);
        mContext = context;
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisc(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public void addAll(List<String> imagePathList) {
        if (imagePathList == null)
            return;
        
        mImageItemList.clear();
        for (String path : imagePathList) {
            ImageItem item = new ImageItem();
            item.path = path;
            item.isSelected = false;
            mImageItemList.add(item);
        }
        notifyDataSetChanged();
    }
    
    public void setMultiChoiceEnabled(boolean enable) {
        mMultiChoiceMode = enable;
        notifyDataSetChanged();
    }
    
    public boolean isMultiChoiceMode() {
        return mMultiChoiceMode;
    }
    
    public void toggleSelection(View view, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (mImageItemList.get(position).isSelected) {
            mImageItemList.get(position).isSelected = false;
        } else {
            mImageItemList.get(position).isSelected = true;
        }
        holder.selectImage.setSelected(mImageItemList.get(position).isSelected);
    }
    
    public ArrayList<String> getAllSelection() {
        ArrayList<String> result = new ArrayList<String>();
        for (ImageItem item : mImageItemList) {
            if (item.isSelected) {
                result.add(item.path);
            }
        }
        return result;
    }
    
    public void clearAllSelection() {
        for (ImageItem item : mImageItemList) {
            item.isSelected = false;
        }
        notifyDataSetChanged();
    }
    
    public void deleteImageList(ArrayList<String> imageList) {
        if (imageList == null || imageList.isEmpty())
            return;
        ArrayList<ImageItem> imageItemList = new ArrayList<ImageItem>();
        for (ImageItem item : mImageItemList) {
            if (imageList.contains(item.path))
                continue;
            imageItemList.add(item);
        }
        mImageItemList.clear();
        mImageItemList.addAll(imageItemList);
        clearAllSelection();
    }
    
    public ArrayList<String> getAllImagePath() {
        ArrayList<String> allImagePath = new ArrayList<String>();
        for (ImageItem item : mImageItemList) {
            allImagePath.add(item.path);
        }
        return allImagePath;
    }

    @Override
    public int getCount() {
        return mImageItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.item_grid_image, parent, false);
            holder = new ViewHolder();
            assert view != null;
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.selectImage = (ImageView) view.findViewById(R.id.select_image);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String imageUrl = StringUtil.getFileUrl(mImageItemList.get(position).path);
        mImageLoader.displayImage(imageUrl, holder.imageView, mOptions,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                            FailReason failReason) {
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                            Bitmap loadedImage) {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view,
                            int current, int total) {
                        holder.progressBar.setProgress(Math.round(100.0f
                                * current / total));
                    }
                });

        if (mMultiChoiceMode) {
            holder.selectImage.setSelected(mImageItemList.get(position).isSelected);
            holder.selectImage.setVisibility(View.VISIBLE);
        } else {
            holder.selectImage.setVisibility(View.GONE);
        }
        
        return view;
    }

    class ViewHolder {
        ImageView imageView;
        ImageView selectImage;
        ProgressBar progressBar;
    }
}
