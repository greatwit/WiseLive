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

import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.NativeWebRtcContextRegistry;

import com.great.happyness.utils.SysConfig;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RtcRecvActivity extends Activity  implements MediaEngineObserver
{

  private String  TAG = "WebrtcActivity";

  private Button btStartStopCall;
  private TextView txttitle;
  // Remote and local stream displays.
  private LinearLayout llRemoteSurface;
  
  private NativeWebRtcContextRegistry contextRegistry = null;
  
   
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Global settings.
    requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    String destip = SysConfig.getSaveAddr(this);
    
	Log.w(TAG, "get destip:" + destip );
	
    setContentView(R.layout.activity_webrtc);
    llRemoteSurface = (LinearLayout) findViewById(R.id.llRemoteView);

    // Must be instantiated before MediaEngine.
    contextRegistry = new NativeWebRtcContextRegistry();
    contextRegistry.register(this);

    
    txttitle = (TextView) findViewById(R.id.txttitle);
    txttitle.setText("控制端");
    

    btStartStopCall = (Button) findViewById(R.id.takepicture);
    //btStartStopCall.setBackgroundResource(getEngine().isRunning() ? R.drawable.record_stop : R.drawable.record_start);
    btStartStopCall.setOnClickListener(new View.OnClickListener() {
        public void onClick(View button) {
          //toggleStart();
        }
      });

    toggleStart();
    //Log.i(TAG, VideoCaptureDeviceInfoAndroid.getDeviceInfo());
  }
  

  private void setViews() {
//    SurfaceView remoteSurfaceView = getEngine().getRemoteSurfaceView();
//    if (remoteSurfaceView != null) {
//      llRemoteSurface.addView(remoteSurfaceView);
//    }
  }

  private void clearViews() {
//    SurfaceView remoteSurfaceView = getEngine().getRemoteSurfaceView();
//    if (remoteSurfaceView != null) {
//      llRemoteSurface.removeView(remoteSurfaceView);
//    }
  }

  // tvStats need to be updated on the UI thread.
  public void newStats(final String stats) {
    	runOnUiThread(new Runnable() {
        public void run() {
          //tvStats.setText(stats);
        }
      });
  }

  private void toggleCamera(Button btSwitchCamera) {
//    SurfaceView svLocal = getEngine().getLocalSurfaceView();
//    boolean resetLocalView = svLocal != null;
//    if (resetLocalView) {
//      llLocalSurface.removeView(svLocal);
//    }
//    getEngine().toggleCamera();
//    if (resetLocalView) {
//      svLocal = getEngine().getLocalSurfaceView();
//      llLocalSurface.addView(svLocal);
//    }
//    btSwitchCamera.setText(getEngine().frontCameraIsSet() ?
//        R.string.backCamera :
//        R.string.frontCamera);
  }

  public void toggleStart() {

    //btStartStopCall.setBackgroundResource(getEngine().isRunning() ? R.drawable.record_stop : R.drawable.record_start);
  }

  public void stopAll() {
    clearViews();
  }

  private void startCall() {
    setViews();
  }
  
  @Override
  public void onDestroy() 
  {
	    contextRegistry.unRegister();
	    super.onDestroy();
  }
}

