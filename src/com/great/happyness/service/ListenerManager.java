package com.great.happyness.service;

import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.great.happyness.aidl.IServiceListen;


public class ListenerManager {
	protected String TAG = getClass().getName();
	
	private static ListenerManager gInstance=null; 
	
	private static RemoteCallbackList<IServiceListen> mListenerList = new RemoteCallbackList<IServiceListen>();
    //静态工厂方法   
    public static ListenerManager Instance() {  
         if (gInstance == null) {    
        	 gInstance = new  ListenerManager();
         }    
        return gInstance;  
    }
    

    public void register(IServiceListen listener) throws RemoteException {
        if (listener != null) {
            mListenerList.register(listener);
            Log.i(TAG, "registerListener:"+listener);
        }
    }

    public void unregister(IServiceListen listener) throws RemoteException {
        if (listener != null) {
            mListenerList.unregister(listener);
            Log.i(TAG, "unregisterListener:"+listener);
        }
    }
    
    public void sendMessage(int action, Message msg) {
    	final int N = mListenerList.beginBroadcast();
    	if(N<=0)return;
    	
        try {
            for (int i = 0; i < N; i++) {
                IServiceListen broadcastItem = mListenerList.getBroadcastItem(i);
                if (broadcastItem != null) {
                    broadcastItem.onAction(action, msg);
                    Log.w(TAG, "broadcastItem:"+action);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                mListenerList.finishBroadcast();
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e(TAG, illegalArgumentException.toString());
            }
        }
    }
    
}