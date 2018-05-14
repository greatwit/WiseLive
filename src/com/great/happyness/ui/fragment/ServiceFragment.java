package com.great.happyness.ui.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.great.happyness.R;
import com.great.happyness.CameraRecvActivity;
import com.great.happyness.CaptureCameraActivity;
import com.great.happyness.ConnectWifiActivity;
import com.great.happyness.CreateWifiActivity;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.ui.bannerView.BannerViewPager;
import com.great.happyness.ui.bannerView.OnPageClickListener;
import com.great.happyness.ui.bannerView.ViewPagerAdapter;
import com.great.happyness.ui.popwin.CameraPopwin;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
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
		implements OnClickListener, OnPageClickListener
{
	private Context mContext;
	private final String TAG = ServiceFragment.class.getSimpleName();
	private View view;
	private LinearLayout item_create_ll, item_connect_ll, item_camera_ll, wifi_state_ll;
	
	
	private ImageView bar_recv, bar_send, bar_delete;
	private TextView  bar_status;
	private BannerViewPager bannerViewPager;
	private ViewPagerAdapter mAdapter;
	
	private WifiUtils mWifiUtils;
	private CameraPopwin mCamPopwin  = null;
	
	private ServiceControl mServCont = ServiceControl.getInstance();
	private boolean mbUdpServerStarted 	= false;
	
	private final static float BEEP_VOLUME = 0.10f;
	private MediaPlayer mediaPlayer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = getActivity();
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("InflateParams") 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		view = LayoutInflater.from(mContext).inflate(R.layout.fragment_service, null);
		initView();
		initBeepSound();
		
		mWifiUtils = new WifiUtils(mContext);
		checkWifiStatus();

		EventBus.getDefault().register(this);
		return view;
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CmdEvent event) {
        if (event != null) {
	        int cmd = event.getCmd();
	        switch(cmd) {
		        case ConstDef.CMD_CONNED_SYN://连接客服端发来连接成功通知
		        	playBeepSoundAndVibrate();
		        	mServCont.sendCmd(mWifiUtils.getDestAddr(), 
		        			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CONNED_ACK);
		        	checkWifiStatus();
		        	break;
		        case ConstDef.CMD_CONNED_ACK://服务端回连接
		        	playBeepSoundAndVibrate();
		        	checkWifiStatus();
		        	break;
		        	
		        case ConstDef.CMD_SHAKE_SYN://触动，双方都可以主动发送
		        	playBeepSoundAndVibrate();
		        	mServCont.sendCmd(mWifiUtils.getDestAddr(), 
		        			SysConfig.UDP_BIND_PORT, ConstDef.CMD_SHAKE_ACK);
		        	break;
		        case ConstDef.CMD_SHAKE_ACK://收到回触动命令
		        	playBeepSoundAndVibrate();
		        	break;
		        	
		        case ConstDef.CMD_CAMOPEN_SYN:
		        	startActivity(new Intent().setClass(mContext, CaptureCameraActivity.class));
		        	mServCont.sendCmd(mWifiUtils.getDestAddr(), 
		        			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMOPEN_ACK);
		        	break;
		        case ConstDef.CMD_CAMOPEN_ACK:
		        	break;
		        	
		        case ConstDef.UI_SER_CONNED:
		    		if(checkWifiStatus() && mbUdpServerStarted==false) {
		    			mbUdpServerStarted = mServCont.startUdpServer();
		    		}
		        	break;
	        }
            Log.i(TAG, "onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
        }else {
            System.out.println("event:"+event);
        }
    }
	
	private void initView() {
		item_create_ll = (LinearLayout) view.findViewById(R.id.item_create_ll);
		item_create_ll.setOnClickListener(this);
		
		item_connect_ll = (LinearLayout) view.findViewById(R.id.item_connect_ll);
		item_connect_ll.setOnClickListener(this);
		
		item_camera_ll = (LinearLayout) view.findViewById(R.id.item_camera_ll);
		item_camera_ll.setOnClickListener(this);
		
		wifi_state_ll = (LinearLayout) view.findViewById(R.id.wifi_state_ll);
		
		bar_recv 	= (ImageView) view.findViewById(R.id.bar_recv);
		bar_send 	= (ImageView) view.findViewById(R.id.bar_send);
		bar_delete 	= (ImageView) view.findViewById(R.id.bar_delete);
		bar_recv.setOnClickListener(this);
		bar_send.setOnClickListener(this);
		bar_delete.setOnClickListener(this);
		
		bannerViewPager = (BannerViewPager) view.findViewById(R.id.banner);
        //一开始只添加5个Item
        final List<View> mViews = new ArrayList<View>();
        int[] images = { R.mipmap.ic_img01, R.mipmap.ic_img02, R.mipmap.ic_img03, 
        				R.mipmap.ic_img04, R.mipmap.ic_img05 };
        
        for(int i=0; i<images.length; i++) {
            final ImageView iv = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.banner_item, bannerViewPager,false);
            iv.setImageResource(images[i]);
            mViews.add(iv);
        }

        mAdapter = new ViewPagerAdapter(mViews, this);
        bannerViewPager.setAdapter(mAdapter);
        
		bar_status	= (TextView) view.findViewById(R.id.bar_status);
	}

	private void initBeepSound() {
		if ( mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,so we now play on the music stream.
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
	
	private void playBeepSoundAndVibrate(){
		AudioManager aum = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		if (aum.getRingerMode() != AudioManager.RINGER_MODE_SILENT 
				&& mediaPlayer != null) {
			mediaPlayer.start();
	    }
		
		Vibrator vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
		long [] pattern = { 0, 180, 180, 180};   // {100,400,100,400} 停止 开启 停止 开启   
        vibrator.vibrate(pattern, -1);
	}
	
	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private boolean checkWifiStatus() {
		boolean bResult = false;
		if (!mWifiUtils.isWifiApEnabled()) {
			wifi_state_ll.setVisibility(View.GONE);
		}
		else
		{
			wifi_state_ll.setVisibility(View.VISIBLE);

			bar_recv.setVisibility(View.VISIBLE);
			bar_recv.setImageDrawable(getResources().getDrawable(R.drawable.camera));
			
			ArrayList<String> array = mWifiUtils.getConnectedIP();
			int consize = array.size();
			Log.w(TAG,"connected array:"+consize);
			if(consize>0) {
				bar_send.setVisibility(View.VISIBLE);
				bResult = true;
			}
			for(int i=0; i<consize; i++)
				Log.w(TAG,"connected info:"+array.get(i));
			Log.w(TAG,"local spot ip:"+mWifiUtils.getGateWayIpAddress());
		}
		if(mWifiUtils.isWifiEnable()){
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
				if(apen.equals("WISE")) {
					bar_send.setVisibility(View.VISIBLE);
					bResult = true;
				}
				else
					wifi_state_ll.setVisibility(View.GONE);
				Log.w(TAG,"connected wifi ip:"+mWifiUtils.getGateWayIpAddress());
			}
		}
		return bResult;
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.item_create_ll:
				startActivity(new Intent().setClass(mContext, CreateWifiActivity.class));
				if(mbUdpServerStarted == false)
					mbUdpServerStarted = mServCont.startUdpServer();
				break;
				
			case R.id.item_connect_ll://ConnectWifiActivity
				if(mbUdpServerStarted == false)
					mbUdpServerStarted = mServCont.startUdpServer();
				startActivity(new Intent().setClass(mContext, ConnectWifiActivity.class));
				break;
			
			case R.id.item_camera_ll: //打开单机照相机
				intent = new Intent().setClass(mContext, CaptureCameraActivity.class);
				startActivity(intent);
				break;
				
			case R.id.bar_recv:
				startActivity(new Intent().setClass(mContext, CreateWifiActivity.class));
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
				mServCont.sendCmd(mWifiUtils.getDestAddr(), 
						SysConfig.UDP_BIND_PORT, ConstDef.CMD_SHAKE_SYN);
				Log.i(TAG, "layout_touch");
                break;
                
            case R.id.layout_camera:
            	startActivity(new Intent().setClass(mContext, CameraRecvActivity.class));
				mServCont.sendCmd(mWifiUtils.getDestAddr(), 
						SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMOPEN_SYN);
				Log.i(TAG, "layout_camera");
                break;
				
			case R.id.bar_delete:
				if(mWifiUtils.isWifiApEnabled())
					mWifiUtils.destroyWifiHotspot();
				wifi_state_ll.setVisibility(View.GONE);

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
		if(mbUdpServerStarted){
			mServCont.stopUdpServer();
			mbUdpServerStarted = false;
			Log.w(TAG,"stopUdpServer");
		}
		
		EventBus.getDefault().unregister(this);
		
//		if(!mWifiUtils.isWifiEnable())
//			mWifiUtils.setWifiEnabled(true);
		
		super.onDestroy();
	}

	@Override
	public void onPageClick(View view, int position) {
		// TODO Auto-generated method stub
		
	}

}

