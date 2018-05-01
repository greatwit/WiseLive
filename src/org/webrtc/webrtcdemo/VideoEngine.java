
package org.webrtc.webrtcdemo;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.SurfaceView;


@SuppressWarnings("deprecation")
public class VideoEngine {
  private final long nativeVideoEngine;

  private int videoChannel;
  private int currentCameraHandle;
  
  private String  TAG = getClass().getSimpleName();
  
  private static final int VCM_VP8_PAYLOAD_TYPE = 100;
  private static final int SEND_CODEC_FPS 		= 30;
  // TODO(henrike): increase INIT_BITRATE_KBPS to 2000 and ensure that
  // 720p30fps can be acheived (on hardware that can handle it). Note that
  // setting 2000 currently leads to failure, so that has to be resolved first.
  private static final int INIT_BITRATE_KBPS 	= 500;
  private static final int MAX_BITRATE_KBPS 	= 3000;

  private boolean mSendRunning = false;
  private boolean mRecvRunning = false;
  
  public static final int[][] RESOLUTIONS = {
    {176,144}, {320,240}, {352,288}, {640,480}, {1280,720}, {1920,1080}
  };
  
  public static int numberOfResolutions() { return RESOLUTIONS.length; }

  public static String[] resolutionsAsString() {
    String[] retVal = new String[numberOfResolutions()];
    for (int i = 0; i < numberOfResolutions(); ++i) {
      retVal[i] = RESOLUTIONS[i][0] + "x" + RESOLUTIONS[i][1];
    }
    return retVal;
  }
  
  // Keep in sync (including this comment) with webrtc/common_types.h:TraceLevel
  public enum TraceLevel {
    TRACE_NONE(0x0000),
    TRACE_STATE_INFO(0x0001),
    TRACE_WARNING(0x0002),
    TRACE_ERROR(0x0004),
    TRACE_CRITICAL(0x0008),
    TRACE_API_CALL(0x0010),
    TRACE_DEFAULT(0x00ff),
    TRACE_MODULE_CALL(0x0020),
    TRACE_MEMORY(0x0100),
    TRACE_TIMER(0x0200),
    TRACE_STREAM(0x0400),
    TRACE_DEBUG(0x0800),
    TRACE_INFO(0x1000),
    TRACE_TERSE_INFO(0x2000),
    TRACE_ALL(0xffff);

    public final int level;
    TraceLevel(int level) {
      this.level = level;
    }
  };

  // Keep in sync (including this comment) with
  // webrtc/video_engine/include/vie_rtp_rtcp.h:ViEKeyFrameRequestMethod
  public enum VieKeyFrameRequestMethod {
    KEY_FRAME_REQUEST_NONE, KEY_FRAME_REQUEST_PLI_RTCP,
    KEY_FRAME_REQUEST_FIR_RTP, KEY_FRAME_REQUEST_FIR_RTCP
  }

  // Keep in sync (including this comment) with
  // webrtc/common_types.h:RtpDirections
  public enum RtpDirections { INCOMING, OUTGOING }

  private NativeWebRtcContextRegistry contextRegistry = null;
  
  // Checks for and communicate failures to user (logcat and popup).
  private void check(boolean value, String message) {
    if (value) {
      return;
    }
    Log.e(TAG, message);
  }
  
  public VideoEngine(Context context) {
	    contextRegistry = new NativeWebRtcContextRegistry();
	    contextRegistry.register(context);
	    nativeVideoEngine = create();
  }

  public void initEngine()
  {
	  check(init() == 0, "Failed vie Init");
	  videoChannel = createChannel();
	  check(setKeyFrameRequestMethod(videoChannel, VideoEngine.VieKeyFrameRequestMethod.
	            KEY_FRAME_REQUEST_PLI_RTCP) == 0, "Failed setKeyFrameRequestMethod");
	  
	  setTraceFilter(VideoEngine.TraceLevel.TRACE_NONE);
  }
  
