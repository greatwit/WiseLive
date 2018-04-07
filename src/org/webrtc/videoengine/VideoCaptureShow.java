
package org.webrtc.videoengine;

import java.io.IOException;
import java.util.concurrent.Exchanger;

import org.webrtc.webrtcdemo.MediaEngine;


import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// Wrapper for android Camera, with support for direct local preview rendering.
// Threading notes: this class is called from ViE C++ code, and from Camera &
// SurfaceHolder Java callbacks.  Since these calls happen on different threads,
// the entry points to this class are all synchronized.  This shouldn't present
// a performance bottleneck because only onPreviewFrame() is called more than
// once (and is called serially on a single thread), so the lock should be
// uncontended.  Note that each of these synchronized methods must check
// |camera| for null to account for having possibly waited for stopCapture() to
// complete.
@SuppressWarnings("deprecation")
public class VideoCaptureShow implements PreviewCallback, Callback {
  private final String TAG = getClass().getSimpleName();

  private SurfaceView svLocal;
  private SurfaceHolder localPreview;
  private Camera camera;  // Only non-null while capturing.
  private CameraThread cameraThread;
  private Handler cameraThreadHandler;
  private MediaEngine mEngine = null;
  //private static int id;
  
  //private final Camera.CameraInfo info;
  //private final OrientationEventListener orientationListener;
  //private final long native_capturer;  // |VideoCaptureAndroid*| in C++.

  // Arbitrary queue depth.  Higher number means more memory allocated & held,
  // lower number means more sensitivity to processing time in the client (and
  // potentially stalling the capturer if it runs out of buffers to write to).
  private final int numCaptureBuffers = 3;

  public SurfaceView getLocalSurfaceView() {
	    return svLocal;
  }
  
  public VideoCaptureShow(Context context, MediaEngine engine) {
    // Don't add any code here; see the comment above |self| above!
		svLocal = new SurfaceView(context);
		localPreview = svLocal.getHolder();
		mEngine = engine;
  }

  // Return the global application context.
  //private static native Context GetContext();
  // Request frame rotation post-capture.
  //private native void OnOrientationChanged(long captureObject, int degrees);

  private class CameraThread extends Thread {
    private Exchanger<Handler> handlerExchanger;
    public CameraThread(Exchanger<Handler> handlerExchanger) {
      this.handlerExchanger = handlerExchanger;
    }

    @Override public void run() {
      Looper.prepare();
      exchange(handlerExchanger, new Handler());
      Looper.loop();
    }
  }

  // Called by native code.  Returns true if capturer is started.
  //
  // Note that this actually opens the camera, and Camera callbacks run on the
  // thread that calls open(), so this is done on the CameraThread.  Since ViE
  // API needs a synchronous success return value we wait for the result.
  public synchronized boolean startCapture(
      final int width, final int height,
      final int min_mfps, final int max_mfps) {
    Log.d(TAG, "startCapture: " + width + "x" + height + "@" +
        min_mfps + ":" + max_mfps);
    if (cameraThread != null || cameraThreadHandler != null) {
      throw new RuntimeException("Camera thread already started!");
    }
    Exchanger<Handler> handlerExchanger = new Exchanger<Handler>();
    cameraThread = new CameraThread(handlerExchanger);
    cameraThread.start();
    cameraThreadHandler = exchange(handlerExchanger, null);

    final Exchanger<Boolean> result = new Exchanger<Boolean>();
    cameraThreadHandler.post(new Runnable() {
        @Override public void run() {
          startCaptureOnCameraThread(width, height, min_mfps, max_mfps, result);
        }
      });
    boolean startResult = exchange(result, false); // |false| is a dummy value.
    //orientationListener.enable();
    return startResult;
  }

  private void startCaptureOnCameraThread(
      int width, int height, int min_mfps, int max_mfps,
      Exchanger<Boolean> result) {
    Throwable error = null;
    try {
      camera = Camera.open(0); //id

      if (localPreview != null) {
        localPreview.addCallback(this);
        if (localPreview.getSurface() != null &&
            localPreview.getSurface().isValid()) {
          camera.setPreviewDisplay(localPreview);
        }
      }

      Camera.Parameters parameters = camera.getParameters();
      Log.d(TAG, "isVideoStabilizationSupported: " +
          parameters.isVideoStabilizationSupported());
      if (parameters.isVideoStabilizationSupported()) {
        parameters.setVideoStabilization(true);
      }
      parameters.setPreviewSize(width, height); 
      //parameters.setRotation(90);
      //parameters.setPreviewFpsRange(min_mfps, max_mfps);
      int format = ImageFormat.NV21;
      parameters.setPreviewFormat(format);
      camera.setParameters(parameters); 
      int bufSize = width * height * ImageFormat.getBitsPerPixel(format) / 8;
      for (int i = 0; i < numCaptureBuffers; i++) {
        camera.addCallbackBuffer(new byte[bufSize]);
      }
      camera.setPreviewCallback(this);
      //averageDurationMs = 1000 / max_mfps;
      camera.setDisplayOrientation(90);
      camera.startPreview();
      exchange(result, true);
      
      return;
    } catch (IOException e) {
      error = e;
    } catch (RuntimeException e) {
      error = e;
    }
    Log.e(TAG, "startCapture failed", error);
    if (camera != null) {
      Exchanger<Boolean> resultDropper = new Exchanger<Boolean>();
      stopCaptureOnCameraThread(resultDropper);
      exchange(resultDropper, false);
    }
    exchange(result, false);
    return;
  }

  
  public void setPreviewCallback(boolean callback)
  {
	  if(camera==null)
		  return;
	  
	  if(callback)
		  camera.setPreviewCallback(this);
	  else
		  camera.setPreviewCallback(null);
  }
  
