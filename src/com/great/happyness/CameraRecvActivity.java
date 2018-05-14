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
import org.webrtc.webrtcdemo.VoiceEngine;

import com.great.happyness.camera.FocusImageView;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.protrans.message.EntityMessages;
import com.great.happyness.protrans.message.MsgFocus;
import com.great.happyness.protrans.message.MsgZoom;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
  private FocusImageView mFocusImageView;
  private static final int MODE_INIT = 0;//记录是拖拉照片模式还是放大缩小照片模式
  private static final int MODE_ZOOM = 1;//放大缩小照片模式
  private int mode = MODE_INIT;// 初始状态
  private float startDis;

  private boolean mSmoothZoomSupported = false;
  private int mZoomValue = 0;  // The current zoom value.
  private int mZoomMax = 60;
  
  private final Handler mHandler = new MainHandler();
  
  private VideoEngine mVideoEngine;
  private VoiceEngine mVoiceEngine;
  
  private boolean mbHasVoice	  	= false;
  private String mDestAddr			= "";
  private boolean mbHasFinished 	= false;
  
  ServiceControl mServCont 	= ServiceControl.getInstance();
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
	    
        int temp = SysConfig.getSavePlay(this);
      	if((temp&0x4) != 0)
      		 mbHasVoice = true;
        
        mVideoEngine = new VideoEngine(this);
        mVideoEngine.initEngine();
        
        if(mbHasVoice){
	        mVoiceEngine = new VoiceEngine(this);
	        mVoiceEngine.initEngine();
        }
	    
	    WifiUtils wifiUtils = new WifiUtils(this);
	    mDestAddr = wifiUtils.getDestAddr();
	    
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
      
      mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
      
	  mtvFlashLight.setText("闪光");
	
	  Drawable drawable = getResources().getDrawable(R.drawable.selector_btn_flashlight_auto);
	  drawable.setBounds(0, 0, 64, 64);
	  mtvFlashLight.setCompoundDrawables(drawable, null, null, null);
	  
	  Drawable drawable1 = getResources().getDrawable(R.drawable.selector_btn_camera_front);
	  drawable1.setBounds(0, 0, 64, 64);
	  mtvCameraDireation.setCompoundDrawables(drawable1, null, null, null);
	  mtvCameraDireation.setText("前置");
  }
  
  private class MainHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
      }
  }
  
  public void onCameraFocus(final Point point, boolean needDelay) {
      long delayDuration = needDelay ? 300 : 0;

      mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
                      mFocusImageView.startFocus(point);
          }
      }, delayDuration);
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
      switch (event.getAction() & MotionEvent.ACTION_MASK) {
      // 手指压下屏幕
      case MotionEvent.ACTION_DOWN:
          mode = MODE_INIT;
          break;
          
      case MotionEvent.ACTION_POINTER_DOWN:
          //如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
          mode = MODE_ZOOM;
          /** 计算两个手指间的距离 */
          startDis = spacing(event);
          break;
          
      case MotionEvent.ACTION_MOVE:
          if (mode == MODE_ZOOM) {
              //只有同时触屏两个点的时候才执行
              if (event.getPointerCount() < 2) return true;
              float endDis = spacing(event);// 结束距离
              //每变化10f zoom变1
              int scale = (int) ((endDis - startDis) / 10f);
              if (scale >= 1 || scale <= -1) {
                  int zoom = mZoomValue + scale;
                  //zoom不能超出范围
                  if (zoom > mZoomMax) zoom = mZoomValue;
                  if (zoom < 0) zoom = 0;
                  mZoomValue = zoom;
                  
                  Log.e(TAG, "zoom value:"+mZoomValue);
	  		      MsgZoom msgzoom = EntityMessages.getInst().getMsgZoom();
	  		      String msg = msgzoom.encodeData(ConstDef.CMD_CAMZOOM_SYN, mZoomValue);
	  	          mServCont.sendData(mDestAddr, SysConfig.UDP_BIND_PORT, msg);
                  //将最后一次的距离设为当前距离
                  startDis = endDis;
              }
          }
          break;
	        // 手指离开屏幕
	  case MotionEvent.ACTION_UP:
      	  if (mode != MODE_ZOOM) {
	            //设置聚焦
	            Point point = new Point((int) event.getX(), (int) event.getY());
	            Log.w(TAG, "onTouchEvent x:"+point.x + " y:"+point.y);
	            onCameraFocus(point, false);
	            
		        MsgFocus focus = EntityMessages.getInst().getMsgFocus();
		        String msg = focus.encodeData(ConstDef.CMD_CAMFOCUS_SYN, point.x, point.y);
	            mServCont.sendData(mDestAddr, SysConfig.UDP_BIND_PORT, msg);
      	  }
          break;
      }
      return true;
  }
  
  //两点的距离
  private float spacing(MotionEvent event) {
      if (event == null) {
          return 0;
      }
      float x = event.getX(0) - event.getX(1);
      float y = event.getY(0) - event.getY(1);
      return (float)Math.sqrt(x * x + y * y);
  }
  
  @Override
  public void onClick(View v) {
  	// TODO Auto-generated method stub
	  
      switch (v.getId()) {
      case R.id.ib_takephoto:
    	  mServCont.sendCmd(mDestAddr, 
    			SysConfig.UDP_BIND_PORT, ConstDef.CMD_TAKEPIC_SYN);
    	  break;
    	  
      case R.id.tv_camera_direction:
    	  mServCont.sendCmd(mDestAddr, 
	    		SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMDIREC_SYN);
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
          switch(event.getCmd()) {
	          case ConstDef.CMD_CAMEXIT_SYN:
	      	  	mbHasFinished = true;
	        		finish();
	        	  break;
        	  
	          case ConstDef.CMD_CAMFOCUS_ACK:
	        	  mFocusImageView.onFocusSuccess();
	        	  break;
          }
      } else {
          System.out.println("event:"+event);
      }
  }
  
  @Override    
  public boolean onKeyDown(int keyCode, KeyEvent event) {    
      if ((keyCode == KeyEvent.KEYCODE_BACK)) {    
           System.out.println("back_key  onKeyDown()");
           finish();
           return false;    
      }else {    
          return super.onKeyDown(keyCode, event);    
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
    
	if(mbHasVoice){
	    if(mVoiceEngine.isVoiceRunning()){
	    	mVoiceEngine.stopVoice();
	    	mVoiceEngine.deInitEngine();
	    }else{
	    	mVoiceEngine.startVoice(mDestAddr, 11113, 11113);
	    }
	}
    //btStartStopCall.setBackgroundResource(mVideoEngine.isRecvRunning() ? R.drawable.record_stop : R.drawable.record_start);
  }
  
  @Override
  public void onDestroy() {
	    if(mbHasFinished==false) {
		  mServCont.sendCmd(mDestAddr, 
				  SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMEXIT_SYN);
	    }
	  
	    if (mVideoEngine.isRecvRunning()){
	    	mVideoEngine.stopRecv();
	    }
	    removeViews();
	    remoteSurfaceView = null;
	    mVideoEngine.deInitEngine();
	    
		if(mbHasVoice){
		    if(mVoiceEngine.isVoiceRunning()){
		    	mVoiceEngine.stopVoice();
		    	mVoiceEngine.deInitEngine();
		    }else{
		    	String mDestip =  new WifiUtils(this).getDestAddr();
		    	mVoiceEngine.startVoice(mDestip, 11113, 11113);
		    }
		}
	    
	    EventBus.getDefault().unregister(this);
	    
	    super.onDestroy();
  }
  
}

