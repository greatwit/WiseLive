/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.great.happyness;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.videoengine.ViERenderer;
import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.VideoEngine;

import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CameraRecvActivity extends Activity  
			 implements MediaEngineObserver, OnClickListener
{
  private String  TAG = getClass().getSimpleName();


  // Remote and local stream displays.
  private LinearLayout llRemoteSurface;
  SurfaceView remoteSurfaceView;
  
  private TextView mtvFlashLight, mtvCameraDireation;
  private ImageView mibRecentPic, mibTakephoto, mibExit;
  
  static public VideoEngine mVideoEngine;
  private boolean mbHasFinished = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
	    super.onCreate(savedInstanceState);
	    // Global settings.
	    requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
		
	    setContentView(R.layout.activity_camera_recv);
	    initView();
	    
	    mVideoEngine = new VideoEngine(this);
	    mVideoEngine.initEngine();
	    
	    toggleStart();
	    EventBus.getDefault().register(this);
  }
  
  void initView() {
	  llRemoteSurface 	= (LinearLayout) findViewById(R.id.llRemoteView);
	  remoteSurfaceView = ViERenderer.CreateRenderer(this, true);
	  if (remoteSurfaceView != null)
	      llRemoteSurface.addView(remoteSurfaceView);
	  
      mtvFlashLight 		= (TextView) findViewById(R.id.tv_flashlight);
      mtvCameraDireation 	= (TextView) findViewById(R.id.tv_camera_direction);
      mibRecentPic 			= (ImageView) findViewById(R.id.ib_recentpic);
      mibTakephoto			= (ImageView) findViewById(R.id.ib_takephoto);
      mibExit				= (ImageView) findViewById(R.id.ib_exit);
      
      mtvFlashLight.setOnClickListener(this);
      mtvCameraDireation.setOnClickListener(this);
      mibRecentPic.setOnClickListener(this);
      mibTakephoto.setOnClickListener(this);
      mibExit.setOnClickListener(this);
      
      
	  mtvFlashLight.setText("闪光");
	
	  Drawable drawable = getResources().getDrawable(R.drawable.selector_btn_flashlight_auto);
	  drawable.setBounds(0, 0, 64, 64);
	  mtvFlashLight.setCompoundDrawables(drawable, null, null, null);
	  
	  Drawable drawable1 = getResources().getDrawable(R.drawable.selector_btn_camera_front);
	  drawable1.setBounds(0, 0, 64, 64);
	  mtvCameraDireation.setCompoundDrawables(drawable1, null, null, null);
	  mtvCameraDireation.setText("前置");
  }
  
  @Override
  public void onClick(View v) {
  	// TODO Auto-generated method stub
      switch (v.getId()) {
      case R.id.ib_takephoto:
		  WifiUtils wifiUtils = new WifiUtils(this);
		  ServiceControl servCont = ServiceControl.getInstance();
		  servCont.sendCmd(wifiUtils.getDestAddr(), 
    			SysConfig.UDP_BIND_PORT, ConstDef.CMD_TAKEPIC_SYN);
    	  break;
    	  
      case R.id.ib_exit:
      	onBackPressed();
      	break;
      }
  }
  
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(CmdEvent event) {
      if (event != null) {
          Log.i(TAG, "onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
          if(event.getCmd() == ConstDef.CMD_CAMEXIT_SYN) {
        	  	mbHasFinished = true;
          		finish();
          }
      } else {
          System.out.println("event:"+event);
      }
  }

  private void removeViews() {
	    remoteSurfaceView = ViERenderer.CreateRenderer(this, true);
	    if (remoteSurfaceView != null) {
	      llRemoteSurface.removeView(remoteSurfaceView);
	    }
	  }
  
  // tvStats need to be updated on the UI thread.
  public void newStats(final String stats) {
    	runOnUiThread(new Runnable() {
        public void run() {
        }
      });
  }

  public void toggleStart() {
    if (mVideoEngine.isRecvRunning()) {
    	mVideoEngine.stopRecv();
    } else {
    	mVideoEngine.startRecv(remoteSurfaceView, 11111, true, SysConfig.getSaveResolution(this));
    }
    //btStartStopCall.setBackgroundResource(mVideoEngine.isRecvRunning() ? R.drawable.record_stop : R.drawable.record_start);
  }

  
  @Override
  public void onDestroy() {
	  if(mbHasFinished==false) {
		  WifiUtils wifiUtils = new WifiUtils(this);
		  ServiceControl servCont = ServiceControl.getInstance();
		  servCont.sendCmd(wifiUtils.getDestAddr(), 
    			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMEXIT_SYN);
	  }
	    if (mVideoEngine.isRecvRunning()){
	    	mVideoEngine.stopRecv();
	    }
	    removeViews();
	    remoteSurfaceView = null;
	    mVideoEngine.deInitEngine();
	    
	    EventBus.getDefault().unregister(this);
	    
	    super.onDestroy();
  }
  
}

