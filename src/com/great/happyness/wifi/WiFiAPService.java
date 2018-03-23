package com.great.happyness.wifi;


import com.great.happyness.aidl.IServiceListen;
import com.great.happyness.aidl.IActivityReq;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * @ClassName:  WiFiAPService   
 * @Description:  wifi hot manage service
 * @author: jajuan.wang  
 * @date:   2015-06-09 00:43  
 * version:1.0.0
 */
public class WiFiAPService extends Service {
	
	private static String TAG = "WiFiAPService";
	
	public static String ACTION_START_SERVICE = "action_start_service";
	public static String ACTION_STOP_SERVICE = "action_stop_service";
	private static WiFiAPObserver wiFiAPObserver = new WiFiAPObserver();
	
    public static final int WIFI_ACTION_CONNECT 	= 255;
    public static final int WIFI_ACTION_DISCONNECT 	= 256;
	
    private RemoteCallbackList<IServiceListen> mListenerList = new RemoteCallbackList<IServiceListen>();
	
    Binder mBinder = new IActivityReq.Stub() {
        @Override
        public void action(int action, String datum) throws RemoteException {
            switch (action) {
	            case WIFI_ACTION_CONNECT:
	            	break;
	            case WIFI_ACTION_DISCONNECT:
	            	break;
            }
        }

        @Override
        public void registerListener(IServiceListen listener) throws RemoteException {
            if (listener != null) {
                mListenerList.register(listener);
            }

        }

        @Override
        public void unregisterListener(IServiceListen listener) throws RemoteException {
            if (listener != null) {
                mListenerList.unregister(listener);
            }
        }

    };
    
	/**
	 * static method to start service
	 * @param context
	 */
	public static void startService(Context context) 
	{
		Log.i(TAG, "WiFiAPService startService");
		Intent intent = new Intent(context, WiFiAPService.class);
		intent.setAction(ACTION_START_SERVICE);
		context.startService(intent);
	}
	
	/**
	 * static method to stop service
	 * @param context
	 */
	public static void stopService(Context context) {
		Log.i(TAG, "WiFiAPService stopService");
		Intent intent = new Intent(context, WiFiAPService.class);
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
	
	@Override
	public void onCreate() 
	{
		Log.i(TAG, "WiFiAPService onCreate");
		IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        mFilter.addAction("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
		
		registerReceiver(wifiReceiver,mFilter);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() 
	{
		unregisterReceiver(wifiReceiver);
		wiFiAPObserver.clearWiFiAPListener();
		super.onDestroy();
	}
	
	
    private void sendMessage(int action, Message msg) {
        try {
            final int N = mListenerList.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IServiceListen broadcastItem = mListenerList.getBroadcastItem(i);
                if (broadcastItem != null) {
                    broadcastItem.onAction(action, msg);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                mListenerList.finishBroadcast();
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e("Error while diffusing message to listener  finishBroadcast ", illegalArgumentException.toString());
            }
        }

    }
	
	public static void addWiFiAPListener(WiFiAPListener wiFiAPListener) {
		wiFiAPObserver.addWiFiAPListener(wiFiAPListener);
	}
	
	public static void removeWiFiAPListener(WiFiAPListener wiFiAPListener) {
		wiFiAPObserver.removeWiFiAPListener(wiFiAPListener);
	}


	private BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) 
        {
        	String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) 
            {
                int state = intent.getIntExtra("wifi_state",  0);
                Log.i(TAG, "state= "+state);
                wiFiAPObserver.stateChanged(state);
            }
        }
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
