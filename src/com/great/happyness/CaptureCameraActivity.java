
package com.great.happyness;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.CameraProfile;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.great.happyness.R;
import com.great.happyness.camera.CameraHardwareException;
import com.great.happyness.camera.CameraHolder;
import com.great.happyness.camera.CameraSettings;
import com.great.happyness.camera.ComboPreferences;
import com.great.happyness.camera.FocusImageView;
import com.great.happyness.camera.ImageManager;
import com.great.happyness.camera.MenuHelper;
import com.great.happyness.camera.OnScreenHint;
import com.great.happyness.camera.RecordLocationPreference;
import com.great.happyness.camera.RotateImageView;
import com.great.happyness.camera.SensorControler;
import com.great.happyness.camera.ShutterButton;
import com.great.happyness.camera.ThumbnailController;
import com.great.happyness.camera.Util;
import com.great.happyness.camera.gallery.IImage;
import com.great.happyness.camera.gallery.IImageList;
import com.great.happyness.camera.ui.CameraHeadUpDisplay;
import com.great.happyness.camera.ui.GLRootView;
import com.great.happyness.camera.ui.HeadUpDisplay;
import com.great.happyness.camera.ui.ZoomControllerListener;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.evenbus.event.DataEvent;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.protrans.message.EntityMessages;
import com.great.happyness.protrans.message.MsgFocus;
import com.great.happyness.protrans.message.MsgZoom;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.utils.SysConfig;
import com.great.happyness.wifi.WifiUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.webrtcdemo.VideoEngine;
import org.webrtc.webrtcdemo.VoiceEngine;

