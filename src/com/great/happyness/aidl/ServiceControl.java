package com.great.happyness.aidl;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class ServiceControl implements ServiceConnection
{

	protected String TAG = getClass().getName();
	
	IActivityReq mActivityReq;
	
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
    
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mActivityReq = IActivityReq.Stub.asInterface(service);
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

