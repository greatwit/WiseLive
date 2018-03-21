package com.great.happyness;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

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

	public synchronized static WiseApplication getInstance() {
		return instance;
	}

	public static void setInstance(WiseApplication instance) {
		WiseApplication.instance = instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setInstance(this);
		getScreenSize();
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

