
package org.webrtc.videoengine;

import java.io.IOException;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;

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
public class VideoCaptureShow implements Callback {
  private final static String TAG = "WEBRTC-JC";

  private SurfaceView svLocal;
  private SurfaceHolder localPreview;
  private Camera camera;  // Only non-null while capturing.

  //private static int id;
  
  //private final Camera.CameraInfo info;
  //private final OrientationEventListener orientationListener;
  //private final long native_capturer;  // |VideoCaptureAndroid*| in C++.

  // Arbitrary queue depth.  Higher number means more memory allocated & held,
  // lower number means more sensitivity to processing time in the client (and
  // potentially stalling the capturer if it runs out of buffers to write to).
  private final int numCaptureBuffers = 3;

  // Requests future capturers to send their frames to |localPreview| directly.
  public void setLocalPreview(Context context) {
    // It is a gross hack that this is a class-static.  Doing it right would
    // mean plumbing this through the C++ API and using it from
    // webrtc/examples/android/media_demo's MediaEngine class.

  }

  public SurfaceView getLocalSurfaceView() {
	    return svLocal;
	  }
  
  public VideoCaptureShow(Context context) {
    // Don't add any code here; see the comment above |self| above!
		svLocal = new SurfaceView(context);
		localPreview = svLocal.getHolder();
  }

  // Return the global application context.
  //private static native Context GetContext();
  // Request frame rotation post-capture.
  //private native void OnOrientationChanged(long captureObject, int degrees);



  // Called by native code.  Returns true if capturer is started.
  //
  // Note that this actually opens the camera, and Camera callbacks run on the
  // thread that calls open(), so this is done on the CameraThread.  Since ViE
  // API needs a synchronous success return value we wait for the result.
  public synchronized boolean startCapture(
		  Camera.PreviewCallback camCallback,
      final int width, final int height,
      final int min_mfps, final int max_mfps) 
  {
    Log.d(TAG, "startCapture: " + width + "x" + height + "@" +
        min_mfps + ":" + max_mfps);

    startCaptureOnCameraThread(camCallback, width, height, min_mfps, max_mfps);
    //orientationListener.enable();
    return true;
  }

  private void startCaptureOnCameraThread(
		  Camera.PreviewCallback camCallback,
      int width, int height, int min_mfps, int max_mfps) 
  {
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
      camera.setPreviewCallback(camCallback);
      //averageDurationMs = 1000 / max_mfps;
      camera.setDisplayOrientation(90);
      camera.startPreview();
      
      return;
    } catch (IOException e) {
      error = e;
    } catch (RuntimeException e) {
      error = e;
    }
    Log.e(TAG, "startCapture failed", error);

    return;
  }

  // Called by native code.  Returns true when camera is known to be stopped.
  public synchronized boolean stopCapture() {
    Log.d(TAG, "stopCapture");
    //orientationListener.disable();

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
        //Looper.myLooper().quit();
      } catch (IOException e) {
        error = e;
      } catch (RuntimeException e) {
        error = e;
      }
      Log.e(TAG, "Failed to stop camera", error);


      svLocal = null;
    
    Log.d(TAG, "stopCapture done");
    return true;
  }

  // Sets the rotation of the preview render window.
  // Does not affect the captured video image.
  // Called by native code.
  private synchronized void setPreviewRotation(final int rotation) {
    if (camera == null ) {
      return;
    }

    camera.setDisplayOrientation(rotation);

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
          try {
              camera.setPreviewDisplay(holder);
            } catch (IOException e) {
              return;
            }
  }

  @Override
  public synchronized void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(TAG, "VideoCaptureAndroid::surfaceDestroyed");
    if (camera == null) {
      return;
    }
    try {
        camera.setPreviewDisplay(null);
      } catch (IOException e) {
        return;
      }
  }
  
}

