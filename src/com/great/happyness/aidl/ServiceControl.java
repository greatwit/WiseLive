package com.great.happyness.aidl;


import java.util.ArrayList;

import com.great.happyness.service.ServiceCreatedListen;
import com.great.happyness.service.WiFiAPService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class ServiceControl implements ServiceConnection
{

	protected String TAG = getClass().getName();
	
	IActivityReq mActivityReq;
	private static ArrayList<ServiceCreatedListen> mListenerActivity = new ArrayList<ServiceCreatedListen>();
	private static ServiceControl gServiceControl=null;  
	
    //静态工厂方法   
    public static ServiceControl getInstance() {  
         if (gServiceControl == null) {    
        	 gServiceControl = new  ServiceControl();
         }    
        return gServiceControl;  
    }
    
	public ServiceControl() {
	}
    
    public void bindService(Context cont, ServiceCreatedListen listen) {
        Intent intent = new Intent(cont, WiFiAPService.class);
        cont.bindService(intent, getInstance(), Service.BIND_AUTO_CREATE);
        mListenerActivity.add(listen);
        Log.w(TAG, "bindService");
    }
	
    public void unbindService(Context cont)
    {
    	cont.unbindService(getInstance());     //取消服务的绑定
    }
    
    public void registListener(ServiceCreatedListen listen)
    {
    	mListenerActivity.add(listen);
    }
    
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mActivityReq = IActivityReq.Stub.asInterface(service);
		for(ServiceCreatedListen it : mListenerActivity){
			it.serviceChanged(1);
			mListenerActivity.remove(it);
		}
		Log.w(TAG, "onServiceConnected");
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		Log.w(TAG, "onServiceDisconnected");
	}
	
	public IActivityReq getActivityReq()
	{
		return mActivityReq;
	}
	
}

