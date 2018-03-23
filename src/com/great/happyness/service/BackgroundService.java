package com.great.happyness.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 普通的后台Service进程
 *
 * @author clock
 * @since 2016-04-12
 */
public class BackgroundService extends Service {

    private final static String TAG = BackgroundService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.i(TAG, "BackgroundService onCreate");
        super.onCreate();
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "BackgroundService onStartCommand");
		return super.onStartCommand(intent, START_STICKY, startId);
	}
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
    	Log.i(TAG, "BackgroundService onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "BackgroundService onDestroy");
        super.onDestroy();
    }
}