  public void startSend(String remoteIp, int videoTxPort, boolean nack, int resolutionIndex, int cameraid)
  {
	  if(mSendRunning == false)
	  {
		  Log.e(TAG,"setSendDestination");
	      check(setSendDestination(videoChannel, videoTxPort, remoteIp) == 0,
	              "Failed setSendDestination");
	      
	      Log.e(TAG,"getVideoCodec");
	      VideoCodecInst codec = getVideoCodec(0, resolutionIndex);
	      Log.e(TAG,"setSendCodec");
	      check(setSendCodec(videoChannel, codec) == 0, "Failed setReceiveCodec");
	      codec.dispose();
	      Log.e(TAG,"setNackStatus");
	      check(setNackStatus(videoChannel, nack) == 0,
	    	        "Failed setNackStatus");
	      int id = getCameraId(cameraid);
	      
	      Log.e(TAG,"allocateCaptureDevice");
	      currentCameraHandle = CreateCaptureDevice(id);
	      Log.e(TAG,"connectCaptureDevice id:"+id);
	      check(connectCaptureDevice(currentCameraHandle, videoChannel) == 0,
	          "Failed to connect capture device");
	      
	      //check(startCapture(currentCameraHandle) == 0, "Failed StartCapture");
	      
	      int w = RESOLUTIONS[resolutionIndex][0];
	      int h = RESOLUTIONS[resolutionIndex][1];
	      Log.w(TAG,"startCapture w:" + w + " h:"+h);
	      
	      gstartCapture(w,h,35,12,6);
	      Log.e(TAG,"startSend");
	      check(startSend(videoChannel) == 0, "Failed StartSend");
	      
	      mSendRunning = true;
	      Log.e(TAG,"cameraid:"+ cameraid + " getCameraId id:"+id + " currentCameraHandle:"+currentCameraHandle);
	  }
  }
  
  public void stopSend()
  {
	  if(mSendRunning)
	  {
		  check(stopCapture(currentCameraHandle) == 0, "Failed StopCapture");
		  check(releaseCaptureDevice(currentCameraHandle) == 0, "Failed ReleaseCaptureDevice");
		  
		  mSendRunning = false;
	  }
  }
  
  public void startRecv(SurfaceView svRemote, int videoRxPort, boolean nack, int resolutionIndex)
  {
	  if(mRecvRunning==false)
	  {
		  check(setLocalReceiver(videoChannel, videoRxPort) == 0,
		            "Failed setLocalReceiver");
		  check(setNackStatus(videoChannel, nack) == 0,
		    	        "Failed setNackStatus");
		  
	      VideoCodecInst codec = getVideoCodec(0, resolutionIndex);
	      check(setReceiveCodec(videoChannel, codec) == 0, "Failed setReceiveCodec");
	      codec.dispose();
		  
	      check(addRenderer(videoChannel, svRemote,
	              0, 0, 0, 1, 1) == 0, "Failed AddRenderer");
	      check(startRender(videoChannel) == 0, "Failed StartRender");
	      check(startReceive(videoChannel) == 0, "Failed StartReceive");
	      mRecvRunning = true;
	  }
  }
  
  public void stopRecv()
  {
	  if(mRecvRunning)
	  {
		  check(stopReceive(videoChannel) == 0, "StopReceive");
	      check(stopRender(videoChannel) == 0, "StopRender");
	      check(removeRenderer(videoChannel) == 0, "RemoveRenderer");
	      
	      mRecvRunning = false;
	  }
  }
  
  public void deInitEngine()
  {
	    check(deleteChannel(videoChannel) == 0, "DeleteChannel");
	    dispose();
	    contextRegistry.unRegister();
  }
  
  public int provideCameraBuffer(byte[] javaCameraFrame, int length)
  {
	  return provideCameraBuffer(currentCameraHandle, javaCameraFrame, length);
  }
  
  public int registerCodecObserver(VideoDecodeEncodeObserver callback)
  {
	  return registerObserver(videoChannel, callback);
  }

  
  public boolean isSendRunning()
  {
	  return mSendRunning;
  }
  
