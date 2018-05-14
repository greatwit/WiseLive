
package com.great.happyness;

import org.webrtc.videoengine.VideoCaptureShow;
import org.webrtc.webrtcdemo.VideoEngine;
import org.webrtc.webrtcdemo.VoiceEngine;

import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class CameraSendActivity extends Activity implements PreviewCallback
{
  private String  TAG = getClass().getSimpleName();

  private ImageButton btStartStopCall;
  private TextView tvStats;

  // Remote and local stream displays.
  private LinearLayout llLocalSurface;
  
  private VideoCaptureShow mVideoCapture;
  
  public VideoEngine mVideoEngine;
  public VoiceEngine mVoiceEngine;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Global settings.
    requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    
	
	
    setContentView(R.layout.activity_webrtc);
    llLocalSurface  = (LinearLayout) findViewById(R.id.llRemoteView);

    
    mVideoEngine = new VideoEngine(this);
    mVideoEngine.initEngine();
    
    mVoiceEngine = new VoiceEngine(this);
    mVoiceEngine.initEngine();
    
    tvStats = (TextView) findViewById(R.id.tvStats);
    
    Button btSwitchCamera = (Button) findViewById(R.id.btSwitchCamera);

      btSwitchCamera.setEnabled(false);

    btStartStopCall = (ImageButton) findViewById(R.id.btStartStopCall);
    //btStartStopCall.setBackgroundResource(getEngine().isRunning() ? R.drawable.record_stop : R.drawable.record_start);
    btStartStopCall.setOnClickListener(new View.OnClickListener() {
        public void onClick(View button) {
        	toggleStart();
        }
      });
    
    Log.w(TAG, "spCodecSize.setSelection:"+ SysConfig.getSaveResolution(this) );

    
    mVideoCapture = new VideoCaptureShow(this);
    toggleStart();
  }

  private void setViews() {
    SurfaceView svLocal = mVideoCapture.getLocalSurfaceView();
    //svLocal.setZOrderOnTop(true);
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
          tvStats.setText(stats);
        }
      });
  }

  public void toggleStart() {
  	if(mVideoEngine.isSendRunning())
  	{
         mVideoEngine.stopSend();
         stopAll();
         
         mVoiceEngine.stopVoice();
  	}else
  	{
  		String mDestip =  new WifiUtils(this).getDestAddr();
  		Log.w(TAG, "get destip:" + mDestip );
         mVideoEngine.startSend(mDestip, 11111, true, 3, 1);
         startCall();
         
         int temp = SysConfig.getSavePlay(this);
       	 if((temp&0x4) != 0)
       		 mVoiceEngine.startVoice(mDestip, 11113, 11113);
  	}
    //btStartStopCall.setBackgroundResource(mVideoEngine.isSendRunning() ? R.drawable.record_stop : R.drawable.record_start);
  }
  
  private void toggleCamera(Button btSwitchCamera) {
    SurfaceView svLocal = mVideoCapture.getLocalSurfaceView();
    boolean resetLocalView = svLocal != null;
    if (resetLocalView) {
      llLocalSurface.removeView(svLocal);
    }
    //getEngine().toggleCamera();
    if (resetLocalView) {
      svLocal = mVideoCapture.getLocalSurfaceView();
      llLocalSurface.addView(svLocal);
    }
  }

  public void stopAll() {
	mVideoCapture.stopCapture();
    clearViews();
    //getEngine().stopVideoSend();
  }

  private void startCall() {
    //getEngine().startVideoSend();
	int position = SysConfig.getSaveResolution(this);
	int width = VideoEngine.RESOLUTIONS[position][0];
	int height = VideoEngine.RESOLUTIONS[position][1];
    mVideoCapture.startCapture(this, width, height, 2000, 35000);
    Log.w(TAG,"startCall "+"width:"+width+" height:"+height);
    setViews();
  }
  
  @Override
  public void onDestroy()
  {
	    if (mVideoEngine.isSendRunning()){
	    	mVideoEngine.stopSend();
	        stopAll();
	    }
	    mVideoEngine.deInitEngine();
	    
	    if(mVoiceEngine.isVoiceRunning())
	    {
	    	mVoiceEngine.stopVoice();
	    }
	    mVoiceEngine.deInitEngine();
	    super.onDestroy();
  }

@Override
public void onPreviewFrame(byte[] data, Camera camera) {
	// TODO Auto-generated method stub
	mVideoEngine.provideCameraBuffer(data, data.length);
	Log.w(TAG, "Provide:"+data.length);
}
  
}