/** The Camera activity which can preview and take pictures. */
@SuppressWarnings("deprecation")
@SuppressLint("InlinedApi")
public class CaptureCameraActivity extends NoSearchActivity 
		implements View.OnClickListener, ShutterButton.OnShutterButtonListener, 
		SurfaceHolder.Callback, PreviewCallback
{
    private static final String TAG = "CaptureCameraActivity";

    private static final int CROP_MSG 			= 1;
    private static final int FIRST_TIME_INIT 	= 2;
    private static final int RESTART_PREVIEW 	= 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;

    // The subset of parameters we need to update in setCameraParameters().
    private static final int UPDATE_PARAM_INITIALIZE 	= 1;
    private static final int UPDATE_PARAM_ZOOM 			= 2;
    private static final int UPDATE_PARAM_PREFERENCE 	= 4;
    private static final int UPDATE_PARAM_ALL 			= -1;

    // When setCameraParametersWhenIdle() is called, we accumulate the subsets
    // needed to be updated in mUpdateSet.
    private int mUpdateSet;

    // The brightness settings used when it is set to automatic in the system.
    // The reason why it is set to 0.7 is just because 1.0 is too bright.
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;

    private static final int SCREEN_DELAY 		= 2 * 60 * 1000;
    private static final int FOCUS_BEEP_VOLUME 	= 100;

    private static final int ZOOM_STOPPED = 0;
    private static final int ZOOM_START = 1;
    private static final int ZOOM_STOPPING = 2;

    private int mZoomState = ZOOM_STOPPED;
    private boolean mSmoothZoomSupported = false;
    private int mZoomValue = 0;  // The current zoom value.
    private int mZoomMax;
    private int mTargetZoomValue;

    
    private static final int MODE_INIT = 0;//记录是拖拉照片模式还是放大缩小照片模式
    private static final int MODE_ZOOM = 1;//放大缩小照片模式
    private int mode = MODE_INIT;// 初始状态
    private float startDis;
    
    
    private Parameters mParameters;
    private Parameters mInitialParams;

    private MyOrientationEventListener mOrientationListener;
    // The device orientation in degrees. Default is unknown.
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    // The orientation compensation for icons and thumbnails.
    private int mOrientationCompensation = 0;
    private ComboPreferences mPreferences;

    private static final int IDLE = 1;
    private static final int SNAPSHOT_IN_PROGRESS = 2;

    private int mStatus = IDLE;
    private static final String sTempCropFilename = "crop-temp";

    private android.hardware.Camera mCameraDevice;
    private ContentProviderClient mMediaProviderClient;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    private ShutterButton mShutterButton;
    //private FocusRectangle mFocusRectangle;
    private ToneGenerator mFocusToneGenerator;
    private GestureDetector mGestureDetector;
    //private Switcher mSwitcher;
    private SensorControler mSensorControler;
    /**
     * 触摸屏幕时显示的聚焦图案
     */
    private FocusImageView mFocusImageView;
    
    private GLRootView mGLRootView;

    // mPostCaptureAlert, mLastPictureButton, mThumbController
    // are non-null only if isImageCaptureIntent() is true.
    private ImageView mLastPictureButton;
    private ThumbnailController mThumbController;
    private ImageView mbtExit;
    private RotateImageView mCameraSwitch, mFlashLight;

    // mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.

    private ImageCapture mImageCapture = null;

    private boolean mStartPreviewFail = false;
    private boolean mPreviewing;
    private boolean mPausing;
    private boolean mFirstTimeInitialized;
    private boolean mIsImageCaptureIntent;
    private boolean mRecordLocation;

    private VideoEngine mVideoEngine;
    private VoiceEngine mVoiceEngine;
    
    private boolean mbHasVoice	  = false;
    private boolean mbHasFinished = false;

    private ContentResolver mContentResolver;
    private boolean mDidRegister = false;

    private final ArrayList<MenuItem> mGalleryItems = new ArrayList<MenuItem>();

    private LocationManager mLocationManager = null;

    private final ShutterCallback mShutterCallback = new ShutterCallback();
    private final PostViewPictureCallback mPostViewPictureCallback =
            		new PostViewPictureCallback();
    private final RawPictureCallback mRawPictureCallback =
            		new RawPictureCallback();
    // Use the ErrorCallback to capture the crash count
    // on the mediaserver
    private final ErrorCallback mErrorCallback = new ErrorCallback();

    private long mCaptureStartTime;
    private long mShutterCallbackTime;
    private long mPostViewPictureCallbackTime;
    private long mRawPictureCallbackTime;
    private long mJpegPictureCallbackTime;
    private int  mPicturesRemaining;

    // These latency time are for the CameraLatency test.
    public long mAutoFocusTime;
    public long mShutterLag;
    public long mShutterToPictureDisplayedTime;
    public long mPictureDisplayedToJpegCallbackTime;
    public long mJpegCallbackFinishTime;

    // Add for test
    public static boolean mMediaServerDied = false;

    // Focus mode. Options are pref_camera_focusmode_entryvalues.
    private String mFocusMode;
    private String mSceneMode;

    private final Handler mHandler = new MainHandler();
    private CameraHeadUpDisplay mHeadUpDisplay;

    // multiple cameras support
    private int mNumberOfCameras;
    private int mCameraId;

	
	private ServiceControl mServCont = ServiceControl.getInstance();
	private String mDestAddr= "";
    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESTART_PREVIEW: {
                    restartPreview();
                    if (mJpegPictureCallbackTime != 0) {
                        long now = System.currentTimeMillis();
                        mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
                        Log.v(TAG, "mJpegCallbackFinishTime = "
                                + mJpegCallbackFinishTime + "ms");
                        mJpegPictureCallbackTime = 0;
                    }
                    break;
                }

                case CLEAR_SCREEN_DELAY: {
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                }

                case FIRST_TIME_INIT: {
                    initializeFirstTime();
                    break;
                }

                case SET_CAMERA_PARAMETERS_WHEN_IDLE: {
                    setCameraParametersWhenIdle(0);
                    break;
                }
            }
        }
    }

    private void resetExposureCompensation() {
        String value = mPreferences.getString(CameraSettings.KEY_EXPOSURE,
                CameraSettings.EXPOSURE_DEFAULT_VALUE);
        if (!CameraSettings.EXPOSURE_DEFAULT_VALUE.equals(value)) {
            Editor editor = mPreferences.edit();
            editor.putString(CameraSettings.KEY_EXPOSURE, "0");
            editor.apply();
            if (mHeadUpDisplay != null) {
                mHeadUpDisplay.reloadPreferences();
            }
        }
    }

    private void keepMediaProviderInstance() {
        // We want to keep a reference to MediaProvider in camera's lifecycle.
        // TODO: Utilize mMediaProviderClient instance to replace
        // ContentResolver calls.
        if (mMediaProviderClient == null) {
            mMediaProviderClient = getContentResolver()
                    .acquireContentProviderClient(MediaStore.AUTHORITY);
        }
    }

    // Snapshots can only be taken after this is called. It should be called
    // once only. We could have done these things in onCreate() but we want to
    // make preview screen appear as soon as possible.
    private void initializeFirstTime() {
        if (mFirstTimeInitialized) return;

        // Create orientation listenter. This should be done first because it
        // takes some time to get first orientation.
        mOrientationListener = new MyOrientationEventListener(CaptureCameraActivity.this);
        mOrientationListener.enable();

        // Initialize location sevice.
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        mRecordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());
        if (mRecordLocation) startReceivingLocationUpdates();

        keepMediaProviderInstance();
        checkStorage();

        // Initialize last picture button.
        mContentResolver = getContentResolver();
        if (!mIsImageCaptureIntent)  {
            //findViewById(R.id.camera_switch).setOnClickListener(this);
            mLastPictureButton = (ImageView) findViewById(R.id.review_thumbnail);
            mLastPictureButton.setOnClickListener(this);
            mThumbController = new ThumbnailController(
                    getResources(), mLastPictureButton, mContentResolver);
            mThumbController.loadData(ImageManager.getLastImageThumbPath());
            
            mCameraSwitch 	= (RotateImageView) findViewById(R.id.btn_camera_switch);
            mCameraSwitch.setOnClickListener(this);
            mFlashLight 	= (RotateImageView) findViewById(R.id.btn_flashlight);
            mFlashLight.setOnClickListener(this);
            
            // Update last image thumbnail.
            updateThumbnailButton();
        }

        // Initialize shutter button.
        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnShutterButtonListener(this);
        mShutterButton.setVisibility(View.VISIBLE);

        mbtExit = (ImageView) findViewById(R.id.iv_exit);
        mbtExit.setOnClickListener(this);
        
        initializeScreenBrightness();
        installIntentFilter();
        initializeFocusTone();
        initializeZoom();
        mHeadUpDisplay = new CameraHeadUpDisplay(this);
        mHeadUpDisplay.setListener(new MyHeadUpDisplayListener());
        initializeHeadUpDisplay();
        mFirstTimeInitialized = true;
        changeHeadUpDisplayState();
        addIdleHandler();
    }

    private void addIdleHandler() {
        MessageQueue queue = Looper.myQueue();
        queue.addIdleHandler(new MessageQueue.IdleHandler() {
            public boolean queueIdle() {
                ImageManager.ensureOSXCompatibleFolder();
                return false;
            }
        });
    }

    private void updateThumbnailButton() {
        // Update last image if URI is invalid and the storage is ready.
        if (!mThumbController.isUriValid() && mPicturesRemaining >= 0) {
            updateLastImage();
        }
        mThumbController.updateDisplayIfNeeded();
    }

    // If the activity is paused and resumed, this method will be called in
    // onResume.
    private void initializeSecondTime() {
        // Start orientation listener as soon as possible because it takes
        // some time to get first orientation.
        mOrientationListener.enable();

        // Start location update if needed.
        mRecordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());
        if (mRecordLocation) startReceivingLocationUpdates();

        installIntentFilter();
        initializeFocusTone();
        initializeZoom();
        changeHeadUpDisplayState();

        keepMediaProviderInstance();
        checkStorage();

        if (!mIsImageCaptureIntent) {
            updateThumbnailButton();
        }
    }

    private void initializeZoom() {
        if (!mParameters.isZoomSupported()) return;

        mZoomMax = mParameters.getMaxZoom();
        mSmoothZoomSupported = mParameters.isSmoothZoomSupported();
        mGestureDetector = new GestureDetector(this, new ZoomGestureListener());
    }

    private void onZoomValueChanged(int index) {
        if (mSmoothZoomSupported) {
            if (mTargetZoomValue != index && mZoomState != ZOOM_STOPPED) {
                mTargetZoomValue = index;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mCameraDevice.stopSmoothZoom();
                }
            } else if (mZoomState == ZOOM_STOPPED && mZoomValue != index) {
                mTargetZoomValue = index;
                mCameraDevice.startSmoothZoom(index);
                mZoomState = ZOOM_START;
            }
        } else {
            mZoomValue = index;
            setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);
        }
    }

    private float[] getZoomRatios() {
        if(!mParameters.isZoomSupported()) return null;
        List<Integer> zoomRatios = mParameters.getZoomRatios();
        float result[] = new float[zoomRatios.size()];
        for (int i = 0, n = result.length; i < n; ++i) {
            result[i] = (float) zoomRatios.get(i) / 100f;
        }
        return result;
    }

    private class ZoomGestureListener extends
            GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Perform zoom only when preview is started and snapshot is not in
            // progress.
            if (mPausing || !isCameraIdle() || !mPreviewing
                    || mZoomState != ZOOM_STOPPED) {
                return false;
            }

            if (mZoomValue < mZoomMax) {
                // Zoom in to the maximum.
                mZoomValue = mZoomMax;
            } else {
                mZoomValue = 0;
            }

            setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);

            mHeadUpDisplay.setZoomIndex(mZoomValue);
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        if (!super.dispatchTouchEvent(m) && mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(m);
        }
        return true;
    }

    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                checkStorage();
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                checkStorage();
                if (!mIsImageCaptureIntent)  {
                    updateThumbnailButton();
                }
            }
        }
    };

    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            // If GPS is available before start camera, we won't get status
            // update so update GPS indicator when we receive data.
            if (mRecordLocation
                    && LocationManager.GPS_PROVIDER.equals(mProvider)) {
                if (mHeadUpDisplay != null) {
                    mHeadUpDisplay.setGpsHasSignal(true);
                }
            }
            mLastLocation.set(newLocation);
            mValid = true;
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;
                    if (mRecordLocation &&
                            LocationManager.GPS_PROVIDER.equals(provider)) {
                        if (mHeadUpDisplay != null) {
                            mHeadUpDisplay.setGpsHasSignal(false);
                        }
                    }
                    break;
                }
            }
        }

        public Location current() {
            return mValid ? mLastLocation : null;
        }
    }

    private final class ShutterCallback
            implements android.hardware.Camera.ShutterCallback {
        public void onShutter() {
            mShutterCallbackTime = System.currentTimeMillis();
            mShutterLag = mShutterCallbackTime - mCaptureStartTime;
            Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
        }
    }

    private final class PostViewPictureCallback implements PictureCallback {
        public void onPictureTaken(
                byte [] data, android.hardware.Camera camera) {
            mPostViewPictureCallbackTime = System.currentTimeMillis();
            Log.v(TAG, "mShutterToPostViewCallbackTime = "
                    + (mPostViewPictureCallbackTime - mShutterCallbackTime)
                    + "ms");
        }
    }

    private final class RawPictureCallback implements PictureCallback {
        public void onPictureTaken(
                byte [] rawData, android.hardware.Camera camera) {
            mRawPictureCallbackTime = System.currentTimeMillis();
            Log.v(TAG, "mShutterToRawCallbackTime = "
                    + (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
        }
    }

    private final class JpegPictureCallback implements PictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        public void onPictureTaken(
                final byte [] jpegData, final android.hardware.Camera camera) {
            if (mPausing) {
                return;
            }

            mJpegPictureCallbackTime = System.currentTimeMillis();
            // If postview callback has arrived, the captured image is displayed
            // in postview callback. If not, the captured image is displayed in
            // raw picture callback.
            if (mPostViewPictureCallbackTime != 0) {
                mShutterToPictureDisplayedTime =
                        mPostViewPictureCallbackTime - mShutterCallbackTime;
                mPictureDisplayedToJpegCallbackTime =
                        mJpegPictureCallbackTime - mPostViewPictureCallbackTime;
            } else {
                mShutterToPictureDisplayedTime =
                        mRawPictureCallbackTime - mShutterCallbackTime;
                mPictureDisplayedToJpegCallbackTime =
                        mJpegPictureCallbackTime - mRawPictureCallbackTime;
            }
            Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
                    + mPictureDisplayedToJpegCallbackTime + "ms");
            mHeadUpDisplay.setEnabled(true);

            if (!mIsImageCaptureIntent) {
                // We want to show the taken picture for a while, so we wait
                // for at least 1.2 second before restarting the preview.
                long delay = 1200 - mPictureDisplayedToJpegCallbackTime;
                if (delay < 0) {
                    restartPreview();
                } else {
                    mHandler.sendEmptyMessageDelayed(RESTART_PREVIEW, delay);
                }
            }
            mImageCapture.storeImage(jpegData, camera, mLocation);

            // Calculate this in advance of each shot so we don't add to shutter
            // latency. It's true that someone else could write to the SD card in
            // the mean time and fill it, but that could have happened between the
            // shutter press and saving the JPEG too.
            calculatePicturesRemaining();

            if (mPicturesRemaining < 1) {
                updateStorageHint(mPicturesRemaining);
            }

            if (!mHandler.hasMessages(RESTART_PREVIEW)) {
                long now = System.currentTimeMillis();
                mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
                Log.v(TAG, "mJpegCallbackFinishTime = "
                        + mJpegCallbackFinishTime + "ms");
                mJpegPictureCallbackTime = 0;
            }
        }
    }

    private static final class ErrorCallback
        implements android.hardware.Camera.ErrorCallback {
        public void onError(int error, android.hardware.Camera camera) {
            if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
                 mMediaServerDied = true;
                 Log.v(TAG, "media server died");
            }
        }
    }

    private class ImageCapture {
        private Uri mLastContentUri;
        byte[] mCaptureOnlyData;

        // Returns the rotation degree in the jpeg header.
        private int storeImage(byte[] data, Location loc) {
            try {
                long dateTaken = System.currentTimeMillis();
                String title = createName(dateTaken);
                String filename = title + ".jpg";
                int[] degree = new int[1];
                mLastContentUri = ImageManager.addImage(
                        mContentResolver,
                        title,
                        dateTaken,
                        loc, // location from gps/network
                        ImageManager.CAMERA_IMAGE_BUCKET_NAME, filename,
                        null, data,
                        degree);
                return degree[0];
            } catch (Exception ex) {
                Log.e(TAG, "Exception while compressing image.", ex);
                return 0;
            }
        }

        public void storeImage(final byte[] data,
                android.hardware.Camera camera, Location loc) {
            if (!mIsImageCaptureIntent) {
                int degree = storeImage(data, loc);
                sendBroadcast(new Intent(
                        "com.android.camera.NEW_PICTURE", mLastContentUri));
                setLastPictureThumb(data, degree,
                        mImageCapture.getLastCaptureUri());
                mThumbController.updateDisplayIfNeeded();
            } else {
                mCaptureOnlyData = data;
            }
        }

        //Initiate the capture of an image.
        public void initiate() {
            if (mCameraDevice == null)
                return;
            capture();
        }

        public Uri getLastCaptureUri() {
            return mLastContentUri;
        }

        private void capture() {
            mCaptureOnlyData = null;

            // See android.hardware.Camera.Parameters.setRotation for
            // documentation.
            int rotation = 0;
            if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
                if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - mOrientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + mOrientation) % 360;
                }
            }
            mParameters.setRotation(rotation);

            // Clear previous GPS location from the parameters.
            mParameters.removeGpsData();

            // We always encode GpsTimeStamp
            mParameters.setGpsTimestamp(System.currentTimeMillis() / 1000);

            // Set GPS location.
            Location loc = mRecordLocation ? getCurrentLocation() : null;
            if (loc != null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();
                boolean hasLatLon = (lat != 0.0d) || (lon != 0.0d);

                if (hasLatLon) {
                    mParameters.setGpsLatitude(lat);
                    mParameters.setGpsLongitude(lon);
                    mParameters.setGpsProcessingMethod(loc.getProvider().toUpperCase());
                    if (loc.hasAltitude()) {
                        mParameters.setGpsAltitude(loc.getAltitude());
                    } else {
                        // for NETWORK_PROVIDER location provider, we may have
                        // no altitude information, but the driver needs it, so
                        // we fake one.
                        mParameters.setGpsAltitude(0);
                    }
                    if (loc.getTime() != 0) {
                        // Location.getTime() is UTC in milliseconds.
                        // gps-timestamp is UTC in seconds.
                        long utcTimeSeconds = loc.getTime() / 1000;
                        mParameters.setGpsTimestamp(utcTimeSeconds);
                    }
                } else {
                    loc = null;
                }
            }

            mCameraDevice.setParameters(mParameters);

            mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
                    mPostViewPictureCallback, new JpegPictureCallback(loc));
            mPreviewing = false;
        }

        public void onSnap() {
            // If we are already in the middle of taking a snapshot then ignore.
            if (mPausing || mStatus == SNAPSHOT_IN_PROGRESS) {
                return;
            }
            mCaptureStartTime = System.currentTimeMillis();
            mPostViewPictureCallbackTime = 0;
            mHeadUpDisplay.setEnabled(false);
            mStatus = SNAPSHOT_IN_PROGRESS;

            mImageCapture.initiate();
        }

        private void clearLastData() {
            mCaptureOnlyData = null;
        }
    }

    private void setLastPictureThumb(byte[] data, int degree, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        Bitmap lastPictureThumb =
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
        lastPictureThumb = Util.rotate(lastPictureThumb, degree);
        mThumbController.setData(uri, lastPictureThumb);
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                getString(R.string.image_file_name_format));

        return dateFormat.format(date);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.capture_camera_activity);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);

        mPreferences = new ComboPreferences(this);
        CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
        mCameraId = CameraSettings.readPreferredCameraId(mPreferences);
        mPreferences.setLocalId(this, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());

        mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();

        int temp = SysConfig.getSavePlay(this);
      	if((temp&0x4) != 0)
      		 mbHasVoice = true;
        
        mVideoEngine = new VideoEngine(this);
        mVideoEngine.initEngine();
        
        if(mbHasVoice) {
	        mVoiceEngine = new VoiceEngine(this);
	        mVoiceEngine.initEngine();
	        Log.w(TAG, "VoiceEngine init.");
        }
        
        // we need to reset exposure for the preview
        resetExposureCompensation();
        
        // To reduce startup time, we start the preview in another thread.
        // We make sure the preview is started at the end of onCreate.
         
        Thread startPreviewThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mStartPreviewFail = false;
                    Context cont = WiseApplication.CONTEXT;
              		String mDestip =  new WifiUtils(cont).getDestAddr();
              		Log.w(TAG, "get destip:" + mDestip );
              		
              		int index = SysConfig.getSaveResolution(cont);
                    mVideoEngine.startSend(mDestip, 11111, true, index, 1);
                    if(mbHasVoice){
                  		 mVoiceEngine.startVoice(mDestip, 11113, 11113);
                  		 Log.w(TAG, "VoiceEngine startVoice.");
                    }
                    
                    startPreview();
                } catch (CameraHardwareException e) {
                    // In eng build, we throw the exception so that test tool
                    // can detect it and report it
                    if ("eng".equals(Build.TYPE)) {
                        throw new RuntimeException(e);
                    }
                    mStartPreviewFail = true;
                }
            }
        });
        startPreviewThread.start();

        // don't set mSurfaceHolder here. We have it set ONLY within
        // surfaceChanged / surfaceDestroyed, other parts of the code
        // assume that when it is set, the surface is also set.
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mIsImageCaptureIntent = isImageCaptureIntent();
        if (mIsImageCaptureIntent) {
            //setupCaptureParams();
        }
        Log.e(TAG, "IsImageCaptureIntent:"+mIsImageCaptureIntent);

        mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
        
        mSensorControler = new SensorControler(this);

        mSensorControler.setCameraFocusListener(
        		new SensorControler.CameraFocusListener() 
        {
            @Override
            public void onFocus() {
                int screenWidth = WiseApplication.SCREEN_WIDTH;
                Point point = new Point(screenWidth / 2, screenWidth / 2);
                Log.e(TAG, "setCameraFocusListener");
                onCameraFocus(point, false);
            }
        });
        
        WifiUtils wifiUtils 	= new WifiUtils(this);
        mDestAddr = wifiUtils.getDestAddr();
        
        EventBus.getDefault().register(this);
        
        // Make sure preview is started.
        try {
            startPreviewThread.join();
            if (mStartPreviewFail) {
                showCameraErrorAndFinish();
                return;
            }
        } catch (InterruptedException ex) {
            // ignore
        }
    }//onCreate

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
	        	  doSnap();
	        	  break;
	        	  
	          case ConstDef.CMD_CAMDIREC_SYN:
	        	  switchCameraId();
	        	  break;
          }
        } else {
            System.out.println("event:"+event);
        }
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataEvent event) {
        if (event != null) {
            Log.i(TAG, "onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
            String data = event.getData();
            switch(event.getCmd()) {
  	          case ConstDef.CMD_CAMFOCUS_SYN:
	  	        	MsgFocus focus = EntityMessages.getInst().getMsgFocus();
	  	        	focus.decodeData(data);
	  	        	Point point = new Point(focus.getX(), focus.getY());
	  	        	onCameraFocus(point, false);
  	        	  break;
  	          case ConstDef.CMD_CAMZOOM_SYN:
	  	        	MsgZoom zoom = EntityMessages.getInst().getMsgZoom();
	  	        	zoom.decodeData(data);
                    Log.e(TAG, "zoom value:"+mZoomValue);
                    mZoomValue = zoom.getValue();
                    onZoomValueChanged(mZoomValue);
  	        	  break;
            }
        } else {
            System.out.println("event is null");
        }
    }
    
    /**
     * 相机对焦
     *
     * @param point
     * @param needDelay 是否需要延时
     */
    public void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSensorControler.isFocusLocked()) {
                    if (onFocus(point, autoFocusCallback)) {
                        mSensorControler.lockFocus();
                        mFocusImageView.startFocus(point);
                    }
                }
            }
        }, delayDuration);
    }
    
    private final Camera.AutoFocusCallback autoFocusCallback = 
    		new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
                mFocusImageView.onFocusSuccess();
      		  	mServCont.sendCmd(mDestAddr, 
          			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMFOCUS_ACK);
                // User is half-pressing the focus key. Play the focus tone.
                // Do not take the picture now.
