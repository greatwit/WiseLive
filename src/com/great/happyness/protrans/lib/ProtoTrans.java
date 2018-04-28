package com.great.happyness.protrans.lib;


import com.great.happyness.service.ListenerManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.annotation.SuppressLint;


public class ProtoTrans
{
	private static ProtoTrans mInstance;
	
	private final int mKeepLivetime = 10000;
	
	static{
		System.loadLibrary("Protrans");
	}
	
	public void callbackByteArray(byte[] buffer){
		//String str=new String(buffer);
		Log.e("callback", "callback size = "+ buffer.length);
	}
	
	public void callbackString(String buffer){
		ListenerManager.Instance().sendString(1, buffer);
		Log.e("callback", "callback size = "+ buffer.length());
	}
	
	//////////////////////////////////////////////////timer////////////////////////////////////////////////////
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			}
		}
	};
	
	private Runnable getLiveDevice = new Runnable() 
	{
	    public void run() 
	    {
	    	mHandler.removeCallbacks(getLiveDevice);
	    }
	}; 
	
	private Runnable keepAlive = new Runnable() 
	{
	    public void run(){
	    	/*to-do*/
	    	mHandler.postDelayed(keepAlive, mKeepLivetime);
	    	//int res = sendKeepAlive(mSessionid);
	    }
	};
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	//native interface
	public native int createSdk();
	public native int destroySdk();
	
	public native int creatUdpServer(int bindport);
	public native int closeUdpServer(int socketId);
	public native int udpSend(int socketId, String destAddr, int destPort, String data, int datalen);

	//private static native int sendKeepAlive(int sessionId);
}

