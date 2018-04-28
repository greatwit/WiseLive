package com.great.happyness;

import java.io.IOException;
import java.util.Vector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.ui.popwin.ProcessBarPopWin;
import com.great.happyness.ui.qrcode.CameraManager;
import com.great.happyness.ui.qrcode.CaptureActivityHandler;
import com.great.happyness.ui.qrcode.InactivityTimer;
import com.great.happyness.ui.qrcode.ViewfinderView;
import com.great.happyness.ui.radar.RandomTextView;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ConnectWifiActivity extends Activity implements OnClickListener, Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private Vector<BarcodeFormat> decodeFormats = null;
	private String characterSet					= null;
	private InactivityTimer inactivityTimer;

	private ImageButton bt_qr_capture 		= null;
	private ImageView bar_goback;
	private LinearLayout radar_ll 			= null;
	private LinearLayout  qr_capture_ll 	= null;
	ProcessBarPopWin processBarPopWin 		= null;
	
	
	private WifiUtils mWifiUtils;
	private String TAG = getClass().getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_wifi_connect);
		
        initRadar();
        initQRCapture();
        initBroadcastReceiver();
       
        mWifiUtils = new WifiUtils(this);
        Log.w(TAG,"wifi enable:"+mWifiUtils.isWifiEnable());
        if(!mWifiUtils.isWifiEnable())
        	mWifiUtils.setWifiEnabled(true);
        else
        {
        	if(mWifiUtils.isWifiConnected())
        	{
        		Log.w(TAG,"wifi connected:"+true);
        		WifiInfo info = mWifiUtils.getConnectionInfo();
        		Log.w(TAG,"wifi ssid:"+info.getSSID()+ " ip:"+mWifiUtils.getWifiIp());
        	}
        	else
        		Log.w(TAG,"wifi connected:"+false);
        }
        
        EventBus.getDefault().register(this);
	}

    void initRadar() {
        bt_qr_capture 	= (ImageButton)findViewById( R.id.bt_qr_capture);
        bt_qr_capture.setOnClickListener(this);
        bar_goback		= (ImageView)findViewById(R.id.bar_goback);
        bar_goback.setOnClickListener(this);
        
        radar_ll 		= (LinearLayout)findViewById( R.id.radar_ll);
        qr_capture_ll 	= (LinearLayout)findViewById( R.id.qr_capture_ll);
        
        final RandomTextView randomTextView = (RandomTextView) findViewById( R.id.random_textview);
        randomTextView.setOnRippleViewClickListener(
        new RandomTextView.OnRippleViewClickListener()
        {
            @Override
            public void onRippleViewClicked(View view)
            {
            	Toast.makeText(ConnectWifiActivity.this, "你好!", Toast.LENGTH_LONG).show();
            }
        });

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                randomTextView.addKeyWord("彭丽媛");
                randomTextView.addKeyWord("习近平");
                randomTextView.addKeyWord("曾伟鹏");
                randomTextView.show();
            }
        }, 2 * 1000);
    }
    
    @SuppressWarnings("deprecation")
	void initQRCapture() {
		// 初始化 CameraManager
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		inactivityTimer = new InactivityTimer(this);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        registerReceiver(wifirecv, intentFilter);
    }
    
	public void handleDecode(final Result obj, Bitmap barcode) {
		
		inactivityTimer.onActivity();

		String resultString = obj.getText();
		Log.w(TAG, "Got decode succeeded resultString:"+resultString);
		if (resultString.equals("")) {
			Toast.makeText(ConnectWifiActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		} 
		else 
		{
			//if(!mWifiUtils.isWifiEnable())
			if (mWifiUtils.isWifiApEnabled())
				mWifiUtils.destroyWifiHotspot();
			
			mWifiUtils.setWifiEnabled(true);
			mWifiUtils.wifiConnect(resultString, SysConfig.WIFI_AP_KEY, WifiUtils.WIFICIPHER_WPA);
	    	if(processBarPopWin==null)
	    	{
	        	int pw = (int) (WiseApplication.SCREEN_WIDTH*0.3);
	        	int ph = (int) (WiseApplication.SCREEN_WIDTH*0.8);
	    		processBarPopWin = new ProcessBarPopWin(this, pw, ph);
	    		processBarPopWin.showAtLocation(findViewById(R.id.radar_ll), Gravity.CENTER, 0, 0);
	    	}
		}
		//finish();
	}
    
    @SuppressWarnings("static-access")
    private BroadcastReceiver wifirecv = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) 
        {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) //wifi已成功扫描到可用wifi
            {
                Log.w(TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
            } 
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) //wifi状态
            {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.w(TAG, "WifiManager.WIFI_STATE_CHANGED_ACTION wifistate:" + wifiState);
                switch (wifiState) 
                {
                    case WifiManager.WIFI_STATE_ENABLED://获取到wifi开启的广播
                        break;
                        
                    case WifiManager.WIFI_STATE_DISABLED://wifi关闭发出的广播
                        break;
                }
            } 
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
            {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.w(TAG, "WifiManager.NETWORK_STATE_CHANGED_ACTION state:"+info.getState());
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) //连接已断开
                {
        	    	if(processBarPopWin!=null)
        	    		processBarPopWin.setState("连接已断开");
                } 
                else if (info.getState().equals(NetworkInfo.State.CONNECTED)) //已连接到网络
                {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Log.w(TAG,"wifiInfo.getSSID():"+wifiInfo.getSSID());
        	    	if(processBarPopWin!=null)
        	    	{
        	    		processBarPopWin.setState("已连接到网络");
        	    		ServiceControl servCont = ServiceControl.getInstance();
        	    		servCont.sendCmd(mWifiUtils.getDestAddr(), 
    		        			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CONNED_SYN);
        	    	}
                }
                else 
                {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING)//连接中
                    {
            	    	if(processBarPopWin!=null)
            	    		processBarPopWin.setState("连接中");
                    }
                    else if (state == state.CONNECTED)
                    {
            	    	if(processBarPopWin!=null)
            	    		processBarPopWin.setState("连接成功");
                        
                    }
                    else if (state == state.AUTHENTICATING)//正在验证身份信息 
                    {
            	    	if(processBarPopWin!=null)
            	    		processBarPopWin.setState("正在验证身份信息");
                    } else if (state == state.OBTAINING_IPADDR)//正在获取IP地址 
                    {
            	    	if(processBarPopWin!=null)
            	    		processBarPopWin.setState("正在获取IP地址");
                    } else if (state == state.FAILED)//连接失败 
                    {
            	    	if(processBarPopWin!=null)
            	    		processBarPopWin.setState("连接失败 ");
                    }
                }
            }
            else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) 
            {
                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))//连接已断开 
                {
                    //wifiManager.removeNetwork(wcgID);
                } else {//已连接到网络
                }
            }
        }
    };
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CmdEvent event) {
        if (event != null) {
        	Log.i(TAG, "onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
            if(event.getCmd() == ConstDef.CMD_CONNED_ACK)
            	finish();
        }
        else 
            System.out.println("event:"+event);
    }
    
	@Override
	protected void onResume() 
	{
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		if(processBarPopWin!=null)
		{
			processBarPopWin.dismiss();
			processBarPopWin = null;
		}
		unregisterReceiver(wifirecv);
		
		EventBus.getDefault().unregister(this);
		
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
			initCamera(holder);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.bt_qr_capture:
				//startActivity(new Intent().setClass(QrCaptureActivity.this, QrCaptureActivity.class));
				radar_ll.setVisibility(View.GONE);
				qr_capture_ll.setVisibility(View.VISIBLE);
				break;
				
			case R.id.bar_goback:
				finish();
				break;
		}
	}
	
}

