
package com.great.happyness;

import com.great.happyness.Codec.CodecMedia;



import android.annotation.SuppressLint;
import android.app.Activity;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class RecvDecodeActivity extends Activity implements SurfaceHolder.Callback 
{
	private final int width 	= 1920;//1280;//
	private final int height 	= 1080;//720;//
	
	private SurfaceHolder holder = null;
	
    private CodecMedia mCodecMedia  	=  new CodecMedia();
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_native_decode);
		SurfaceView sfv_video = (SurfaceView) findViewById(R.id.sfv_video);

		holder = sfv_video.getHolder();
		holder.addCallback(this);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		mCodecMedia.StartCodecRecv(width, height, holder.getSurface());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//mCodecMedia.StopVideoSend();
		mCodecMedia.StopCodecRecver();
	}

}


