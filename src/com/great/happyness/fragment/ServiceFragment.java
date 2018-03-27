package com.great.happyness.fragment;

import java.util.ArrayList;

import com.great.happyness.CreateWifiActivity;
import com.great.happyness.R;
import com.great.happyness.ConnectWifiActivity;
import com.great.happyness.WebrtcActivity;

import com.great.happyness.aidl.IActivityReq;
import com.great.happyness.aidl.IServiceListen;
import com.great.happyness.aidl.ServiceControl;
import com.great.happyness.service.WiFiAPService;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
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
public class ServiceFragment extends Fragment
		implements OnClickListener
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
	IActivityReq mActReq = null;
	
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
		
		mActReq = ServiceControl.getInstance().getActivityReq();
		if(mActReq!=null)
		try {
			mActReq.registerListener(mServListener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return view;
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

					wifi_state_ll.setVisibility(View.VISIBLE);
					bar_recv.setVisibility(View.VISIBLE);
					bar_recv.setImageDrawable(getResources().getDrawable(R.drawable.camera));
					if(result)
						bar_send.setVisibility(View.VISIBLE);
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
				}
				else
					bar_status.setText("连接失败");
				break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.item_create_ll:
				//startActivityForResult(new Intent().setClass(mContext, CreateWifiActivity.class), CREATE_GREQUEST_CODE);
				try {
					mActReq.action(WiFiAPService.NET_CMD, WiFiAPService.ACTION_START_SERVICE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case R.id.item_connect_ll:
				startActivityForResult(new Intent().setClass(mContext, ConnectWifiActivity.class), CONNECT_GREQUEST_CODE);
				break;
	
			case R.id.bar_recv:
				startActivityForResult(new Intent().setClass(mContext, CreateWifiActivity.class), CREATE_GREQUEST_CODE);
				break;
				
			case R.id.bar_send:
				String destip = "";
				if(mWifiUtils.isWifiEnable())
					destip = mWifiUtils.getGateWayIpAddress();
				else
				{
					ArrayList<String> array = mWifiUtils.getConnectedIP();
					int consize = array.size();
					if(consize>0)
						destip = array.get(0);
				}
				Intent intent = new Intent().setClass(mContext, WebrtcActivity.class);
				intent.putExtra("destip", destip);
				startActivity(intent);
				break;
				
			case R.id.bar_delete:
				if(mWifiUtils.isWifiApEnabled())
					mWifiUtils.closeWifiHotspot();
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
//				mWifiUtils.closeWifiHotspot();
		}
		
//		if(!mWifiUtils.isWifiEnable())
//			mWifiUtils.setWifiEnabled(true);
		
		if(mActReq!=null)
		try {
			mActReq.registerListener(mServListener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onDestroy();
	}
}

