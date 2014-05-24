package com.tw.screenshot.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tw.screenshot.R;
import com.tw.screenshot.adapter.GridImageAdapter;
import com.tw.screenshot.data.Constant;
import com.tw.screenshot.utils.DeviceUtil;
import com.tw.screenshot.utils.FileUtil;

public class ImageGridActivity extends SherlockFragmentActivity implements Callback {

    private static final int RequestCode = 100;
    private String mPath;
    private HandlerThread mHandlerThread;
    private ImageHandler mWorkHandler;
    private Handler mUiHandler;
    private GridView mGridView;
    private GridImageAdapter mAdapter;
    private SuperCardToast mSuperCardToast;

    private enum SelfMessage {
        Show_Image_Grid
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView = (GridView) findViewById(R.id.gridview);
        mAdapter = new GridImageAdapter(this);
        mPath = getIntent().getStringExtra("path");
        mUiHandler = new Handler(this);
        mHandlerThread = new HandlerThread("WorkThread");
        mHandlerThread.start();
        mWorkHandler = new ImageHandler(mHandlerThread.getLooper());
        mSuperCardToast = new SuperCardToast(this, SuperToast.Type.PROGRESS);

        mSuperCardToast.setText(getString(R.string.loading_data));
        mSuperCardToast.setDuration(SuperToast.Duration.VERY_SHORT);
        mSuperCardToast.show();
        mGridView.setFastScrollEnabled(true);
        mWorkHandler.obtainMessage(0, mPath).sendToTarget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandlerThread.getLooper().quit();
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
            if (mAdapter != null) {
                mAdapter.setMultiChoiceEnabled(true);
                startActionMode(new AnActionModeOfEpicProportions());
            }
            break;
        }
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg) {
        case Show_Image_Grid:
            @SuppressWarnings("unchecked")
            ArrayList<String> imagePathList = (ArrayList<String>) msg.obj;
            mAdapter = new GridImageAdapter(this);
            mAdapter.addAll(imagePathList);
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (mAdapter != null && mAdapter.isMultiChoiceMode()) {
                        mAdapter.toggleSelection(view, position);
                    } else {
                        startImagePagerActivity(position);
                    }
                }
            });
            mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                        final int position, long id) {
                    DeviceUtil.vibrate(getApplicationContext(), 200);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImageGridActivity.this).setTitle(null);
                    builder.setItems(R.array.longclick_image_menu, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(Intent.createChooser(createShareIntent(mAdapter.getImagePath(position)), getString(R.string.please_choose)));
                        }
                    }).create().show();
                    return true;
                }
            });
            break;
        }
        return false;
    }
    
    private Intent createShareIntent(String imagePath) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(new File(imagePath));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != RequestCode || data == null)
            return;
        ArrayList<String> deletedImageList = data.getStringArrayListExtra(Constant.DELETED_IMAGE_PATHS);
        if (deletedImageList == null || deletedImageList.isEmpty())
            return;
        if (mAdapter != null) {
            mAdapter.deleteImageList(deletedImageList);
        }
    }
    
    private void startImagePagerActivity(int position) {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        intent.putExtra(Constant.IMAGE_PATHS, mAdapter.getAllImagePath().toArray(new String[0]));
        intent.putExtra(Constant.IMAGE_POSITION, position);
        startActivityForResult(intent, RequestCode);
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
                ArrayList<String> imagePathList = new ArrayList<String>();
                for (String image : imageList) {
                    imagePathList.add(path + File.separator + image);
                }
                Collections.reverse(imagePathList); // 最新的排在最前面
                mUiHandler.obtainMessage(SelfMessage.Show_Image_Grid.ordinal(), imagePathList).sendToTarget();
            }

            return;
        }
    }
    
    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(Menu.NONE, R.id.action_delete, Menu.NONE, "Delete")
                .setIcon(R.drawable.ic_delete)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                final ArrayList<String> selections = mAdapter.getAllSelection();
                if (selections.isEmpty()) {
                    Toast.makeText(ImageGridActivity.this, getString(R.string.select_image_tips), Toast.LENGTH_SHORT).show();
                } else {
                    final ProgressDialog dialog = new ProgressDialog(ImageGridActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage(getString(R.string.dialog_delete_msg));
                    dialog.show();
                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (String path : selections) {
                                FileUtil.deleteFile(path);
                            }
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.deleteImageList(selections);
                                    dialog.dismiss();
                                    mode.finish();
                                }
                            });
                        }
                    });
                }
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.setMultiChoiceEnabled(false);
        }
    }
}
