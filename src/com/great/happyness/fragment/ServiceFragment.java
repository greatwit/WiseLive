package com.great.happyness.fragment;

import java.io.IOException;
import java.util.ArrayList;

import com.great.happyness.CameraSenderActivity;
import com.great.happyness.ConnectWifiActivity;
import com.great.happyness.CreateWifiActivity;
import com.great.happyness.R;
import com.great.happyness.RtcCameraActivity;

import com.great.happyness.aidl.IActivityReq;
import com.great.happyness.aidl.IServiceListen;
import com.great.happyness.aidl.ServiceControl;
import com.great.happyness.popwin.CameraPopwin;
import com.great.happyness.service.ServiceCreatedListen;
import com.great.happyness.service.WiFiAPService;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 视频播放的Fragment
 * 
 * @author 
 * 
 */
@SuppressWarnings("deprecation")
public class ServiceFragment extends Fragment
		implements OnClickListener, ServiceCreatedListen
{
	private Context mContext;
	private final String TAG = ServiceFragment.class.getSimpleName();
	private View view;
	private LinearLayout item_create_ll, item_connect_ll, wifi_state_ll;
	
	private final static int CREATE_GREQUEST_CODE 	= 1;
	private final static int CONNECT_GREQUEST_CODE 	= 2;
	
	private ImageView bar_recv, bar_send, bar_delete;
	private TextView  bar_status;
	
	private WifiUtils mWifiUtils;
	private boolean mServiceRegisted = false;
	private CameraPopwin mCamPopwin = null;
	
	private MediaPlayer mediaPlayer;
	private static final float BEEP_VOLUME = 0.10f;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = getActivity();
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = LayoutInflater.from(mContext).inflate(R.layout.fragment_service, null);
		init();
		
		mWifiUtils = new WifiUtils(mContext);
		checkWifiStatus();
		initBeepSound();

		return view;
	}


	private void checkWifiStatus()
	{
		if (!mWifiUtils.isWifiApEnabled()) {
			wifi_state_ll.setVisibility(View.GONE);
		}else
		{
			wifi_state_ll.setVisibility(View.VISIBLE);

			bar_recv.setVisibility(View.VISIBLE);
			bar_recv.setImageDrawable(getResources().getDrawable(R.drawable.camera));
			
			ArrayList<String> array = mWifiUtils.getConnectedIP();
			int consize = array.size();
			Log.w(TAG,"connected array:"+consize);
			if(consize>0)
				bar_send.setVisibility(View.VISIBLE);
			for(int i=0; i<consize; i++)
				Log.w(TAG,"connected info:"+array.get(i));
			Log.w(TAG,"local spot ip:"+mWifiUtils.getGateWayIpAddress());
		}
		if(mWifiUtils.isWifiEnable())
		{
			if(mWifiUtils.isWifiConnected())
			{
				wifi_state_ll.setVisibility(View.VISIBLE);
				
				WifiInfo info = mWifiUtils.getConnectionInfo();
				String apen = "";
				if(info!=null&&info.getSSID().length()>6)
				{
					bar_status.setText(info.getSSID().substring(6, info.getSSID().length()-1));
					apen = info.getSSID().substring(1, 5);
				}
				if(apen.equals("WISE"))
					bar_send.setVisibility(View.VISIBLE);
				else
					wifi_state_ll.setVisibility(View.GONE);
				Log.w(TAG,"connected wifi ip:"+mWifiUtils.getGateWayIpAddress());
			}
		}
	}
	
	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private void initBeepSound() {
		if ( mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			//setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}
	
	private void playBeepSoundAndVibrate() 
	{
		AudioManager aum = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		if (aum.getRingerMode() != AudioManager.RINGER_MODE_SILENT && mediaPlayer != null) 
	    {
			//mediaPlayer.start();
	    }
		
		Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		long [] pattern = { 0, 180, 180, 180};   // {100,400,100,400} 停止 开启 停止 开启   
        vibrator.vibrate(pattern, -1);
	}
	
	private IActivityReq getBinderReq()
	{
		IActivityReq actReq = ServiceControl.getInstance().getActivityReq();
		if(actReq!=null && mServiceRegisted == false)
		{
			try {
				actReq.registerListener(mServListener);
				mServiceRegisted = true;
				Log.i(TAG, "mActReq.registerListener");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else
		{
			Log.e(TAG, "mActReq == null");
			ServiceControl.getInstance().registListener(this);
		}
		return actReq;
	}
	
	@Override
	public void serviceChanged(int state) {
		// TODO Auto-generated method stub
	}
	
	private void init() 
	{
		item_create_ll = (LinearLayout) view.findViewById(R.id.item_create_ll);
		item_create_ll.setOnClickListener(this);
		
		item_connect_ll = (LinearLayout) view.findViewById(R.id.item_connect_ll);
		item_connect_ll.setOnClickListener(this);
		
		wifi_state_ll = (LinearLayout) view.findViewById(R.id.wifi_state_ll);
		
		bar_recv 	= (ImageView) view.findViewById(R.id.bar_recv);
		bar_send 	= (ImageView) view.findViewById(R.id.bar_send);
		bar_delete 	= (ImageView) view.findViewById(R.id.bar_delete);
		bar_recv.setOnClickListener(this);
		bar_send.setOnClickListener(this);
		bar_delete.setOnClickListener(this);
		
		bar_status	= (TextView) view.findViewById(R.id.bar_status);
	}

    IServiceListen mServListener = new IServiceListen.Stub() {
		@Override
		public void onAction(int action, Message msg) throws RemoteException {
			// TODO Auto-generated method stub
			Log.i(TAG, "IServiceListen onAction:"+action);
			switch(action)
			{
				case WiFiAPService.WIFI_CMD:
					break;
					
				case WiFiAPService.NET_CMD:
					byte[] data = msg.getData().getByteArray("data");
					switch(data[14])
					{
						case 'a':
							playBeepSoundAndVibrate();
							break;
							
						case 'b':
							break;
					}
					break;
					
					default:
						break;
			}
		}
    };
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		Log.w(TAG, "onActivityResult:" + requestCode + " " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) 
		{
			case CREATE_GREQUEST_CODE://创建返回
				wifi_state_ll.setVisibility(View.VISIBLE);
				if (resultCode == Activity.RESULT_OK) 
				{
					Bundle bundle = data.getExtras();

					boolean result = bundle.getBoolean("result");
					Log.w(TAG, "onActivityResult content:" + result);
					if(result)
						checkWifiStatus();
				}
				else
				{
					bar_status.setText("创建失败");
				}
				break;
				
			case CONNECT_GREQUEST_CODE://连接返回
				wifi_state_ll.setVisibility(View.VISIBLE);
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getExtras();
					String content = bundle.getString("result");
					Log.w(TAG, "onActivityResult content:" + content);
					WifiInfo info = mWifiUtils.getConnectionInfo();
					bar_status.setText(info.getSSID().substring(6, info.getSSID().length()-1));
					
					wifi_state_ll.setVisibility(View.VISIBLE);
					bar_recv.setVisibility(View.GONE);
					bar_send.setVisibility(View.VISIBLE);
					
					try {
						if(getBinderReq()!=null)
						{
							getBinderReq().startUdpServer();
							getBinderReq().sendData("192.168.43.1", SysConfig.UDP_TALK_PORT, "a");
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					bar_status.setText("连接失败");
				break;
		}
	}
	
	@Override
	public void onClick(View v) {
		String addr = "";
		switch (v.getId()) {
			case R.id.item_create_ll:
				startActivityForResult(new Intent().setClass(mContext, CreateWifiActivity.class), CREATE_GREQUEST_CODE);
				try {
					if(getBinderReq()!=null)
						getBinderReq().startUdpServer();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case R.id.item_connect_ll://ConnectWifiActivity
				startActivityForResult(new Intent().setClass(mContext, RtcCameraActivity.class), CONNECT_GREQUEST_CODE);
				break;
	
			case R.id.bar_recv:
				startActivityForResult(new Intent().setClass(mContext, CreateWifiActivity.class), CREATE_GREQUEST_CODE);
				break;
				
			case R.id.bar_send:
				int[] location = new int[2];  
				bar_send.getLocationOnScreen(location);  
				
				if (mCamPopwin == null) { 
				    mCamPopwin = new CameraPopwin(getActivity(), this, 180, 80);  
				    //监听窗口的焦点事件，点击窗口外面则取消显示  
				    mCamPopwin.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {  
				          
				        @Override  
				        public void onFocusChange(View v, boolean hasFocus) {  
				            if (!hasFocus) {  
				            	mCamPopwin.dismiss();  
				            }  
				        }  
				    });  
				}  
				//设置默认获取焦点  
				mCamPopwin.setFocusable(true);  
				//以某个控件的x和y的偏移量位置开始显示窗口  
				mCamPopwin.showAsDropDown(bar_send);  
				//如果窗口存在，则更新  
				mCamPopwin.update(); 
				break;
				
            case R.id.layout_touch:  
            	Log.i(TAG, "layout_touch");
            	if (mWifiUtils.isWifiApEnabled()){
            		ArrayList<String> strArr = mWifiUtils.getConnectedIP();
            		addr = strArr.get(0);
            		Log.i(TAG, "connected wifi addr:"+addr);
            	}else
            	{
            		addr = "192.168.43.1";
            	}
				try {
					getBinderReq().sendData(addr, SysConfig.UDP_TALK_PORT, "a");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                break;
                
            case R.id.layout_camera:
            	if (mWifiUtils.isWifiApEnabled()){
            		ArrayList<String> strArr = mWifiUtils.getConnectedIP();
            		addr = strArr.get(0);
            		Log.i(TAG, "connected wifi addr:"+addr);
            	}else
            	{
            		addr = "192.168.43.1";
            	}
				try {
					getBinderReq().sendData(addr, SysConfig.UDP_TALK_PORT, "b");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				String destip = "";
//				if(mWifiUtils.isWifiEnable())
//					destip = mWifiUtils.getGateWayIpAddress();
//				else
//				{
//					ArrayList<String> array = mWifiUtils.getConnectedIP();
//					int consize = array.size();
//					if(consize>0)
//						destip = array.get(0);
//				}
//				Intent intent = new Intent().setClass(mContext, WebrtcActivity.class);
//				intent.putExtra("destip", destip);
//				startActivity(intent);
                break; 
				
			case R.id.bar_delete:
				if(mWifiUtils.isWifiApEnabled())
					mWifiUtils.destroyWifiHotspot();
				wifi_state_ll.setVisibility(View.GONE);
				try {
					if(getBinderReq()!=null)
						getBinderReq().stopUdpServer();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			default:
				break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		Log.w(TAG,"onDestroy");
		if (mWifiUtils.isWifiApEnabled())
		{
			String predex = mWifiUtils.getWifiHotspotSSID().substring(0,4);
			Log.w(TAG,"isWifiApEnabled:"+predex);

//			if(predex.equals(SysConfig.WIFI_AP_PREFIX))
//			mWifiUtils.closeWifiHotspot();
		}
		
//		if(!mWifiUtils.isWifiEnable())
//			mWifiUtils.setWifiEnabled(true);
		
		if(getBinderReq()!=null && mServiceRegisted)
		try {
			getBinderReq().unregisterListener(mServListener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		super.onDestroy();
	}


}

