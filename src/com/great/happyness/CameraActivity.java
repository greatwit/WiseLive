package com.great.happyness;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.great.happyness.camera.CameraManager;
import com.great.happyness.camera.album.AlbumHelper;
import com.great.happyness.camera.album.ImageItem;
import com.great.happyness.camera.util.BitmapUtils;
import com.great.happyness.camera.widget.SquareCameraContainer;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 自定义相机的activity
 *
 * @author 
 * @date 2015-09-01
 */
public class CameraActivity extends Activity 
		implements OnClickListener
{
    public static final String TAG = "CameraActivity";

    private CameraManager mCameraManager;

    private SquareCameraContainer mCameraContainer;
    private TextView m_tvFlashLight, m_tvCameraDireation;
    private ImageView m_ibRecentPic, mibTakephoto, mibExit;

    private boolean mbHasFinished = false;
    
    AlbumHelper helper;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        
        setContentView(R.layout.activity_camera);

        mCameraManager = CameraManager.getInstance(this);

        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        initView();
        initData();
        
        EventBus.getDefault().register(this);
        Log.e(TAG, "ROTATION:"+getWindowManager().getDefaultDisplay().getRotation());
    }

    void initView() {
    	mCameraContainer 	= (SquareCameraContainer) findViewById(R.id.cameraContainer);
        m_tvFlashLight 		= (TextView) findViewById(R.id.tv_flashlight);
        m_tvCameraDireation = (TextView) findViewById(R.id.tv_camera_direction);
        m_ibRecentPic 		= (ImageView) findViewById(R.id.ib_recentpic);
        mibTakephoto		= (ImageView) findViewById(R.id.ib_takephoto);
        mibExit				= (ImageView) findViewById(R.id.ib_exit);
        
        m_tvFlashLight.setOnClickListener(this);
        m_tvCameraDireation.setOnClickListener(this);
        m_ibRecentPic.setOnClickListener(this);
        mibTakephoto.setOnClickListener(this);
        mibExit.setOnClickListener(this);
    }

    void initData() {
        mCameraManager.bindOptionMenuView(m_tvFlashLight, m_tvCameraDireation);
//        mCameraContainer.setImagePath(getIntent().getStringExtra(PATH_OUTIMG));
        mCameraContainer.bindActivity(this);

        //todo  获取系统相册中的一张相片
        List<ImageItem> list = helper.getImagesList();
        if (list != null && list.size() != 0) {
            m_ibRecentPic.setImageBitmap(BitmapUtils.createCaptureBitmap(list.get(0).imagePath));
            m_ibRecentPic.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            //设置默认图片
            m_ibRecentPic.setImageResource(R.drawable.selector_camera_icon_album);
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) {
	        case R.id.tv_flashlight:
	        	mCameraContainer.switchFlashMode();
	            break;
	            
	        case R.id.tv_camera_direction:
	            m_tvCameraDireation.setClickable(false);
	            mCameraContainer.switchCamera();
	
	            //500ms后才能再次点击
	            handler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    m_tvCameraDireation.setClickable(true);
	                }
	            }, 500);
	        	break;
	        	
	        case R.id.ib_recentpic:
	            //跳转到系统相册
	            Intent intent = new Intent(Intent.ACTION_DEFAULT,
	                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	            startActivity(intent);
	        	break;
	        	
	        case R.id.ib_takephoto:
	        	mCameraContainer.takePicture();
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
	          switch(event.getCmd()){
		          case ConstDef.CMD_CAMEXIT_SYN:
		        	  	mbHasFinished = true;
		          		finish();
		        	  break;
		        	  
		          case ConstDef.CMD_TAKEPIC_SYN:
		        	  mCameraContainer.takePicture();
		        	  break;
	          }
	          
	      } else {
	          System.out.println("event:"+event);
	      }
	  }
	
    @Override
    protected void onResume() {
        super.onResume();
    }
	    
    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraContainer != null) {
            mCameraContainer.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraContainer != null) {
            mCameraContainer.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
  	  if(mbHasFinished==false) {
		  WifiUtils wifiUtils = new WifiUtils(this);
		  ServiceControl servCont = ServiceControl.getInstance();
		  servCont.sendCmd(wifiUtils.getDestAddr(), 
    			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMEXIT_SYN);
	  }
        
        mCameraManager.unbinding();
        mCameraManager.releaseActivityCamera();
        
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //在创建前  释放相机
    }

    /**
     * 照完照片 提交
     */
    public void postTakePhoto() {
//        mCameraManager.releaseActivityCamera();

        Toast.makeText(this, "take photo", Toast.LENGTH_SHORT).show();

        Bitmap bitmap = WiseApplication.CONTEXT.getCameraBitmap();

        if (bitmap != null) {
            m_ibRecentPic.setImageBitmap(bitmap);
        }
    }

}
