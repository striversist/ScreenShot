package com.tw.screenshot.component;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.waps.AdInfo;
import cn.waps.AppConnect;
import cn.waps.SDKUtils;

public class AppDetail {

	private static AppDetail adDetail;
	private final Handler mHandler = new Handler();
	
	public static AppDetail getInstanct(){
		if(adDetail == null){
			adDetail = new AppDetail();
		}
		return adDetail;
	}
	
	public void showAdDetail(final Context context, final AdInfo adInfo){
    	try {
    		final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent);
    		if(adInfo != null){
				View view = View.inflate(context, context.getResources().getIdentifier("detail", "layout", context.getPackageName()), null);
				ImageView icon = (ImageView) view.findViewById(context.getResources().getIdentifier("detail_icon", "id", context.getPackageName()));
				TextView title = (TextView) view.findViewById(context.getResources().getIdentifier("detail_title", "id", context.getPackageName()));
				TextView version = (TextView) view.findViewById(context.getResources().getIdentifier("detail_version", "id", context.getPackageName()));
				TextView filesize = (TextView) view.findViewById(context.getResources().getIdentifier("detail_filesize", "id", context.getPackageName()));
				Button downButton1 = (Button) view.findViewById(context.getResources().getIdentifier("detail_downButton1", "id", context.getPackageName()));
				TextView content = (TextView) view.findViewById(context.getResources().getIdentifier("detail_content", "id", context.getPackageName()));
				TextView description = (TextView) view.findViewById(context.getResources().getIdentifier("detail_description", "id", context.getPackageName()));
				ImageView image1 = (ImageView) view.findViewById(context.getResources().getIdentifier("detail_image1", "id", context.getPackageName()));
				ImageView image2 = (ImageView) view.findViewById(context.getResources().getIdentifier("detail_image2", "id", context.getPackageName()));
				Button downButton2 = (Button) view.findViewById(context.getResources().getIdentifier("detail_downButton2", "id", context.getPackageName()));
				
				icon.setImageBitmap(adInfo.getAdIcon());
				icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
				title.setText(adInfo.getAdName());
				title.setTextSize(17);
				version.setText("  "+adInfo.getVersion());
				filesize.setText("  "+adInfo.getFilesize()+"M");
				content.setText(adInfo.getAdText());
				description.setText(adInfo.getDescription());
				
				new GetImagesTask(context, adInfo, image1, image2).execute();
			
				downButton1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AppConnect.getInstance(context).downloadAd(context, adInfo.getAdId());
						if(dialog != null){
							dialog.cancel();
						}
					}
				});
				downButton2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AppConnect.getInstance(context).downloadAd(context, adInfo.getAdId());
						if(dialog != null){
							dialog.cancel();
						}
					}
				});
			
				int bg_img = context.getResources().getIdentifier("detail_bg", "drawable", context.getPackageName());
				
				if(bg_img != 0){
					view.setBackgroundResource(bg_img);
				}else{
					view.setBackgroundResource(android.R.drawable.editbox_background);
				}
				
				LinearLayout layout = new LinearLayout(context);
				layout.setGravity(Gravity.CENTER);
				layout.setId(1);
				
				// 对小屏手机进行屏幕判断
				int displaySize = SDKUtils.getDisplaySize(context);
				if(displaySize == 320){
					layout.setPadding(15, 15, 15, 15);
				}else if(displaySize == 240){
					layout.setPadding(10, 10, 10, 10);
				}else{
					layout.setPadding(20, 20, 20, 20);
				}
				
				layout.setBackgroundColor(Color.argb(200, 10, 10, 10));
				
				layout.addView(view);
				
				dialog.setContentView(layout);
				dialog.show();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			AppConnect.getInstance(context).clickAd(context, adInfo.getAdId());
		}
    }
    
    private class GetImagesTask extends AsyncTask<Void, Void, Boolean> {
		Bitmap bitmap1;
		Bitmap bitmap2;
		AdInfo adInfo;
		ImageView image1;
		ImageView image2;
		Context context;
		public GetImagesTask(Context context, AdInfo adInfo, ImageView image1, ImageView image2){
			this.adInfo = adInfo;
			this.image1 = image1;
			this.image2 = image2;
			this.context = context;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean returnValue = false;
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			try {
				bitmap1 = BitmapFactory.decodeStream(
					new DefaultHttpClient().execute(new HttpGet(adInfo.getImageUrls()[0].replaceAll(" ", "%20"))).getEntity().getContent()
					, null, opts);
				bitmap2 = BitmapFactory.decodeStream(
					new DefaultHttpClient().execute(new HttpGet(adInfo.getImageUrls()[1].replaceAll(" ", "%20"))).getEntity().getContent()
					, null, opts);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return returnValue;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			try {
				if (bitmap1 != null && bitmap2 != null) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							try {
								int displaySize = SDKUtils.getDisplaySize(context);
								if(((Activity)context).getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
									if(displaySize == 320){
										image1.setLayoutParams(new LinearLayout.LayoutParams(130, 210));
										image2.setLayoutParams(new LinearLayout.LayoutParams(130, 210));
									}else if(displaySize == 240){
										image1.setLayoutParams(new LinearLayout.LayoutParams(100, 150));
										image2.setLayoutParams(new LinearLayout.LayoutParams(100, 150));
										
									}
								}else{
									if(displaySize == 320){
										image1.setLayoutParams(new LinearLayout.LayoutParams(210, 350));
										image2.setLayoutParams(new LinearLayout.LayoutParams(210, 350));
									}else if(displaySize == 240){
										image1.setLayoutParams(new LinearLayout.LayoutParams(140, 210));
										image2.setLayoutParams(new LinearLayout.LayoutParams(140, 210));
									}
								}
								image1.setImageBitmap(bitmap1);
								image2.setImageBitmap(bitmap2);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
