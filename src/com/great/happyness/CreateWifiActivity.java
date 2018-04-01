package com.great.happyness;

import com.great.happyness.popwin.QRCodePopWin;
import com.great.happyness.utils.CompletedView;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateWifiActivity extends Activity implements OnClickListener{
    private int mTotalProgress   = 12;
    private int mCurrentProgress = 0;
    //进度条
    private CompletedView mTasksView;

	private final int MESSAGE_CREATE_BEGIN  = 0;
	private final int MESSAGE_CREATE_SUCC	= 1;

    private WifiUtils mWifiUtils;
    
    //
    private TextView  tvhot_state;
    private ImageView bar_goback;
    private QRCodePopWin mQrcodePopWin 	= null;
    
	private String TAG = getClass().getSimpleName();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_create_wifi);
        initBroadcastReceiver();
        mTasksView 		= (CompletedView) findViewById(R.id.tasks_view);
        tvhot_state 	= (TextView)findViewById(R.id.tvhot_state);
        tvhot_state.setOnClickListener(this);
        bar_goback		= (ImageView)findViewById(R.id.bar_goback);
        bar_goback.setOnClickListener(this);
        
        mWifiUtils = new WifiUtils(this);
		if (!mWifiUtils.isWifiApEnabled()) {
			tvhot_state.setText("SSID closeed");
		} else {
			tvhot_state.setText("SSID:" + SysConfig.WIFI_AP_SSID );
		}
        
        new Thread(new ProgressRunable()).start();
    }

    class ProgressRunable implements Runnable 
    {
        @Override
        public void run() 
        {
                while (mCurrentProgress < mTotalProgress) {
                    mCurrentProgress += 1;
                    mTasksView.setProgress(mCurrentProgress);
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        		Message message = new Message();   
                message.what = MESSAGE_CREATE_BEGIN;
                mHandler.sendMessage(message);
        }
    }
    
	@Override
	public void onResume() {
		super.onResume();
		//showPopFormBottom();
	}
    
	public Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case MESSAGE_CREATE_BEGIN:
					if(mWifiUtils.isWifiApEnabled())//判断热点是否打开
					{
		            	int pw = (int) (WiseApplication.SCREEN_WIDTH*0.8);
		            	int ph = (int) (WiseApplication.SCREEN_WIDTH*0.8);
		            	if(mQrcodePopWin==null)
		            		mQrcodePopWin = new QRCodePopWin(CreateWifiActivity.this, onClickListener, pw, ph);
		            	mQrcodePopWin.showAtLocation(findViewById(R.id.main_view), Gravity.CENTER, 0, 0);
		            	mQrcodePopWin.createQR(CreateWifiActivity.this, SysConfig.WIFI_AP_SSID, pw-50, ph-50);
		            	mTasksView.setProgress(100);
					}
					else
					{
						if(mWifiUtils.createWifiHotspot(SysConfig.WIFI_AP_SSID, SysConfig.WIFI_AP_KEY))
						{
			            	tvhot_state.setText("创建成功，等待连接");
			            	int pw = (int) (WiseApplication.SCREEN_WIDTH*0.8);
			            	int ph = (int) (WiseApplication.SCREEN_WIDTH*0.8);
			            	if(mQrcodePopWin==null)
			            	{
			            		mQrcodePopWin = new QRCodePopWin(CreateWifiActivity.this, onClickListener, pw, ph);
			            		if(!isFinishing())
			            		{
			            			mQrcodePopWin.showAtLocation(findViewById(R.id.main_view), Gravity.CENTER, 0, 0);
			            			mQrcodePopWin.createQR(CreateWifiActivity.this, 
			            				SysConfig.WIFI_AP_SSID, pw-50, ph-50);
			            		}
			            	}
			        		mTasksView.setProgress(100);
						}
					}
					break;
					
				case MESSAGE_CREATE_SUCC:
					break;
			}
		}
	};
	
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            }
        }
    };
    
    private void initBroadcastReceiver() 
    {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        //wifi ap打开和关闭的广播
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");

        registerReceiver(receiver, intentFilter);
    }
    
    @SuppressWarnings("static-access")
    private BroadcastReceiver receiver = new BroadcastReceiver() 
    {
		@Override
        public void onReceive(Context context, Intent intent) 
        {
            final String action = intent.getAction();
            Log.w(TAG, "action:"+action);
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) 
            {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.w(TAG, "WIFI_STATE_CHANGED_ACTION state:"+wifiState);
                switch (wifiState) 
                {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //获取到wifi开启的广播时，开始扫描
                    	//mWifiManager.startScan();
                        break;
                        
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
                        break;
                }
            }
            else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED"))
            {
            	//10---正在关闭；11---已关闭；12---正在开启；13---已开启
            	int state = intent.getIntExtra("wifi_state", 0);
            	Log.w(TAG, "wifi ap state:"+state);
            }
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
            {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.w(TAG, "WifiManager.NETWORK_STATE_CHANGED_ACTION state::"+info.getState());
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) 
                {
                	//tvhot_state.setText("连接已断开");
                }
                else 
                {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                    	tvhot_state.setText("连接中...");
                    }else if (state == state.CONNECTED) //奇怪了，这里会有连接成功的状态出来
                    {
                    }else if (state == state.AUTHENTICATING) {
                    	tvhot_state.setText("正在验证身份信息...");
                    } else if (state == state.OBTAINING_IPADDR) {
                    	tvhot_state.setText("正在获取IP地址...");
                    } else if (state == state.FAILED) {
                    	tvhot_state.setText("连接失败");
                    }
                }
            }
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        
        if(mQrcodePopWin!=null)
        	mQrcodePopWin.dismiss();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) 
        {
            case R.id.tvhot_state:
                break;
                
            case R.id.bar_goback:
                //关闭Activity
                Intent reintent = new Intent();
                //把返回数据存入Intent
                reintent.putExtra("result", mWifiUtils.isWifiApEnabled());
                //设置返回数据
                CreateWifiActivity.this.setResult(RESULT_OK, reintent);
                
                finish();
                Log.w(TAG, "bar_goback");
            	break;
        }
	}
    
}