  // Called by native code.  Returns true when camera is known to be stopped.
  public synchronized boolean stopCapture() {
    Log.d(TAG, "stopCapture");
    //orientationListener.disable();
    final Exchanger<Boolean> result = new Exchanger<Boolean>();
    cameraThreadHandler.post(new Runnable() {
        @Override public void run() {
          stopCaptureOnCameraThread(result);
        }
      });
    boolean status = exchange(result, false);  // |false| is a dummy value here.
    try {
      cameraThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    cameraThreadHandler = null;
    cameraThread = null;
    svLocal = null;
    
    Log.d(TAG, "stopCapture done");
    return status;
  }

  private void stopCaptureOnCameraThread(
      Exchanger<Boolean> result) {
    if (camera == null) {
      throw new RuntimeException("Camera is already stopped!");
    }
    Throwable error = null;
    try {
      camera.stopPreview();
      camera.setPreviewCallbackWithBuffer(null);
      if (localPreview != null) {
        localPreview.removeCallback(this);
        camera.setPreviewDisplay(null);
      }
      camera.release();
      camera = null;
      exchange(result, true);
      Looper.myLooper().quit();
      return;
    } catch (IOException e) {
      error = e;
    } catch (RuntimeException e) {
      error = e;
    }
    Log.e(TAG, "Failed to stop camera", error);
    exchange(result, false);
    Looper.myLooper().quit();
    return;
  }

  // Called on cameraThread so must not "synchronized".
  @Override
  public void onPreviewFrame(byte[] data, Camera callbackCamera) {
    if (Thread.currentThread() != cameraThread) {
      throw new RuntimeException("Camera callback not on camera thread?!?");
    } 
    Log.w(TAG, "Provide:"+data.length);
    if (camera == null) {
      return;
    }
    if (camera != callbackCamera) {
      throw new RuntimeException("Unexpected camera in callback!");
    }
    if(mEngine!=null)
    	mEngine.provideCameraBuffer(data, data.length);
    camera.addCallbackBuffer(data);
  }

  // Sets the rotation of the preview render window.
  // Does not affect the captured video image.
  // Called by native code.
  private synchronized void setPreviewRotation(final int rotation) {
    if (camera == null || cameraThreadHandler == null) {
      return;
    }
    final Exchanger<IOException> result = new Exchanger<IOException>();
    cameraThreadHandler.post(new Runnable() {
        @Override public void run() {
          setPreviewRotationOnCameraThread(rotation, result);
        }
      });
    // Use the exchanger below to block this function until
    // setPreviewRotationOnCameraThread() completes, holding the synchronized
    // lock for the duration.  The exchanged value itself is ignored.
    exchange(result, null);
  }

  private void setPreviewRotationOnCameraThread(
      int rotation, Exchanger<IOException> result) {
    Log.v(TAG, "setPreviewRotation:" + rotation);

    int resultRotation = 0;
    /*
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      // This is a front facing camera.  SetDisplayOrientation will flip
      // the image horizontally before doing the rotation.
      resultRotation = ( 360 - rotation ) % 360; // Compensate for the mirror.
    } else {
      // Back-facing camera.
      resultRotation = rotation;
    }
    */
    camera.setDisplayOrientation(resultRotation);
    exchange(result, null);
  }

  @Override
  public synchronized void surfaceChanged(
      SurfaceHolder holder, int format, int width, int height) {
    Log.d(TAG, "VideoCaptureAndroid::surfaceChanged ignored: " +
        format + ": " + width + "x" + height);
  }

  @Override
  public synchronized void surfaceCreated(final SurfaceHolder holder) {
    Log.d(TAG, "VideoCaptureAndroid::surfaceCreated");
    if (camera == null || cameraThreadHandler == null) {
      return;
    }
    final Exchanger<IOException> result = new Exchanger<IOException>();
    cameraThreadHandler.post(new Runnable() {
        @Override public void run() {
          setPreviewDisplayOnCameraThread(holder, result);
        }
      });
    IOException e = exchange(result, null);  // |null| is a dummy value here.
    if (e != null) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(TAG, "VideoCaptureAndroid::surfaceDestroyed");
    if (camera == null || cameraThreadHandler == null) {
      return;
    }
    final Exchanger<IOException> result = new Exchanger<IOException>();
    cameraThreadHandler.post(new Runnable() {
        @Override public void run() {
          setPreviewDisplayOnCameraThread(null, result);
        }
      });
    IOException e = exchange(result, null);  // |null| is a dummy value here.
    if (e != null) {
      throw new RuntimeException(e);
    }
  }

  private void setPreviewDisplayOnCameraThread(
      SurfaceHolder holder, Exchanger<IOException> result) {
    try {
      camera.setPreviewDisplay(holder);
    } catch (IOException e) {
      exchange(result, e);
      return;
    }
    exchange(result, null);
    return;
  }

  // Exchanges |value| with |exchanger|, converting InterruptedExceptions to
  // RuntimeExceptions (since we expect never to see these).
  private static <T> T exchange(Exchanger<T> exchanger, T value) {
    try {
      return exchanger.exchange(value);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