//                ToneGenerator tg = mFocusToneGenerator;
//                if (tg != null) {
//                    tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
//                }
            } else {
                //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                mFocusImageView.onFocusFailed();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //一秒之后才能再次对焦
                    mSensorControler.unlockFocus();
                }
            }, 1000);
        }
    };
    
    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mCameraDevice == null) {
            return false;
        }
        if (!(mFocusMode.equals(Parameters.FOCUS_MODE_INFINITY)
                || mFocusMode.equals(Parameters.FOCUS_MODE_FIXED)
                || mFocusMode.equals(Parameters.FOCUS_MODE_EDOF)))
        Log.i(TAG, "onFocus:" + point.x + "," + point.y + " mode:"+mFocusMode);
        
        Camera.Parameters parameters = null;
        try {
            parameters = mCameraDevice.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if(Build.VERSION.SDK_INT >= 14) {
            if (parameters.getMaxNumFocusAreas() <= 0) 
                return focus(callback);

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            Log.i(TAG, "left:" + left + "top:" + top
            		+"right:" + right + "bottom:" + bottom);
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
            	mCameraDevice.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }

        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
        	mCameraDevice.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void changeHeadUpDisplayState() {
        // If the camera resumes behind the lock screen, the orientation
        // will be portrait. That causes OOM when we try to allocation GPU
        // memory for the GLSurfaceView again when the orientation changes. So,
        // we delayed initialization of HeadUpDisplay until the orientation
        // becomes landscape.
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE
                && !mPausing && mFirstTimeInitialized) {
            if (mGLRootView == null) attachHeadUpDisplay();
        } else if (mGLRootView != null) {
            detachHeadUpDisplay();
        }
    }

    private void overrideHudSettings(final String flashMode,
            final String whiteBalance, final String focusMode) {
        mHeadUpDisplay.overrideSettings(
                CameraSettings.KEY_FLASH_MODE, flashMode,
                CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
                CameraSettings.KEY_FOCUS_MODE, focusMode);
    }

    private void updateSceneModeInHud() {
        // If scene mode is set, we cannot set flash mode, white balance, and
        // focus mode, instead, we read it from driver
        if (!Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
            overrideHudSettings(mParameters.getFlashMode(),
                    mParameters.getWhiteBalance(), mParameters.getFocusMode());
        } else {
            overrideHudSettings(null, null, null);
        }
    }

    private void initializeHeadUpDisplay() {
        CameraSettings settings = new CameraSettings(this, mInitialParams,
                CameraHolder.instance().getCameraInfo());
        mHeadUpDisplay.initialize(this,
                settings.getPreferenceGroup(R.xml.camera_preferences),
                getZoomRatios(), mOrientationCompensation);
        
        if (mParameters.isZoomSupported()) {
            mHeadUpDisplay.setZoomListener(new ZoomControllerListener() {
                public void onZoomChanged(
                        int index, float ratio, boolean isMoving) {
                    onZoomValueChanged(index);
                }
            });
        }
        updateSceneModeInHud();
    }

    private void attachHeadUpDisplay() {
        mHeadUpDisplay.setOrientation(mOrientationCompensation);
        if (mParameters.isZoomSupported()) {
            mHeadUpDisplay.setZoomIndex(mZoomValue);
        }
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        mGLRootView = new GLRootView(this);
        mGLRootView.setContentPane(mHeadUpDisplay);
        frame.addView(mGLRootView);
    }

    private void detachHeadUpDisplay() {
        mHeadUpDisplay.setGpsHasSignal(false);
        mHeadUpDisplay.collapse();
        ((ViewGroup) mGLRootView.getParent()).removeView(mGLRootView);
        mGLRootView = null;
    }

    public static int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }

    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = roundOrientation(orientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(CaptureCameraActivity.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                if (!mIsImageCaptureIntent) {
                    setOrientationIndicator(mOrientationCompensation);
                }
                mHeadUpDisplay.setOrientation(mOrientationCompensation);
            }
        }
    }

    private void setOrientationIndicator(int degree) {
        ((RotateImageView) findViewById(
                R.id.review_thumbnail)).setDegree(degree);
        ((RotateImageView) findViewById(
        		R.id.btn_camera_switch)).setDegree(degree);
        ((RotateImageView) findViewById(
                R.id.btn_flashlight)).setDegree(degree);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSensorControler.onStart();
        if (!mIsImageCaptureIntent) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorControler.onStop();
        if (mMediaProviderClient != null) {
            mMediaProviderClient.release();
            mMediaProviderClient = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPausing = false;
        mJpegPictureCallbackTime = 0;
        mZoomValue = 0;
        mImageCapture = new ImageCapture();

        // Start the preview if it is not started.
        if (!mPreviewing && !mStartPreviewFail) {
            resetExposureCompensation();
            if (!restartPreview()) return;
        }

        if (mSurfaceHolder != null) {
            // If first time initialization is not finished, put it in the
            // message queue.
            if (!mFirstTimeInitialized) {
                mHandler.sendEmptyMessage(FIRST_TIME_INIT);
            } else {
                initializeSecondTime();
            }
        }
        keepScreenOnAwhile();
    }
    
    @Override
    public void onDestroy()
    {
    	  if(mbHasFinished==false) {
    		  mServCont.sendCmd(mDestAddr, 
        			SysConfig.UDP_BIND_PORT, ConstDef.CMD_CAMEXIT_SYN);
    	  }
    	
    	
  	    if (mVideoEngine.isSendRunning()){
  	    	mVideoEngine.stopSend();
  	    }
  	    mVideoEngine.deInitEngine();
    	
  	    
  	    if(mbHasVoice){
		    if(mVoiceEngine.isVoiceRunning()){
		    	mVoiceEngine.stopVoice();
		    }
		    mVoiceEngine.deInitEngine();
    	}
  	    
  	    EventBus.getDefault().unregister(this);
  	    
  	    super.onDestroy();
    }
    
    private void checkStorage() {
        calculatePicturesRemaining();
        updateStorageHint(mPicturesRemaining);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.review_thumbnail:
                if (isCameraIdle()) {
                    viewLastImage();
                }
                break;
            case R.id.iv_exit:
            	finish();
            	break;
            case R.id.btn_camera_switch:
            	switchCameraId();
            	break;
            case R.id.btn_flashlight:
            	switchToVideoMode();
            	break;
        }
    }

    public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
        if (mPausing) {
            return;
        }
        switch (button.getId()) {
            case R.id.shutter_button:
                //doFocus(pressed);
                break;
        }
    }

    public void onShutterButtonClick(ShutterButton button) {
        if (mPausing) {
            return;
        }
        switch (button.getId()) {
            case R.id.shutter_button:
                doSnap();
                break;
        }
    }

    private OnScreenHint mStorageHint;

    private void updateStorageHint(int remaining) {
        String noStorageText = null;

        if (remaining == MenuHelper.NO_STORAGE_ERROR) {
            String state = Environment.getExternalStorageState();
            if (state == Environment.MEDIA_CHECKING) {
                noStorageText = getString(R.string.preparing_sd);
            } else {
                noStorageText = getString(R.string.no_storage);
            }
        } else if (remaining == MenuHelper.CANNOT_STAT_ERROR) {
            noStorageText = getString(R.string.access_sd_fail);
        } else if (remaining < 1) {
            noStorageText = getString(R.string.not_enough_space);
        }

        if (noStorageText != null) {
            if (mStorageHint == null) {
                mStorageHint = OnScreenHint.makeText(this, noStorageText);
            } else {
                mStorageHint.setText(noStorageText);
            }
            mStorageHint.show();
        } else if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }
    }

    private void installIntentFilter() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
        mDidRegister = true;
    }

    private void initializeFocusTone() {
        // Initialize focus tone generator.
        try {
            mFocusToneGenerator = new ToneGenerator(
                    AudioManager.STREAM_SYSTEM, FOCUS_BEEP_VOLUME);
        } catch (Throwable ex) {
            Log.w(TAG, "Exception caught while creating tone generator: ", ex);
            mFocusToneGenerator = null;
        }
    }

    private void initializeScreenBrightness() {
        Window win = getWindow();
        // Overright the brightness settings if it is automatic
        int mode = Settings.System.getInt(
                getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(winParams);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        changeHeadUpDisplayState();
    }

    private static ImageManager.DataLocation dataLocation() {
        return ImageManager.DataLocation.EXTERNAL;
    }

    @Override
    protected void onPause() {
        mPausing = true;
        stopPreview();
        // Close the camera now because other activities may need to use it.
        closeCamera();
        resetScreenOn();
        changeHeadUpDisplayState();

        if (mFirstTimeInitialized) {
            mOrientationListener.disable();
            if (!mIsImageCaptureIntent) {
                mThumbController.storeData(
                        ImageManager.getLastImageThumbPath());
            }
        }

        if (mDidRegister) {
            unregisterReceiver(mReceiver);
            mDidRegister = false;
        }
        stopReceivingLocationUpdates();

        if (mFocusToneGenerator != null) {
            mFocusToneGenerator.release();
            mFocusToneGenerator = null;
        }

        if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }

        // If we are in an image capture intent and has taken
        // a picture, we just clear it in onPause.
        mImageCapture.clearLastData();
        mImageCapture = null;

        // Remove the messages in the event queue.
        mHandler.removeMessages(RESTART_PREVIEW);
        mHandler.removeMessages(FIRST_TIME_INIT);

        super.onPause();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CROP_MSG: {
                Intent intent = new Intent();
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                }
                setResult(resultCode, intent);
                finish();

                File path = getFileStreamPath(sTempCropFilename);
                path.delete();

                break;
            }
        }
    }

