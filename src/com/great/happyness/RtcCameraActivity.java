
package com.great.happyness;

import org.webrtc.videoengine.VideoCaptureShow;
import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.NativeWebRtcContextRegistry;
import org.webrtc.webrtcdemo.VideoEngine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class RtcCameraActivity extends Activity implements MediaEngineObserver
{
  private String  TAG = getClass().getSimpleName();

  private boolean mPreviewCall = true;
  
  private Button btStartStopCall;
  private TextView txttitle;
  // Remote and local stream displays.
  private LinearLayout llLocalSurface;
  
  private VideoCaptureShow mVideoCapture;
  
  private NativeWebRtcContextRegistry contextRegistry = null;
  
  static public VideoEngine mVideoEngine;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // Global settings.
    requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    //String destip = SysConfig.getSaveAddr(this);
    //destip = "192.168.0.190";
    Intent intent = getIntent();
    String destip = intent.getStringExtra("destip");
	Log.w(TAG, "get destip:" + destip );
	
    setContentView(R.layout.activity_webrtc);
    llLocalSurface  = (LinearLayout) findViewById(R.id.llRemoteView);

    // Must be instantiated before MediaEngine.
    contextRegistry = new NativeWebRtcContextRegistry();
    contextRegistry.register(this);


    
    txttitle = (TextView) findViewById(R.id.txttitle);
    txttitle.setText("拍照端");
    
    //切换相机
    ImageView btSwitchCamera = (ImageView) findViewById(R.id.change);
    btSwitchCamera.setEnabled(false);

    
    btStartStopCall = (Button) findViewById(R.id.takepicture);
    btStartStopCall.setOnClickListener(new View.OnClickListener() {
        public void onClick(View button) {
          //toggleStart();
        	if(mPreviewCall)
        	{
        		mVideoCapture.setPreviewCallback(false);
        		mPreviewCall = false;
        	}
        	else
        	{
        		mVideoCapture.setPreviewCallback(true);
        		mPreviewCall = true;
        	}
        }
      });
    
    
    mVideoCapture = new VideoCaptureShow(this);
    toggleStart();
  }
  

  private void setViews() {
    SurfaceView svLocal = mVideoCapture.getLocalSurfaceView();
    if (svLocal != null) {
      llLocalSurface.addView(svLocal);
    }
  }

  private void clearViews() {
    SurfaceView svLocal = mVideoCapture.getLocalSurfaceView();
    if (svLocal != null) {
      llLocalSurface.removeView(svLocal);
    }
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
    SurfaceView svLocal = mVideoCapture.getLocalSurfaceView();
    boolean resetLocalView = svLocal != null;
    if (resetLocalView) {
      llLocalSurface.removeView(svLocal);
    }
    if (resetLocalView) {
      svLocal = mVideoCapture.getLocalSurfaceView();
      llLocalSurface.addView(svLocal);
    }
  }

  public void toggleStart() {

  }

  public void stopAll() {
	mVideoCapture.stopCapture();
    clearViews();
  }

  private void startCall() {
    mVideoCapture.startCapture(640, 480, 2000, 35000);
    setViews();
  }
  
  @Override
  public void onDestroy()
  {
	    contextRegistry.unRegister();
	    super.onDestroy();
  }
  
}

