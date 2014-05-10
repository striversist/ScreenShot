package com.tw.screenshot.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import com.tw.screenshot.R;
import com.tw.screenshot.ShakeDetector;
import com.tw.screenshot.ShakeDetector.OnShakeListener;
import com.tw.screenshot.utils.FileUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class RootService extends Service implements OnShakeListener {
	
	private static final String TAG = "RootService";
	private static int sRequestId = 0;
	public enum RequestType { RunCommand, GetRootAccess, QueryRootAccess, StartShakeDetect, StopShakeDetect }
	private ShakeDetector mShakeDetector;
	private long mLastShakeTime;
	private Handler mUiHandler;
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    mShakeDetector = new ShakeDetector(getApplicationContext());
	    mShakeDetector.registerOnShakeListener(this);
	    mUiHandler = new Handler();
	}

	private IRootRequest.Stub mBinder = new IRootRequest.Stub() 
    {
        @Override
        public int sendRequest(final int type, Bundle data, final IRootRequestCallback callback) throws RemoteException 
        {
        	RequestType reqType = RequestType.values()[type];
            int result = 0;
            
            switch (reqType) {
				case RunCommand:
					String command = data.getString("commands");
					if (command == null)
						break;
					
					if (!runCommand(type, callback, command.split(";"))) {
						result = -1;
					}
					break;
				case GetRootAccess:
					if (RootTools.isAccessGiven()) {
						result = 1;
					}
					break;
				case QueryRootAccess:
					if (Shell.isRootShellOpen()) {
						result = 1;
					}
					break;
				case StartShakeDetect:
				    if (!mShakeDetector.start()) {
				        Toast.makeText(getApplicationContext(), "您的手机不支持摇晃截屏", Toast.LENGTH_LONG).show();
				    }
				    break;
				case StopShakeDetect:
				    mShakeDetector.stop();
				    break;
				default:
					break;
			}
            
            return result;
        }
    };
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private boolean runCommand(final int type, final IRootRequestCallback callback, String... command) {
		boolean exception = false;
		try {
			Log.d(TAG, "[runCommand] 1. time=" + System.currentTimeMillis());
			final List<String> lines = new ArrayList<String>();
			RootTools.getShell(true).add(new Command(sRequestId++, command) {
				
				@Override
				public void commandTerminated(int id, String reason) {
				    Log.d(TAG, "[runCommand] 3. time=" + System.currentTimeMillis() + " commandTerminated");
					if (callback != null) {
						try {
							lines.add(reason);
							callback.commandOutput(type, lines.toArray(new String[0]));
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void commandOutput(int id, String line) {
				    Log.d(TAG, "[runCommand] 2. time=" + System.currentTimeMillis() + " line=" + line);
					lines.add(line);
				}
				
				@Override
				public void commandCompleted(int id, int exitCode) {
				    Log.d(TAG, "[runCommand] 3. time=" + System.currentTimeMillis() + " commandCompleted");
					if (callback != null) {
						try {
							callback.commandOutput(type, lines.toArray(new String[0]));
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			exception = true;
		} catch (TimeoutException e) {
			e.printStackTrace();
			exception = true;
		} catch (RootDeniedException e) {
			e.printStackTrace();
			exception = true;
		}
		
		return exception == false;
	}

    @Override
    public void onShake() {
//        Toast.makeText(this, "检测到摇晃", Toast.LENGTH_SHORT).show();
        long now = System.currentTimeMillis();
        if (now - mLastShakeTime < 1500) {   // 防抖
            return;
        }
        mLastShakeTime = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(), "摇一摇正在截图", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultPath = takeScreenShot(createScreenShotPath());
                if (isFileAvailable(resultPath)) {
                    makeToast("截图保存：" + resultPath, true);
                } else {
                    makeToast("截图失败", true);
                }
            }
        }).start();
    }
    
    private void makeToast(final String text, final boolean longTime) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (longTime) {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private String createScreenShotPath() {
        String folderFormat = "yyyy-MM-dd";
        String fileFormat = "HHmmss";
        Date now = new Date();
        
        String root = Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name);
        String suffix = ".jpg";
        
        String folderPath = root + File.separator + new SimpleDateFormat(folderFormat, Locale.getDefault()).format(now);
        String fileName = new SimpleDateFormat(fileFormat, Locale.getDefault()).format(now);
        
        if (!FileUtil.isFileExsit(folderPath)) {
            FileUtil.createNewFolder(folderPath);
        }
        
        return folderPath + File.separator + fileName + suffix;
    }
    
    private String takeScreenShot(String fileName) {
        if (fileName == null)
            return null;
        
        String resultPath = null;
        resultPath = CaptureUtil.captureWithSystemScreenCap(getApplicationContext(), fileName);
        if (isFileAvailable(resultPath)) {
            return resultPath;
        }
        
        resultPath = CaptureUtil.captureWithFrameBuffer(getApplicationContext(), fileName);
        if (isFileAvailable(resultPath)) {
            return resultPath;
        }
        
        return resultPath;
    }
    
    private boolean isFileAvailable(String filePath) {
        return FileUtil.getFileSize(filePath) > 1000;
    }
}
