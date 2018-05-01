package com.great.happyness;

import java.util.LinkedList;
import java.util.List;

import com.great.happyness.camera.util.FileUtil;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


public class WiseApplication extends Application implements 
	Application.ActivityLifecycleCallbacks{
	public static final String TAG = WiseApplication.class.getSimpleName();

	public static WiseApplication CONTEXT 	= null;

	public static int SCREEN_WIDTH 	= -1;
	public static int SCREEN_HEIGHT = -1;
	public static float DIMEN_RATE 	= -1.0F;
	public static int DIMEN_DPI 	= -1;

	private Bitmap mCameraBitmap;

	public static void setInstance(WiseApplication instance) {
		WiseApplication.CONTEXT = instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		setInstance(this);
		getScreenSize();
		FileUtil.initFolder();
		
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
		if (SCREEN_WIDTH > SCREEN_HEIGHT) {
			int t = SCREEN_HEIGHT;
			SCREEN_HEIGHT = SCREEN_WIDTH;
			SCREEN_WIDTH = t;
		}
		Log.e(TAG, "dimen_rate:"+DIMEN_RATE+" dimen_dpi:"+DIMEN_DPI
				+" screen_w:"+SCREEN_WIDTH + "screen_h:"+SCREEN_HEIGHT);
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

    public Bitmap getCameraBitmap() {
        return mCameraBitmap;
    }

    public void setCameraBitmap(Bitmap mCameraBitmap) {
        if (mCameraBitmap != null) {
            recycleCameraBitmap();
        }
        this.mCameraBitmap = mCameraBitmap;
    }

    public void recycleCameraBitmap() {
        if (mCameraBitmap != null) {
            if (!mCameraBitmap.isRecycled()) {
                mCameraBitmap.recycle();
            }
            mCameraBitmap = null;
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

