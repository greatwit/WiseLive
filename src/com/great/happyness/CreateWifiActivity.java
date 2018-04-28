package com.great.happyness;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.ui.popwin.QRCodePopWin;
import com.great.happyness.utils.CompletedView;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
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

public class CreateWifiActivity extends Activity implements OnClickListener
{
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_wifi_create);
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
        
        EventBus.getDefault().register(this);
    }

    class ProgressRunable implements Runnable {
        @Override
        public void run() {
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
			            		if(!isFinishing()) {
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
	
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CmdEvent event) {
        if (event != null) {
            Log.i(TAG, "onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
            if(event.getCmd() == ConstDef.CMD_CONNED_SYN)
            	finish();
        }else {
            System.out.println("event:"+event);
        }
    }
	
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            }
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(mQrcodePopWin!=null)
        	mQrcodePopWin.dismiss();
        
        EventBus.getDefault().unregister(this);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvhot_state:
                break;
                
            case R.id.bar_goback:
                finish();
                Log.w(TAG, "bar_goback");
            	break;
        }
	}
    
}