  public boolean isRecvRunning()
  {
	  return mRecvRunning;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  
  private int getCameraId(int index) {
	    for (int i = Camera.getNumberOfCameras() - 1; i >= 0; --i) {
	      CameraInfo info = new CameraInfo();
	      Camera.getCameraInfo(i, info);
	      if (index == info.facing) {
	        return i;
	      }
	    }
	    throw new RuntimeException("Index does not match a camera");
  }
  
  private VideoCodecInst getVideoCodec(int codecNumber, int resolution) {
	    VideoCodecInst retVal = getCodec(codecNumber);
	    retVal.setStartBitRate(INIT_BITRATE_KBPS);
	    retVal.setMaxBitRate(MAX_BITRATE_KBPS);
	    retVal.setWidth(RESOLUTIONS[resolution][0]);
	    retVal.setHeight(RESOLUTIONS[resolution][1]);
	    retVal.setMaxFrameRate(SEND_CODEC_FPS);
	    return retVal;
  }
  
  // API comments can be found in VideoEngine's native APIs. Not all native
  // APIs are available.
  private native long create();
  private native int init();
  private native int setVoiceEngine(VoiceEngine voe);
  private native void dispose();
  private native int startSend(int channel);
  private native int stopRender(int channel);
  private native int stopSend(int channel);
  private native int startReceive(int channel);
  private native int stopReceive(int channel);
  private native int createChannel();
  private native int deleteChannel(int channel);
  private native int connectAudioChannel(int videoChannel, int voiceChannel);
  private native int setLocalReceiver(int channel, int port);
  private native int setSendDestination(int channel, int port, String ipAddr);
  private native int numberOfCodecs();
  private native VideoCodecInst getCodec(int index);
  private native int setReceiveCodec(int channel, VideoCodecInst codec);
  private native int setSendCodec(int channel, VideoCodecInst codec);
  private native int addRenderer(int channel, Object glSurface, int zOrder,
      float left, float top,
      float right, float bottom);
  private native int removeRenderer(int channel);
  private native int registerExternalReceiveCodec(int channel, int plType,
      MediaCodecVideoDecoder decoder, boolean internal_source);
  private native int deRegisterExternalReceiveCodec(int channel, int plType);
  private native int startRender(int channel);
  private native int numberOfCaptureDevices();
  private native int CreateCaptureDevice(int index);
  private native int allocateCaptureDevice(int index/*CameraDesc camera*/);
  private native int connectCaptureDevice(int cameraId, int channel);
  
  private native int gstartCapture(int width, int height, int maxFPS, int rawType, int codecType);
  private native int startCapture(int cameraId);
  private native int stopCapture(int cameraId);
  private native int provideCameraBuffer(int cameraId, byte[] javaCameraFrame, int length);
  private native int releaseCaptureDevice(int cameraId);
  private native int setRotateCapturedFrames(int cameraId, int degrees);
  private native int setNackStatus(int channel, boolean enable);
  private int setKeyFrameRequestMethod(int channel,
      VieKeyFrameRequestMethod requestMethod) {
    return setKeyFrameRequestMethod(channel, requestMethod.ordinal());
  }
  private native int setKeyFrameRequestMethod(int channel,
      int requestMethod);
  private native RtcpStatistics getReceivedRtcpStatistics(int channel);
  private native int registerObserver(int channel,
      VideoDecodeEncodeObserver callback);
  private native int deregisterObserver(int channel);
  private native int setTraceFile(String fileName,
      boolean fileCounter);
  private int setTraceFilter(TraceLevel filter) {
    return nativeSetTraceFilter(filter.level);
  }
  private native int nativeSetTraceFilter(int filter);
  private int startRtpDump(int channel, String file,
      RtpDirections direction) {
    return startRtpDump(channel, file, direction.ordinal());
  }
  private native int startRtpDump(int channel, String file,
      int direction);
  private int stopRtpDump(int channel, RtpDirections direction) {
    return stopRtpDump(channel, direction.ordinal());
  }
  private native int stopRtpDump(int channel, int direction);
  //public native int setLocalSSRC(int channel, int ssrc);
}