//    private void updateFocusIndicator() {
//        if (mFocusRectangle == null) return;
//
//        if (mFocusState == FOCUSING || mFocusState == FOCUSING_SNAP_ON_FINISH) {
//            mFocusRectangle.showStart();
//        } else if (mFocusState == FOCUS_SUCCESS) {
//            mFocusRectangle.showSuccess();
//        } else if (mFocusState == FOCUS_FAIL) {
//            mFocusRectangle.showFail();
//        } else {
//            mFocusRectangle.clear();
//        }
//    }

    @Override
    public void onBackPressed() {
        if (!isCameraIdle()) {
            // ignore backs while we're taking a picture
            return;
        } else if (mHeadUpDisplay == null || !mHeadUpDisplay.collapse()) {
            super.onBackPressed();
        }
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
                    onZoomValueChanged(mZoomValue);
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
        	}
            break;
        }
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.w(TAG, "onKeyDown:"+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                    //doFocus(true);
                }
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                    doSnap();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // If we get a dpad center event without any focused view, move
                // the focus to the shutter button and press it.
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                    // Start auto-focus immediately to reduce shutter lag. After
                    // the shutter button gets the focus, doFocus() will be
                    // called again but it is fine.
                    if (mHeadUpDisplay.collapse()) return true;
                    //doFocus(true);
                    if (mShutterButton.isInTouchMode()) {
                        mShutterButton.requestFocusFromTouch();
                    } else {
                        mShutterButton.requestFocus();
                    }
                    mShutterButton.setPressed(true);
                }
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
                if (mFirstTimeInitialized) {
                    //doFocus(false);
                }
                Log.e(TAG, "KEYCODE_FOCUS:"+keyCode);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void doSnap() {
        if (mHeadUpDisplay.collapse()) return;

        Log.i(TAG, "doSnap: mFocusState=");
        // If the user has half-pressed the shutter and focus is completed, we
        // can take the photo right away. If the focus mode is infinity, we can
        // also take the photo.
//        if (mFocusMode.equals(Parameters.FOCUS_MODE_INFINITY)
//                || mFocusMode.equals(Parameters.FOCUS_MODE_FIXED)
//                || mFocusMode.equals(Parameters.FOCUS_MODE_EDOF)
//                ) {
            mImageCapture.onSnap();
        //}
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Make sure we have a surface in the holder before proceeding.
        if (holder.getSurface() == null) {
            Log.d(TAG, "holder.getSurface() == null");
            return;
        }

        // We need to save the holder for later use, even when the mCameraDevice
        // is null. This could happen if onResume() is invoked after this
        // function.
        mSurfaceHolder = holder;

        // The mCameraDevice will be null if it fails to connect to the camera
        // hardware. In this case we will show a dialog and then finish the
        // activity, so it's OK to ignore it.
        if (mCameraDevice == null) return;

        // Sometimes surfaceChanged is called after onPause or before onResume.
        // Ignore it.
        if (mPausing || isFinishing()) return;

        if (mPreviewing && holder.isCreating()) {
            // Set preview display if the surface is being created and preview
            // was already started. That means preview display was set to null
            // and we need to set it now.
            setPreviewDisplay(holder);
        } else {
            // 1. Restart the preview if the size of surface was changed. The
            // framework may not support changing preview display on the fly.
            // 2. Start the preview now if surface was destroyed and preview
            // stopped.
            restartPreview();
        }

        // If first time initialization is not finished, send a message to do
        // it later. We want to finish surfaceChanged as soon as possible to let
        // user see preview first.
        if (!mFirstTimeInitialized) {
            mHandler.sendEmptyMessage(FIRST_TIME_INIT);
        } else {
            initializeSecondTime();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        mSurfaceHolder = null;
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
        	//mCameraDevice.setZoomChangeListener(null);
            CameraHolder.instance().release();
            mCameraDevice = null;
            mPreviewing = false;
        }
    }

    private void ensureCameraDevice() throws CameraHardwareException {
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open(this, mCameraId, true);
            mInitialParams = mCameraDevice.getParameters();
        }
    }

    private void updateLastImage() {
        IImageList list = ImageManager.makeImageList(
            mContentResolver,
            dataLocation(),
            ImageManager.INCLUDE_IMAGES,
            ImageManager.SORT_ASCENDING,
            ImageManager.CAMERA_IMAGE_BUCKET_ID);
        int count = list.getCount();
        if (count > 0) {
            IImage image = list.getImageAt(count - 1);
            Uri uri = image.fullSizeImageUri();
            mThumbController.setData(uri, image.miniThumbBitmap());
        } else {
            mThumbController.setData(null, null);
        }
        list.close();
    }

    private void showCameraErrorAndFinish() {
        Resources ress = getResources();
        Util.showFatalErrorAndFinish(CaptureCameraActivity.this,
                ress.getString(R.string.camera_error_title),
                ress.getString(R.string.cannot_connect_camera));
    }

    private boolean restartPreview() {
        try {
            startPreview();
        } catch (CameraHardwareException e) {
            showCameraErrorAndFinish();
            return false;
        }
        return true;
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private void startPreview() throws CameraHardwareException {
        if (mPausing || isFinishing()) return;
        int degree = 0;
        switch(mCameraId) {
        	case Camera.CameraInfo.CAMERA_FACING_FRONT:degree=270;break;
        	case Camera.CameraInfo.CAMERA_FACING_BACK:degree=90;break;
        }
        mVideoEngine.setCaptureRotate(degree);
        
        ensureCameraDevice();

        // If we're previewing already, stop the preview first (this will blank
        // the screen).
        if (mPreviewing) stopPreview();

        setPreviewDisplay(mSurfaceHolder);
        Util.setCameraDisplayOrientation(this, mCameraId, mCameraDevice);
        setCameraParameters(UPDATE_PARAM_ALL);

        mCameraDevice.setErrorCallback(mErrorCallback);

        try {
            Log.v(TAG, "startPreview");
            mCameraDevice.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        mPreviewing = true;
        mZoomState = ZOOM_STOPPED;
        mStatus = IDLE;
    }

    private void stopPreview() {
        if (mCameraDevice != null && mPreviewing) {
            Log.v(TAG, "stopPreview");
            mCameraDevice.stopPreview();
        }
        mPreviewing = false;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = getWindowManager().getDefaultDisplay();
        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurefaceView, use screen height
            WindowManager windowManager = (WindowManager)
                    getSystemService(Context.WINDOW_SERVICE);
            targetHeight = windowManager.getDefaultDisplay().getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.v(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    private void updateCameraParametersInitialize() {
        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera application.
        List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            mParameters.setPreviewFrameRate(max);
        }
    }

    private void updateCameraParametersZoom() {
        // Set zoom.
        if (mParameters.isZoomSupported()) {
            mParameters.setZoom(mZoomValue);
        }
    }

    
	private void updateCameraParametersPreference() {
        // Set picture size.
        String pictureSize = mPreferences.getString(
                CameraSettings.KEY_PICTURE_SIZE, null);
        if (pictureSize == null) {
            CameraSettings.initialCameraPictureSize(this, mParameters);
        } else {
            List<Size> supported = mParameters.getSupportedPictureSizes();
            CameraSettings.setCameraPictureSize(
                    pictureSize, supported, mParameters);
        }
        
        mParameters.setPictureFormat(PixelFormat.JPEG);
        mParameters.setPreviewFormat(ImageFormat.NV21);
        
        // Set the preview frame aspect ratio according to the picture size.
        Size size = mParameters.getPictureSize();
//        PreviewFrameLayout frameLayout =
//                (PreviewFrameLayout) findViewById(R.id.frame_layout);
//        frameLayout.setAspectRatio((double) size.width / size.height);

        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mParameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(
                sizes, (double) size.width / size.height);
        if (optimalSize != null) {
            Size original = mParameters.getPreviewSize();
            if (!original.equals(optimalSize)) {
            	int resolutionIndex = SysConfig.getSaveResolution(this);
      	      	int w = VideoEngine.RESOLUTIONS[resolutionIndex][0];
      	      	int h = VideoEngine.RESOLUTIONS[resolutionIndex][1];
                mParameters.setPreviewSize(w, h);
                Log.e(TAG, "setPreviewSize width:"+optimalSize.width
                		+ " height:"+optimalSize.height);

                // Zoom related settings will be changed for different preview
                // sizes, so set and read the parameters to get lastest values
                mCameraDevice.setParameters(mParameters);
                mParameters = mCameraDevice.getParameters();
            }
        }

        // Since change scene mode may change supported values,
        // Set scene mode first,
        mSceneMode = mPreferences.getString(
                CameraSettings.KEY_SCENE_MODE,
                getString(R.string.pref_camera_scenemode_default));
        if (isSupported(mSceneMode, mParameters.getSupportedSceneModes())) {
            if (!mParameters.getSceneMode().equals(mSceneMode)) {
                mParameters.setSceneMode(mSceneMode);
                mCameraDevice.setParameters(mParameters);

                // Setting scene mode will change the settings of flash mode,
                // white balance, and focus mode. Here we read back the
                // parameters, so we can know those settings.
                mParameters = mCameraDevice.getParameters();
            }
        } else {
            mSceneMode = mParameters.getSceneMode();
            if (mSceneMode == null) {
                mSceneMode = Parameters.SCENE_MODE_AUTO;
            }
        }

        // Set JPEG quality.
        String jpegQuality = mPreferences.getString(
                CameraSettings.KEY_JPEG_QUALITY,
                getString(R.string.pref_camera_jpegquality_default));
        mParameters.setJpegQuality(JpegEncodingQualityMappings.getQualityNumber(jpegQuality));

        // For the following settings, we need to check if the settings are
        // still supported by latest driver, if not, ignore the settings.

        // Set color effect parameter.
        String colorEffect = mPreferences.getString(
                CameraSettings.KEY_COLOR_EFFECT,
                getString(R.string.pref_camera_coloreffect_default));
        if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
            mParameters.setColorEffect(colorEffect);
        }

        // Set exposure compensation
        String exposure = mPreferences.getString(
                CameraSettings.KEY_EXPOSURE,
                getString(R.string.pref_exposure_default));
        try {
            int value = Integer.parseInt(exposure);
            int max = mParameters.getMaxExposureCompensation();
            int min = mParameters.getMinExposureCompensation();
            if (value >= min && value <= max) {
                mParameters.setExposureCompensation(value);
            } else {
                Log.w(TAG, "invalid exposure range: " + exposure);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid exposure: " + exposure);
        }

        if (mHeadUpDisplay != null) updateSceneModeInHud();

        if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
            // Set flash mode.
            String flashMode = mPreferences.getString(
                    CameraSettings.KEY_FLASH_MODE,
                    getString(R.string.pref_camera_flashmode_default));
            List<String> supportedFlash = mParameters.getSupportedFlashModes();
            if (isSupported(flashMode, supportedFlash)) {
                mParameters.setFlashMode(flashMode);
            } else {
                flashMode = mParameters.getFlashMode();
                if (flashMode == null) {
                    flashMode = getString(
                            R.string.pref_camera_flashmode_no_flash);
                }
            }

            // Set white balance parameter.
            String whiteBalance = mPreferences.getString(
                    CameraSettings.KEY_WHITE_BALANCE,
                    getString(R.string.pref_camera_whitebalance_default));
            if (isSupported(whiteBalance,
                    mParameters.getSupportedWhiteBalance())) {
                mParameters.setWhiteBalance(whiteBalance);
            } else {
                whiteBalance = mParameters.getWhiteBalance();
                if (whiteBalance == null) {
                    whiteBalance = Parameters.WHITE_BALANCE_AUTO;
                }
            }

            // Set focus mode.
            mFocusMode = mPreferences.getString(
                    CameraSettings.KEY_FOCUS_MODE,
                    getString(R.string.pref_camera_focusmode_default));
            if (isSupported(mFocusMode, mParameters.getSupportedFocusModes())) {
                mParameters.setFocusMode(mFocusMode);
            } else {
                mFocusMode = mParameters.getFocusMode();
                if (mFocusMode == null) {
                    mFocusMode = Parameters.FOCUS_MODE_AUTO;
                }
            }
        } else {
            mFocusMode = mParameters.getFocusMode();
        }
    }

    // We separate the parameters into several subsets, so we can update only
    // the subsets actually need updating. The PREFERENCE set needs extra
    // locking because the preference can be changed from GLThread as well.
    private void setCameraParameters(int updateSet) {
        mParameters = mCameraDevice.getParameters();

        if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
            updateCameraParametersInitialize();
        }

        if ((updateSet & UPDATE_PARAM_ZOOM) != 0) {
            updateCameraParametersZoom();
        }

        if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
            updateCameraParametersPreference();
        }
        
        mCameraDevice.setParameters(mParameters);
    }

    // If the Camera is idle, update the parameters immediately, otherwise
    // accumulate them in mUpdateSet and update later.
    private void setCameraParametersWhenIdle(int additionalUpdateSet) {
        mUpdateSet |= additionalUpdateSet;
        if (mCameraDevice == null) {
            // We will update all the parameters when we open the device, so
            // we don't need to do anything now.
            mUpdateSet = 0;
            return;
        } else if (isCameraIdle()) {
            setCameraParameters(mUpdateSet);
            mUpdateSet = 0;
        } else {
            if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
                mHandler.sendEmptyMessageDelayed(
                        SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
            }
        }
    }

    private void gotoGallery() {
        MenuHelper.gotoCameraImageGallery(this);
    }

    private void viewLastImage() {
        if (mThumbController.isUriValid()) {
            Intent intent = new Intent(Util.REVIEW_ACTION, mThumbController.getUri());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW, mThumbController.getUri());
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "review image fail", e);
                }
            }
        } else {
            Log.e(TAG, "Can't view last image.");
        }
    }

    private void startReceivingLocationUpdates() {
        if (mLocationManager != null) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
        }
    }

    private void stopReceivingLocationUpdates() {
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private Location getCurrentLocation() {
        // go in best to worst order
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            if (l != null) return l;
        }
        return null;
    }

    private boolean isCameraIdle() {
        return mStatus == IDLE;
    }

    private boolean isImageCaptureIntent() {
        String action = getIntent().getAction();
        return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action));
    }

    private int calculatePicturesRemaining() {
        mPicturesRemaining = MenuHelper.calculatePicturesRemaining();
        return mPicturesRemaining;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Only show the menu when camera is idle.
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(isCameraIdle());
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mIsImageCaptureIntent) {
            // No options menu for attach mode.
            return false;
        } else {
            addBaseMenuItems(menu);
        }
        return true;
    }

    private void addBaseMenuItems(Menu menu) {
        MenuHelper.addSwitchModeMenuItem(menu, true, new Runnable() {
            public void run() {
                switchToVideoMode();
            }
        });
        MenuItem gallery = menu.add(Menu.NONE, Menu.NONE,
                MenuHelper.POSITION_GOTO_GALLERY,
                R.string.camera_gallery_photos_text)
                .setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                gotoGallery();
                return true;
            }
        });
        gallery.setIcon(android.R.drawable.ic_menu_gallery);
        mGalleryItems.add(gallery);

        if (mNumberOfCameras > 1) {
            menu.add(Menu.NONE, Menu.NONE,
                    MenuHelper.POSITION_SWITCH_CAMERA_ID,
                    R.string.switch_camera_id)
                    .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switchCameraId((mCameraId + 1) % mNumberOfCameras);
                    return true;
                }
            }).setIcon(android.R.drawable.ic_menu_camera);
        }
    }

    private void switchCameraId() {
    	int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
    	if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
    		cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    	else
    		cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    	switchCameraId(cameraId);
    }
    
    private void switchCameraId(int cameraId) {
        if (mPausing || !isCameraIdle()) return;
        mCameraId = cameraId;
        CameraSettings.writePreferredCameraId(mPreferences, cameraId);

        stopPreview();
        closeCamera();

        // Remove the messages in the event queue.
        mHandler.removeMessages(RESTART_PREVIEW);

        // Reset variables
        mJpegPictureCallbackTime = 0;
        mZoomValue = 0;

        // Reload the preferences.
        mPreferences.setLocalId(this, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());

        // Restart the preview.
        resetExposureCompensation();
        if (!restartPreview()) return;

        initializeZoom();

        // Reload the UI.
        if (mFirstTimeInitialized) {
            initializeHeadUpDisplay();
        }
    }

    private boolean switchToVideoMode() {
        if (isFinishing() || !isCameraIdle()) return false;
        MenuHelper.gotoVideoMode(this);
        mHandler.removeMessages(FIRST_TIME_INIT);
        finish();
        return true;
    }

    private void onSharedPreferenceChanged() {
        // ignore the events after "onPause()"
        if (mPausing) return;

        boolean recordLocation;

        recordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());

        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (mRecordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
        int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
        if (mCameraId != cameraId) {
            switchCameraId(cameraId);
        } else {
            setCameraParametersWhenIdle(UPDATE_PARAM_PREFERENCE);
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        keepScreenOnAwhile();
    }

    private void resetScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }

    private class MyHeadUpDisplayListener 
    			implements HeadUpDisplay.Listener 
    {
        public void onSharedPreferencesChanged() {
            CaptureCameraActivity.this.onSharedPreferenceChanged();
        }

        public void onRestorePreferencesClicked() {
            CaptureCameraActivity.this.onRestorePreferencesClicked();
        }

        public void onPopupWindowVisibilityChanged(int visibility) {
        }
    }

    protected void onRestorePreferencesClicked() {
        if (mPausing) return;
        Runnable runnable = new Runnable() {
            public void run() {
                mHeadUpDisplay.restorePreferences(mParameters);
            }
        };
        MenuHelper.confirmAction(this,
                getString(R.string.confirm_restore_title),
                getString(R.string.confirm_restore_message),
                runnable);
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		mVideoEngine.provideCameraBuffer(data, data.length);
	}

}//captureCameraActivity end

/*
 * Provide a mapping for Jpeg encoding quality levels
 * from String representation to numeric representation.
 */
@SuppressLint("InlinedApi")
class JpegEncodingQualityMappings {
    private static final String TAG = "JpegEncodingQualityMappings";
    private static final int DEFAULT_QUALITY = 85;
    private static HashMap<String, Integer> mHashMap =
            new HashMap<String, Integer>();

	static {
        mHashMap.put("normal",    CameraProfile.QUALITY_LOW);
        mHashMap.put("fine",      CameraProfile.QUALITY_MEDIUM);
        mHashMap.put("superfine", CameraProfile.QUALITY_HIGH);
    }

    // Retrieve and return the Jpeg encoding quality number
    // for the given quality level.
    public static int getQualityNumber(String jpegQuality) {
        Integer quality = mHashMap.get(jpegQuality);
        if (quality == null) {
            Log.w(TAG, "Unknown Jpeg quality: " + jpegQuality);
            return DEFAULT_QUALITY;
        }
        return CameraProfile.getJpegEncodingQualityParameter(quality.intValue());
    }
}
