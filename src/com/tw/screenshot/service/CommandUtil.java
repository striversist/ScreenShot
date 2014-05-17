package com.tw.screenshot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.Constants;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import com.tw.screenshot.MyApplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class CommandUtil {
	
	private static final String TAG = "CommandUtil";
	private static Context sContext;
	private static IRootRequest sRootRequest;
	private static int sRequestId = 0;

	public interface CommandResultCallback {
		void commandOutput(boolean executed, String... lines);
	}
	
	/**
	 * 判断手机有没有被root
	 * @return
	 */
	public static boolean isRootAvailable() {
        return findBinary("su");
    }
	
	/**
	 * 判断是否已经获取了root权限（用户授权过）
	 * @param context
	 * @return
	 */
	public static boolean hasRootAccess(Context context) {
		if (context == null)
			return false;
		
		if (!checkInitialized()) {
		    if (!waitForInitialized(2000)) {
		        return false;
		    }
		}
		
		try {
			int result = sRootRequest.sendRequest(RootService.RequestType.QueryRootAccess.ordinal(), null, null);
			if (result > 0) {
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 尝试获取root权限，会弹出root授权框
	 * @param context
	 * @return true(用户授权); false(拒绝授权)
	 */
	public static boolean tryGetRootAccesss(Context context) {
		if (context == null)
			return false;
		
		if (!checkInitialized()) {
			if (Looper.myLooper() == Looper.getMainLooper()) {	// 主线程不能卡住
				return false;
			} else {
				if (!waitForInitialized(2000)) {
				    return false;
				}
			}
		}
		
		return isRootAccessGivenInternal(context.getApplicationContext());
	}
	
	/**
	 * 等待初始化完成
	 * @param timeout
	 */
	private static boolean waitForInitialized(int timeout) {
	    if (timeout < 0)
	        return false;
	    
	    final int SLEEP_TIME = 500;    // ms
	    int maxTimes = timeout / SLEEP_TIME;
	    if (maxTimes == 0) {
	        maxTimes = 1;
	    }
	    int retryTimes = 0;
        while (retryTimes++ < maxTimes) {  // 尝试等待初始化完成
            Log.i(TAG, "[waitForInitialized] retryTimes=" + retryTimes);
            if (sContext != null && sRootRequest != null)   // 初始化完成
                break;
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (sContext != null && sRootRequest != null) {  // 初始化完成
            return true;
        } else {
            return false;
        }
	}
	
	/**
	 * 以root或普通权限运行命令，异步获取执行结果
	 * @param context
	 * @param root
	 * @param callback
	 * @param command
	 */
	public static void runCommand(final Context context, boolean root, final CommandResultCallback callback, String... command) {
		if (context == null || command == null)
			return;
		
		if (!checkInitialized()) {
			if (callback != null) {
				callback.commandOutput(false, "");
			}
			return;
		}
		
		assert (callback != null);
		final List<String> output = new ArrayList<String>();
		if (root) {
			sendRemoteRequest(RootService.RequestType.RunCommand, new CommandResultCallback() {
				@Override
				public void commandOutput(boolean executed, String... lines) {
				    Log.d(TAG, "runCommand executed=" + executed);
					callback.commandOutput(executed, lines);
				}	
			}, command);
		} else {	// non-root command
			try {
				RootTools.getShell(false).add(new Command(sRequestId++, command) {
					
					@Override
					public void commandTerminated(int id, String reason) {
						callback.commandOutput(false, reason);
					}
					
					@Override
					public void commandOutput(int id, String line) {
						output.add(line);
					}
					
					@Override
					public void commandCompleted(int id, int exitCode) {
						callback.commandOutput(true, output.toArray(new String[0]));
					}
				});
			} catch (IOException e) {
				callback.commandOutput(false, e.toString());
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
				callback.commandOutput(false, e.toString());
			} catch (RootDeniedException e) {
				e.printStackTrace();
				callback.commandOutput(false, e.toString());
			}
		}
	}
	
	/**
	 * 以root或普通权限执行命令，同步获取执行结果
	 * @param context
	 * @param root
	 * @param timeout
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static String[] runCommand(final Context context, boolean root, int timeout, String... command) throws IOException {
		if (context == null || command == null)
			return null;
		
		if (!checkInitialized())
			return null;
		
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IOException("Can not call in MainThread");
		}
		
		final Object mutex = new Object();
		final List<String> output = new ArrayList<String>();
		boolean exception = false;
		if (root) {
			sendRemoteRequest(RootService.RequestType.RunCommand, new CommandResultCallback() {
				@Override
				public void commandOutput(boolean executed, String... lines) {
				    Log.d(TAG, "runCommand executed=" + executed);
					for (String line : lines) {
						output.add(line);
					}
					unlock(mutex);
				}	
			}, command);
		} else {	// non-root command
			try {
				RootTools.getShell(false).add(new Command(sRequestId++, command) {
					
					@Override
					public void commandTerminated(int id, String reason) {
						unlock(mutex);
					}
					
					@Override
					public void commandOutput(int id, String line) {
						output.add(line);
					}
					
					@Override
					public void commandCompleted(int id, int exitCode) {
						unlock(mutex);
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
		}
		
		if (!exception) {
			try {
				synchronized (mutex) {
					mutex.wait(timeout);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return output.toArray(new String[0]);
	}

	private static boolean checkInitialized() {
        if (sContext == null) {
        	sContext = MyApplication.getInstance().getApplicationContext();
        }
        if (sRootRequest == null) {
        	bindService();
        }
        if (sContext == null || sRootRequest == null) {
//        	throw new IllegalStateException("context is " + sContext + ", sRootRequest is " + sRootRequest);
        	return false;
        }
        
        return true;
    }
	
	private static void bindService() {
		if (sContext == null || sRootRequest != null)
			return;
		
		Intent serviceIntent = new Intent(sContext, RootService.class);
        sContext.startService(serviceIntent);
        sContext.bindService(serviceIntent, new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				sRootRequest = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				IRootRequest request = IRootRequest.Stub.asInterface(service);
				if (request != null) {
					sRootRequest = request;
				}
			}
		}, Context.BIND_AUTO_CREATE);
	}
	
	private static void unlock(Object mutex)
	{
		if (mutex != null) {
			synchronized (mutex) {
				mutex.notifyAll();
			}
		}
	}
	
	private static boolean isRootAccessGivenInternal(Context context) {
		if (context == null)
			return false;
		
		try {
			int result = sRootRequest.sendRequest(RootService.RequestType.GetRootAccess.ordinal(), null, null);
			if (result > 0) {
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static void sendRemoteRequest(final RootService.RequestType type, 
			final CommandResultCallback callback, final String... commands) {
		checkInitialized();
		if (sRootRequest != null) {
			switch (type) {
				case RunCommand:
					String allCommands = "";
					for (String cmd : commands) {
						allCommands += cmd + ";";
					}
					Bundle bundle = new Bundle();
					bundle.putString("commands", allCommands);
					
					try {
						int result = sRootRequest.sendRequest(RootService.RequestType.RunCommand.ordinal(), bundle, 
								new IRootRequestCallback.Stub() {
									@Override
									public int commandOutput(int type, String[] lines) throws RemoteException {
										callback.commandOutput(true, lines);
										return 0;
									}
								});
						if (result < 0) {
							callback.commandOutput(false);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						callback.commandOutput(false);
					}
					break;
				default:
					callback.commandOutput(false);
					break;
			}
		} else {
			callback.commandOutput(false);
		}
	}
	
	private static boolean findBinary(final String binaryName) {
        boolean found = false;
        final List<String> list = new ArrayList<String>();
        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};

        try {
            for(final String path : places) {
                CommandCapture cc = new CommandCapture(0, false, "ls " + path + binaryName) {
                    @Override
                    public void commandOutput(int id, String line) {
                        if (TextUtils.equals(line, path + binaryName)) {
                            list.add(path);
                            Log.d(TAG, binaryName + " was found here: " + path);
                        }
                    }
                };

                RootTools.getShell(false).add(cc);
                commandWait(cc);

            }

            found = !list.isEmpty();
        } catch (Exception e) {
            Log.d(TAG, binaryName + " was not found, more information MAY be available with Debugging on.");
        }
        
        return found;
    }
    
    private static void commandWait(Command cmd) throws Exception {
        while (!cmd.isFinished()) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!cmd.isExecuting() && !cmd.isFinished()) {
                if (!Shell.isExecuting && !Shell.isReading) {
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else if (Shell.isExecuting && !Shell.isReading) {
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else {
                    Log.e(Constants.TAG, "Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                }
            }
        }
    }
}
