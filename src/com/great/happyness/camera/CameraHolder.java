package com.great.happyness.camera;

import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import static com.great.happyness.camera.Util.Assert;

import java.io.IOException;


@SuppressWarnings("deprecation")
public class CameraHolder //implements PreviewCallback 
{
    private static final String TAG = "CameraHolder";
    private android.hardware.Camera mCameraDevice;
    private long mKeepBeforeTime = 0;  // Keep the Camera before this time.
    private final Handler mHandler;
    private int mUsers = 0;  // number of open() - number of release()
    private int mNumberOfCameras;
    private int mCameraId = -1;
    private CameraInfo[] mInfo;

    // We store the camera parameters when we actually open the device,
    // so we can restore them in the subsequent open() requests by the user.
    // This prevents the parameters set by the Camera activity used by
    // the VideoCamera activity inadvertently.
    private Parameters mParameters;

//    private VideoEngine mVideoEngine = null;
//    private WifiUtils mWifiUtils 	 = null;
    
    // Use a singleton.
    private static CameraHolder sHolder;
    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }

    private static final int RELEASE_CAMERA = 1;
    private class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case RELEASE_CAMERA:
                    synchronized (CameraHolder.this) {
                        // In 'CameraHolder.open', the 'RELEASE_CAMERA' message
                        // will be removed if it is found in the queue. However,
                        // there is a chance that this message has been handled
                        // before being removed. So, we need to add a check
                        // here:
                        if (CameraHolder.this.mUsers == 0) releaseCamera();
                    }
                    break;
            }
        }
    }

    private CameraHolder() {
        HandlerThread ht = new HandlerThread("CameraHolder");
        ht.start();
        mHandler = new MyHandler(ht.getLooper());
        mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            android.hardware.Camera.getCameraInfo(i, mInfo[i]);
        }
    }

    public int getNumberOfCameras() {
        return mNumberOfCameras;
    }

    public CameraInfo[] getCameraInfo() {
        return mInfo;
    }

    public synchronized android.hardware.Camera open(int cameraId)
            throws CameraHardwareException {
        Assert(mUsers == 0);
        if (mCameraDevice != null && mCameraId != cameraId) {
            mCameraDevice.release();
            mCameraDevice = null;
            mCameraId = -1;
        }
        if (mCameraDevice == null) {
            try {
                Log.v(TAG, "open camera " + cameraId);
                mCameraDevice = android.hardware.Camera.open(cameraId);
                mCameraId = cameraId;
            } catch (RuntimeException e) {
                Log.e(TAG, "fail to connect Camera", e);
                throw new CameraHardwareException(e);
            }
            
            mParameters = mCameraDevice.getParameters();
        } else {
            try {
                mCameraDevice.reconnect();
            } catch (IOException e) {
                Log.e(TAG, "reconnect failed.");
                throw new CameraHardwareException(e);
            }
            mCameraDevice.setParameters(mParameters);
        }
        ++mUsers;
        mHandler.removeMessages(RELEASE_CAMERA);
        mKeepBeforeTime = 0;
        return mCameraDevice;
    }

    public synchronized android.hardware.Camera open(int cameraId, PreviewCallback cont)
    {
    	try {
			mCameraDevice = open(cameraId);
		} catch (CameraHardwareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mCameraDevice.setPreviewCallback(cont);
    	return mCameraDevice;
    }
    
    public synchronized android.hardware.Camera open(PreviewCallback cont, int cameraId, boolean dataCall)
    {
//        mWifiUtils = new WifiUtils(cont);
//        mVideoEngine = new VideoEngine(cont);
//        mVideoEngine.initEngine();
//        mVideoEngine.startSend(mWifiUtils.getDestAddr(), 11111, true, 3, 1);

    	try {
			mCameraDevice = open(cameraId);
		} catch (CameraHardwareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(dataCall)
    		mCameraDevice.setPreviewCallback(cont);
    	return mCameraDevice;
    }
    
    /**
     * Tries to open the hardware camera. If the camera is being used or
     * unavailable then return {@code null}.
     */
    public synchronized android.hardware.Camera tryOpen(int cameraId) {
        try {
            return mUsers == 0 ? open(cameraId) : null;
        } catch (CameraHardwareException e) {
            // In eng build, we throw the exception so that test tool
            // can detect it and report it
            if ("eng".equals(Build.TYPE)) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    public synchronized void release() {
        Assert(mUsers == 1);
        --mUsers;
        mCameraDevice.setPreviewCallback(null);
        mCameraDevice.stopPreview();
        releaseCamera();
        
//        if(mVideoEngine!=null){
//		    if (mVideoEngine.isSendRunning()){
//		    	mVideoEngine.stopSend();
//		    }
//		    mVideoEngine.deInitEngine();
//        }
    }

    private synchronized void releaseCamera() {
        Assert(mUsers == 0);
        Assert(mCameraDevice != null);
        long now = System.currentTimeMillis();
        if (now < mKeepBeforeTime) {
            mHandler.sendEmptyMessageDelayed(RELEASE_CAMERA,
                    mKeepBeforeTime - now);
            return;
        }
        mCameraDevice.release();
        mCameraDevice = null;
        mCameraId = -1;
    }

    public synchronized void keep() {
        // We allow (mUsers == 0) for the convenience of the calling activity.
        // The activity may not have a chance to call open() before the user
        // choose the menu item to switch to another activity.
        Assert(mUsers == 1 || mUsers == 0);
        // Keep the camera instance for 3 seconds.
        mKeepBeforeTime = System.currentTimeMillis() + 3000;
    }

//	@Override
//	public void onPreviewFrame(byte[] data, Camera camera) {
//		// TODO Auto-generated method stub
//		
//		//mVideoEngine.provideCameraBuffer(data, data.length);
//		Log.w(TAG, " Provide:"+data.length);
//	}
}
