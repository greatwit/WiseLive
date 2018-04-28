package com.great.happyness.service.aidl;

import com.great.happyness.protrans.message.CommandMessage;
import com.great.happyness.protrans.message.MessagesEntity;
import com.great.happyness.service.ProtransService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class ServiceControl implements ServiceConnection
{

	protected String TAG = getClass().getName();
	
	IActivityReq mActivityReq;
	//private static ArrayList<ServiceCreatedListen> mListenerActivity = new ArrayList<ServiceCreatedListen>();
	private static ServiceControl gServiceControl=null;
	
	private IBindListen mBindListen = null;
	
    //静态工厂方法   
    public static ServiceControl getInstance() {  
         if (gServiceControl == null) {    
        	 gServiceControl = new  ServiceControl();
         }    
        return gServiceControl;  
    }
    
	public ServiceControl() {
	}
    
	public void startService(Context cont, IBindListen listen)
	{
		Intent intentServer = new Intent(cont, ProtransService.class);  
		cont.startService(intentServer);
		mBindListen = listen;
	}
	
    public void bindService(Context cont) {
        Intent intent = new Intent(cont, ProtransService.class);
        cont.bindService(intent, getInstance(), Service.BIND_AUTO_CREATE);
        Log.w(TAG, "bindService");
    }
	
    public void unbindService(Context cont)
    {
    	cont.unbindService(getInstance());     //取消服务的绑定
    }
    
    
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mActivityReq = IActivityReq.Stub.asInterface(service);
		if(mBindListen!=null)
			mBindListen.onServiceConnected();
		Log.w(TAG, "onServiceConnected");
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		Log.w(TAG, "onServiceDisconnected");
	}
	
	//////////////////////////////////////////interface////////////////////////////////////////
	
	public IActivityReq getActivityReq()
	{
		return mActivityReq;
	}
	
	public boolean startUdpServer()
	{
		boolean bResult = false;
		try {
			if(mActivityReq!=null)
				bResult = mActivityReq.startUdpServer();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bResult;
	}

	public boolean stopUdpServer()
    {
		boolean bResult = false;
		try {
			if(mActivityReq!=null)
				bResult = mActivityReq.stopUdpServer();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bResult;
    }
    
	public boolean isUdpServerRunning()
	{
		boolean bResult = false;
		try {
			if(mActivityReq!=null)
				bResult = mActivityReq.isUdpServerRunning();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bResult;
	}
	
	public int sendData(String addr, int port, String data) {
		int bResult = -1;
		try {
			if(mActivityReq!=null)
				bResult = mActivityReq.sendData(addr, port, data);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bResult;
	}
	
	public int sendCmd(String addr, int port, int cmd) {
		int bResult = -1;
		try {
			if(mActivityReq!=null){
		        CommandMessage comm = MessagesEntity.getInst().getCommandMessage();
		        String msg = comm.encodeData(cmd);
				bResult = mActivityReq.sendData(addr, port, msg);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bResult;
	}
	
}

