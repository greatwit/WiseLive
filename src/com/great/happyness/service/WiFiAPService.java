package com.great.happyness.service;


import java.net.InetAddress;

import com.great.happyness.aidl.IServiceListen;
import com.great.happyness.aidl.IActivityReq;
import com.great.happyness.network.CameraReqPack;
import com.great.happyness.network.ProtocolEngine;
import com.great.happyness.network.UdpPackageInfo;
import com.great.happyness.wifi.WiFiAPListener;
import com.great.happyness.wifi.WiFiAPObserver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @ClassName:  WiFiAPService   
 * @Description:  wifi hot manage service
 * @author: jajuan.wang  
 * @date:   2015-06-09 00:43  
 * version:1.0.0
 */
public class WiFiAPService extends Service 
{
	
	private static String TAG = WiFiAPService.class.getSimpleName();
	
	public static String ACTION_START_SERVICE 		= "action_start_service";
	public static String ACTION_STOP_SERVICE 		= "action_stop_service";
	private static WiFiAPObserver wiFiAPObserver 	= new WiFiAPObserver();
	
	public static String FUNC_START_UDP_ENGINE 		= "StartUdpEngine";
	public static String FUNC_STOP_UDP_ENGINE 		= "StopUdpEngine";
	
    public static final int WIFI_CMD 	= 121;
    public static final int NET_CMD 	= 122;
	
    private ProtocolEngine mProtoEngine = ProtocolEngine.getInstance();
    
    private CameraReqPack mCamPack = new CameraReqPack();
    
    Binder mBinder = new IActivityReq.Stub() {
        @Override
        public void action(int action, String datum) throws RemoteException {
        	Log.i(TAG, "action:"+action + " datum:"+datum);
            switch (action) {
	            case WIFI_CMD:
	            	break;
	            	
	            case NET_CMD:
	            	if(datum.equals(FUNC_START_UDP_ENGINE))
	            	{
	            		mProtoEngine.StartEngine();
	            	}else if(datum.equals(FUNC_STOP_UDP_ENGINE))
	            	{
	            		mProtoEngine.StopEngine();
	            	}
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
			// TODO Auto-generated method stub
			byte[] sendbyte = mCamPack.encodePack(data);
			Log.w(TAG, "leng:"+ sendbyte.length + " sendbyte:"+sendbyte);
			
			int res = 0;
			try {
				//res = mProtoEngine.SendData(addr, port, sendbyte);
				UdpPackageInfo pack = new UdpPackageInfo();
				pack.setData(sendbyte);
				pack.setFromAddr(InetAddress.getByName(addr));
				pack.setPort(port);
				mProtoEngine.SendPack(pack);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}

		@Override
		public boolean startUdpServer() throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(TAG, "startUdpServer");
			return mProtoEngine.StartEngine();
		}

		@Override
		public boolean stopUdpServer() throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(TAG, "stopUdpServer");
			return mProtoEngine.StopEngine();
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
		//wifi连接消息
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        //热点wifi消息
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
                Log.i(TAG, "wifi state= "+state);
                //Message msg = new Message();
                //ListenerManager.Instance().sendMessage(WIFI_CMD, msg);
                //wiFiAPObserver.stateChanged(state);
            }
        }
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "return server onBind");
		return mBinder;
	}

	@Override
    public boolean onUnbind(Intent intent)
    {
        // All clients have unbound with unbindService()
		Log.i(TAG, "server onUnbind");
    	Intent localIntent = new Intent();
    	localIntent.setClass(this, WiFiAPService.class); // 
    	startService(localIntent);
		return super.onUnbind(intent);
    }
	
    @Override
    public void onRebind(Intent intent)
    {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    	Log.w(TAG,"LocalService ->onRebind"); 
        super.onRebind(intent);
    }
    
}

