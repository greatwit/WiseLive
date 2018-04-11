
package com.great.happyness;

import org.webrtc.videoengine.ViERenderer;
import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.VideoEngine;

import com.great.happyness.utils.SysConfig;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
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
  private SurfaceView remoteSurfaceView;
  
  public VideoEngine mVideoEngine;
  
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
    setViews();
    
    txttitle = (TextView) findViewById(R.id.txttitle);
    txttitle.setText("控制端");
    
    mVideoEngine = new VideoEngine(this);
    mVideoEngine.initEngine();
    
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
	    remoteSurfaceView = ViERenderer.CreateRenderer(this, true);
	    if (remoteSurfaceView != null) {
	      llRemoteSurface.addView(remoteSurfaceView);
	    }
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
  }

  public void toggleStart() {
	    if (mVideoEngine.isRecvRunning()) {
	    	mVideoEngine.stopRecv();
	    } else {
	    	mVideoEngine.startRecv(remoteSurfaceView, 11111, true, 3);
	    }
  }

  
  @Override
  public void onDestroy() 
  {
	    if (mVideoEngine.isRecvRunning()){
	    	mVideoEngine.stopRecv();
	    }
	    mVideoEngine.deInitEngine();
	    super.onDestroy();
  }
}

