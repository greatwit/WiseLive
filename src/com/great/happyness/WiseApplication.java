package com.great.happyness;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


public class WiseApplication extends Application implements 
	Application.ActivityLifecycleCallbacks{
	public static final String TAG = WiseApplication.class.getSimpleName();

	private static WiseApplication instance 	= null;

	public static int SCREEN_WIDTH 	= -1;
	public static int SCREEN_HEIGHT = -1;
	public static float DIMEN_RATE 	= -1.0F;
	public static int DIMEN_DPI 	= -1;

	public synchronized static WiseApplication getInstance() {
		return instance;
	}

	public static void setInstance(WiseApplication instance) {
		WiseApplication.instance = instance;
	}

	@Override
	public void onCreate() {
		setInstance(this);
		
		getScreenSize();
		super.onCreate();
		this.registerActivityLifecycleCallbacks(this);
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

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStarted(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResumed(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityPaused(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStopped(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	/************************************************************************/
}

