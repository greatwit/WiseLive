package com.great.happyness;

import java.util.LinkedList;
import java.util.List;

import com.great.happyness.aidl.IActivityReq;
import com.great.happyness.wifi.WiFiAPService;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


public class WiseApplication extends Application {
	public static final String TAG = "MyApplication";


	private static WiseApplication instance;

	public static int SCREEN_WIDTH = -1;
	public static int SCREEN_HEIGHT = -1;
	public static float DIMEN_RATE = -1.0F;
	public static int DIMEN_DPI = -1;

	IActivityReq mActivityReq;
	
	public synchronized static WiseApplication getInstance() {
		return instance;
	}

	public static void setInstance(WiseApplication instance) {
		WiseApplication.instance = instance;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		bindService();
		setInstance(this);
		getScreenSize();
	}

    static boolean linkSuccess;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	mActivityReq = IActivityReq.Stub.asInterface(service);
            linkSuccess = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        	bindService();
        }
    };
	
    private void bindService() {
        Intent intent = new Intent(this, WiFiAPService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }
    
	/**
	 * 初始化屏幕宽高
	 */
	public void getScreenSize() 
	{
		WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		Display display = windowManager.getDefaultDisplay();
		display.getMetrics(dm);
		DIMEN_RATE 		= dm.density / 1.0F;
		DIMEN_DPI 		= dm.densityDpi;
		SCREEN_WIDTH 	= dm.widthPixels;
		SCREEN_HEIGHT 	= dm.heightPixels;
		if (SCREEN_WIDTH > SCREEN_HEIGHT) 
		{
			int t = SCREEN_HEIGHT;
			SCREEN_HEIGHT = SCREEN_WIDTH;
			SCREEN_WIDTH = t;
		}
	}

	private List<Activity> mList = new LinkedList<Activity>();

	/**
	 * add Activity
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	/**
	 * remove Activity
	 * 
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		mList.remove(activity);
	}

	/**
	 * 退出App
	 * 
	 * @param exit
	 */
	public void exit(boolean exit) {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (exit) {
				System.exit(0);
			}
		}
	}


	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/************************************************************************/
}

