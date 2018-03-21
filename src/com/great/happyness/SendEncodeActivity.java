
package com.great.happyness;

import com.great.happyness.Codec.CodecMedia;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.os.Bundle;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;


@SuppressLint("UseValueOf")
public class SendEncodeActivity extends Activity implements SurfaceHolder.Callback , OnClickListener
{
	//private String TAG = SendEncodeActivity.class.getSimpleName();
	
	private final int width 	= 1280;//1920;//
	private final int height 	= 720;//1080;//
	
	private SurfaceHolder mHolder 		= null;
	private Button btEncodecStart		= null;
	
    
    private CodecMedia mCodecMedia  	=  new CodecMedia();
    
    
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_native_encode);
		SurfaceView sfv_video = (SurfaceView) findViewById(R.id.sfv_video);
		mHolder = sfv_video.getHolder();
		mHolder.addCallback(this);
		
		btEncodecStart	 = (Button)findViewById(R.id.btEncodecStart);
		btEncodecStart.setOnClickListener(this);
		
	}

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
        mCodecMedia.StartCodecSend("192.168.43.1", width, height, holder.getSurface());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		
        mCodecMedia.StopCodecSender();
	}
	
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		    case R.id.btEncodecStart:
				break;
		}
	}
	
}


