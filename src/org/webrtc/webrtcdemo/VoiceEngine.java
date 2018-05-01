/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc.webrtcdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class VoiceEngine {
  private final long nativeVoiceEngine;

  private final Context mContext;
  private boolean sendRunning = false;
  private boolean recvRunning = false;
  private boolean voiceRunning = false;
  
  private int audioChannel;
  // Arbitrary choice of 4/5 volume (204/256).
  private static final int volumeLevel = 204;
  private BroadcastReceiver headsetListener;
  private boolean headsetPluggedIn;
  private boolean speakerEnabled;
  
  
  private void check(boolean value, String message) 
  {
	    if (value) {
	      return;
	    }
	    Log.e("WEBRTC-CHECK", message);
  }
  
  // Keep in sync (including this comment) with
  // webrtc/common_types.h:NsModes
  public enum NsModes {
    UNCHANGED, DEFAULT, CONFERENCE, LOW_SUPPRESSION,
    MODERATE_SUPPRESSION, HIGH_SUPPRESSION, VERY_HIGH_SUPPRESSION
  }

  // Keep in sync (including this comment) with
  // webrtc/common_types.h:AgcModes
  public enum AgcModes {
    UNCHANGED, DEFAULT, ADAPTIVE_ANALOG, ADAPTIVE_DIGITAL,
    FIXED_DIGITAL
  }

  // Keep in sync (including this comment) with
  // webrtc/common_types.h:AecmModes
  public enum AecmModes {
    QUIET_EARPIECE_OR_HEADSET, EARPIECE, LOUD_EARPIECE,
    SPEAKERPHONE, LOUD_SPEAKERPHONE
  }

  // Keep in sync (including this comment) with
  // webrtc/common_types.h:EcModes
  public enum EcModes { UNCHANGED, DEFAULT, CONFERENCE, AEC, AECM }

  // Keep in sync (including this comment) with
  // webrtc/common_types.h:RtpDirections
  public enum RtpDirections { INCOMING, OUTGOING }

  public static class AgcConfig {
    AgcConfig(int targetLevelDbOv, int digitalCompressionGaindB,
        boolean limiterEnable) {
      this.targetLevelDbOv = targetLevelDbOv;
      this.digitalCompressionGaindB = digitalCompressionGaindB;
      this.limiterEnable = limiterEnable;
    }
    private final int targetLevelDbOv;
    private final int digitalCompressionGaindB;
    private final boolean limiterEnable;
  }

  public VoiceEngine(Context context) {
	mContext = context;
    nativeVoiceEngine = create();
    
    // Set audio mode to communication
    AudioManager audioManager =
        ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    // Listen to headset being plugged in/out.
    IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    headsetListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
          if (intent.getAction().compareTo(Intent.ACTION_HEADSET_PLUG) == 0) {
            headsetPluggedIn = intent.getIntExtra("state", 0) == 1;
            updateAudioOutput();
          }
        }
      };
    context.registerReceiver(headsetListener, receiverFilter);
  }
  
  private void updateAudioOutput() {
	    boolean useSpeaker = !headsetPluggedIn && speakerEnabled;
	    AudioManager audioManager =
	        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
	    audioManager.setSpeakerphoneOn(useSpeaker);
  }
  
  public void initEngine()
  {
	    check(init() == 0, "Failed voe Init");
	    audioChannel = createChannel();
	    
	    check(setSpeakerVolume(volumeLevel) == 0,
	            "Failed setSpeakerVolume");
	    check(setAecmMode(VoiceEngine.AecmModes.SPEAKERPHONE, false) == 0,
	            "VoE set Aecm speakerphone mode failed");
	    
  }
  
  public void deInitEngine()
  {
	  mContext.unregisterReceiver(headsetListener);
	  check(deleteChannel(audioChannel) == 0, "VoE delete channel failed");
	  dispose();
  }
  
  public boolean isVoiceRunning()
  {
	  return voiceRunning;
  }
  
  public void startVoice(String remoteIp, int audioTxPort, int audioRxPort)
  {
	  if(voiceRunning==false)
	  {
	      check(setSendDestination(audioChannel, audioTxPort,
	              remoteIp) == 0, "VoE set send destination failed");
		  check(setLocalReceiver(audioChannel, audioRxPort) == 0,
		          "Failed setLocalReceiver");
		    
	      check(startListen(audioChannel) == 0, "Failed StartListen");
	      check(startPlayout(audioChannel) == 0, "VoE start playout failed");
	      check(startSend(audioChannel) == 0, "VoE start send failed");
	      
		  voiceRunning = true;
	  }
  }
  
  public void stopVoice()
  {
	  if(voiceRunning)
	  {
	    check(stopSend(audioChannel) == 0, "VoE stop send failed");
	    check(stopPlayout(audioChannel) == 0, "VoE stop playout failed");
	    check(stopListen(audioChannel) == 0, "VoE stop listen failed");
	    
	    voiceRunning = false;
	  }
  }
  
  public void startSend(String remoteIp, int audioTxPort)
  {
	  if(sendRunning==false)
	  {
	      check(setSendDestination(audioChannel, audioTxPort,
	              remoteIp) == 0, "VoE set send destination failed");
	      check(startListen(audioChannel) == 0, "Failed StartListen");
	      check(startPlayout(audioChannel) == 0, "VoE start playout failed");
	      check(startSend(audioChannel) == 0, "VoE start send failed");
	      
	      sendRunning = true;
	  }
  }
  
  public void stopSend() 
  {
	  if(sendRunning)
	  {
	    check(stopSend(audioChannel) == 0, "VoE stop send failed");
	    check(stopPlayout(audioChannel) == 0, "VoE stop playout failed");
	    check(stopListen(audioChannel) == 0, "VoE stop listen failed");
	    
	    sendRunning = false;
	  }
  }
  
  public void startRecv(int audioRxPort){
	    check(setLocalReceiver(audioChannel, audioRxPort) == 0,
	            "Failed setLocalReceiver");
  }
  
  public void stopRecv()
  {
	  
  }
  
  //AGC是自动增益补偿功能（Automatic Gain Control），AGC可以自动调麦克风的收音量，使与会者收到一定的音量水平，不会因发言者与麦克风的距离改变时，声音有忽大忽小声的缺点。
  public void setAgc(boolean enable) {
	    VoiceEngine.AgcConfig agc_config =
	        new VoiceEngine.AgcConfig(3, 9, true);
	    check(setAgcConfig(agc_config) == 0, "VoE set AGC Config failed");
	    check(setAgcStatus(enable, VoiceEngine.AgcModes.FIXED_DIGITAL) == 0,
	        "VoE set AGC Status failed");
  }
  
  //ANS是背景噪音抑制功能（Automatic Noise Suppression），ANS可探测出背景固定频率的杂音并消除背景噪音，例如：风扇、空调声自动滤除。呈现出与会者清晰的声音。
  public void setNs(boolean enable) {
	    check(setNsStatus(enable,
	            VoiceEngine.NsModes.MODERATE_SUPPRESSION) == 0,
	        "VoE set NS Status failed");
  }
  
  //AEC是回声消除器（Acoustic Echo Canceller）,AEC是对扬声器信号与由它产生的多路径回声的相关性为基础，建立远端信号的语音模型，利用它对回声进行估计，并不断地修改滤波器的系数，使得估计值更加逼近真实的回声。然后，将回声估计值从话筒的输入信号中减去，从而达到消除回声的目的，AEC还将话筒的输入与扬声器过去的值相比较，从而消除延长延迟的多次反射的声学回声。根椐存储器存放的过去的扬声器的输出值的多少，AEC可以消除各种延迟的回声。
  public void setEc(boolean enable) {
	    check(setEcStatus(enable, VoiceEngine.EcModes.AECM) == 0,
	        "voe setEcStatus");
  }
  
  //设置音频编码格式
  public void setAudioCodec(int codecNumber) {
	    CodecInst codec = getCodec(codecNumber);
	    check(setSendCodec(audioChannel, codec) == 0, "Failed setSendCodec");
	    codec.dispose();
  }
  
  //设置免提模式
  public void setSpeaker(boolean enable) {
	    speakerEnabled = enable;
	    updateAudioOutput();
  }
  
  
  
  private static native long create();
  public native int init();
  public native void dispose();
  public native int createChannel();
  public native int deleteChannel(int channel);
  public native int setLocalReceiver(int channel, int port);
  public native int setSendDestination(int channel, int port, String ipaddr);
  public native int startListen(int channel);
  public native int startPlayout(int channel);
  public native int startSend(int channel);
  public native int stopListen(int channel);
  public native int stopPlayout(int channel);
  public native int stopSend(int channel);
  public native int setSpeakerVolume(int volume);
  public native int setLoudspeakerStatus(boolean enable);
  public native int startPlayingFileLocally(
      int channel,
      String fileName,
      boolean loop);
  public native int stopPlayingFileLocally(int channel);
  public native int startPlayingFileAsMicrophone(
      int channel,
      String fileName,
      boolean loop);
  public native int stopPlayingFileAsMicrophone(int channel);
  public native int numOfCodecs();
  public native CodecInst getCodec(int index);
  public native int setSendCodec(int channel, CodecInst codec);
  public int setEcStatus(boolean enable, EcModes mode) {
    return setEcStatus(enable, mode.ordinal());
  }
  private native int setEcStatus(boolean enable, int ec_mode);
  public int setAecmMode(AecmModes aecm_mode, boolean cng) {
    return setAecmMode(aecm_mode.ordinal(), cng);
  }
  private native int setAecmMode(int aecm_mode, boolean cng);
  public int setAgcStatus(boolean enable, AgcModes agc_mode) {
    return setAgcStatus(enable, agc_mode.ordinal());
  }
  private native int setAgcStatus(boolean enable, int agc_mode);
  public native int setAgcConfig(AgcConfig agc_config);
  public int setNsStatus(boolean enable, NsModes ns_mode) {
    return setNsStatus(enable, ns_mode.ordinal());
  }
  private native int setNsStatus(boolean enable, int ns_mode);
  public native int startDebugRecording(String file);
  public native int stopDebugRecording();
  public int startRtpDump(int channel, String file,
      RtpDirections direction) {
    return startRtpDump(channel, file, direction.ordinal());
  }
  private native int startRtpDump(int channel, String file,
      int direction);
  public int stopRtpDump(int channel, RtpDirections direction) {
    return stopRtpDump(channel, direction.ordinal());
  }
  private native int stopRtpDump(int channel, int direction);
}

