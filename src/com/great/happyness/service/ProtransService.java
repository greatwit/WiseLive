package com.great.happyness.service;


import com.great.happyness.service.aidl.IServiceListen;
import com.great.happyness.service.aidl.IActivityReq;
import com.great.happyness.protrans.lib.ProtoTrans;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @ClassName:  WiFiAPService   
 * @Description:  wifi hot manage service
 * @author:  
 * @date:   2018.04.26
 * version:1.0.0
 */
public class ProtransService extends Service 
{
	private static String TAG = ProtransService.class.getSimpleName();
	
	public static String ACTION_START_SERVICE 		= "action_start_service";
	public static String ACTION_STOP_SERVICE 		= "action_stop_service";

	
    public static final int WIFI_CMD 	= 121;
    public static final int NET_CMD 	= 122;
	
    private int mSocketHandle 		= -1;
    private int mSocketPort 		= 18612;
    private ProtoTrans mNetProto 	= new ProtoTrans();

    
    Binder mBinder = new IActivityReq.Stub() {
        @Override
        public void action(int action, String datum) throws RemoteException {
        	Log.i(TAG, "action:"+action + " datum:"+datum);
            switch (action) {
	            case WIFI_CMD:
	            	break;
	            	
	            case NET_CMD:
	            	break;
            }
        }

        @Override
        public void registerListener(IServiceListen listener) throws RemoteException {
        	ListenerManager.Instance().register(listener);
        }

        @Override
        public void unregisterListener(IServiceListen listener) throws RemoteException {
        	ListenerManager.Instance().unregister(listener);
        }

		@Override
		public int sendData(String addr, int port, String data) throws RemoteException 
		{
			int res = mNetProto.udpSend(mSocketHandle, addr, mSocketPort, data, data.length());
			Log.w(TAG, "send addr:"+addr + " socketHandle:" + mSocketHandle + " res:"+res);
			return res;
		}

		@Override
		public boolean startUdpServer() throws RemoteException {
			// TODO Auto-generated method stub
			if(mSocketHandle>=0){
				Log.i(TAG, "UdpServer is running or don't close.");
				return false;
			}
			
			Log.i(TAG, "startUdpServer");
			
	        if(mNetProto.createSdk()>=0)
	        	mSocketHandle = mNetProto.creatUdpServer(mSocketPort);
	        else
	        	mNetProto.destroySdk();
	        
	        Log.w(TAG, "createSdk mSocketHandle:"+mSocketHandle);
	        
			return (mSocketHandle>=0)?true:false;
		}

		@Override
		public boolean stopUdpServer() throws RemoteException {
			// TODO Auto-generated method stub
			if(mSocketHandle>=0){
		    	mNetProto.closeUdpServer(mSocketHandle);
		    	mNetProto.destroySdk();
		    	mSocketHandle = -1;
		    	
		    	Log.i(TAG, "stopUdpServer");
		    	return true;
			}
			return (mSocketHandle>=0)?false:true;
		}
		
		@Override
		public boolean isUdpServerRunning() throws RemoteException {
			// TODO Auto-generated method stub
	    	Log.i(TAG, "isUdpServerRunning");
			return (mSocketHandle>=0)?true:false;
		}
    };//stub end
    
    
	/**
	 * static method to start service
	 * @param context
	 */
	public static void startService(Context context) 
	{
		Log.i(TAG, "startService");
		Intent intent = new Intent(context, ProtransService.class);
		intent.setAction(ACTION_START_SERVICE);
		context.startService(intent);
	}
	
	/**
	 * static method to stop service
	 * @param context
	 */
	public static void stopService(Context context) {
		Log.i(TAG, "stopService");
		Intent intent = new Intent(context, ProtransService.class);
		intent.setAction(ACTION_STOP_SERVICE);
		context.startService(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(TAG, "intent.getAction()="+intent.getAction());
		// option based on action
		if(intent.getAction()!=null)
		{
			if (intent.getAction().equals(ACTION_START_SERVICE) == true) {
				//startService();do on {@link #onCreat()}
			} else if (intent.getAction().equals(ACTION_STOP_SERVICE) == true) {
				stopSelf();
			} 
		}

		//重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		return Service.START_REDELIVER_INTENT;
	}
	
	//do that when app run first time
	@Override
	public void onCreate(){
		Log.i(TAG, "onCreate");
		super.onCreate();
	}
	
	@Override
	public void onDestroy(){
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}
	

	//do that when app run first time
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "return server onBind");
		return mBinder;
	}

	//do that if call unbindService in context
	@Override
    public boolean onUnbind(Intent intent){
        // All clients have unbound with unbindService()
		Log.i(TAG, "server onUnbind");
    	Intent localIntent = new Intent();
    	localIntent.setClass(this, ProtransService.class); // 
    	startService(localIntent);
		return super.onUnbind(intent);
    }
	
    @Override
    public void onRebind(Intent intent){
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    	Log.w(TAG,"onRebind"); 
        super.onRebind(intent);
    }
    
}

