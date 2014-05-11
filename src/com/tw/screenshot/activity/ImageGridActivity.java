package com.tw.screenshot.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.tw.screenshot.R;
import com.tw.screenshot.data.GlobalData;
import com.tw.screenshot.utils.FileUtil;

public class ImageGridActivity extends SherlockFragmentActivity implements Callback {

    private static final String FILE_SCHEME = "file://";
    private String mPath;
    private List<String> mImagePathList = new ArrayList<String>();
    private HandlerThread mThread;
    private ImageHandler mImageHandler;
    private Handler mUiHandler;
    private DisplayImageOptions mOptions;
    private GridView mGridView;
    protected ImageLoader mImageLoader;

    private enum SelfMessage {
        Show_Image_Grid
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView = (GridView) findViewById(R.id.gridview);
        mPath = getIntent().getStringExtra("path");
        mUiHandler = new Handler(this);
        mThread = new HandlerThread("ImageThread");
        mThread.start();
        mImageHandler = new ImageHandler(mThread.getLooper());
        mImageLoader = ImageLoader.getInstance();

        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisc(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mImageHandler.obtainMessage(0, mPath).sendToTarget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.getLooper().quit();
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
    public boolean handleMessage(Message msg) {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg) {
        case Show_Image_Grid:
            ((GridView) mGridView).setAdapter(new ImageAdapter());
            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    startImagePagerActivity(position);
                }
            });
            break;
        }
        return false;
    }
    
    private void startImagePagerActivity(int position) {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        intent.putExtra(GlobalData.IMAGE_URLS, getImageUrls());
        intent.putExtra(GlobalData.IMAGE_POSITION, position);
        startActivity(intent);
    }
    
    private String[] getImageUrls() {
        String[] imageUrls = new String[mImagePathList.size()];
        for (int i=0; i<mImagePathList.size(); ++i) {
            imageUrls[i] = FILE_SCHEME + mImagePathList.get(i);
        }
        return imageUrls;
    }

    private class ImageHandler extends Handler {

        public ImageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String path = (String) msg.obj;
            if (!FileUtil.isFileExsit(path))
                return;

            File folder = new File(path);
            String[] imageList = folder.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String fileName) {
                    return fileName.endsWith("jpg") || fileName.endsWith("png");
                }
            });
            if (imageList != null) {
                for (String image : imageList) {
                    mImagePathList.add(path + File.separator + image);
                }
                mUiHandler.obtainMessage(SelfMessage.Show_Image_Grid.ordinal()).sendToTarget();
            }

            return;
        }
    }
    
    private class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mImagePathList.size();
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
                view = getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            String imageUrl = FILE_SCHEME + mImagePathList.get(position);
            mImageLoader.displayImage(imageUrl, holder.imageView, mOptions, new SimpleImageLoadingListener() {
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
                                         public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                             holder.progressBar.setVisibility(View.GONE);
                                         }
                                     }, new ImageLoadingProgressListener() {
                                         @Override
                                         public void onProgressUpdate(String imageUri, View view, int current,
                                                 int total) {
                                             holder.progressBar.setProgress(Math.round(100.0f * current / total));
                                         }
                                     }
            );

            return view;
        }

        class ViewHolder {
            ImageView imageView;
            ProgressBar progressBar;
        }
    }
}
