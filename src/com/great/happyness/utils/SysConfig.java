package com.great.happyness.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SysConfig 
{
	/**
	 * 网络监听端口
	 */
	public static final Integer UDP_BIND_PORT 	= 18200;
	
	public static final int VideoSendPort	= 5008;
	public static final int VideoRecvPort	= 5012;
	public static final int AudioSendPort	= 5016;
    public static final int AudioRecvPort	= 5020;
    
    public static final int VIDEO_WIDTH		= 1280;
    public static final int VIDEO_HEIGHT	= 720;
    public static final int VIDEO_FRAME		= 20;
    public static final int VIDEO_BITRATE	= 2500000;
    
    
    /**
     * 热点名称
     */
    public static final String WIFI_AP_PREFIX	= "WISE";
    public static final String WIFI_AP_SSID 	= WIFI_AP_PREFIX + "_" + android.os.Build.MODEL;
    public static final String WIFI_AP_KEY  	= "12345678";
    
    
	private static String RESOLUTION_NAME	= "wise_value";
	private static String RESOLUTION_KEY 	= "wise_key";
	public static int getSaveResolution(Context context)
	{
		int iResult = 3;
		if(context==null)
			return iResult;
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(RESOLUTION_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    iResult = sharedPreferences.getInt(RESOLUTION_KEY, iResult);
	    
	    return iResult;
	}

	public static int setSaveResolution(Context context, int value)
	{
		int iResult = -1;
		if(context==null)
			return iResult;

		SharedPreferences sp = context.getSharedPreferences(RESOLUTION_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    Editor editor = sp.edit();
	    editor.putInt(RESOLUTION_KEY, value);
	    editor.commit();

	    return 0;
	}
	
	//1111 right to left : video receive, video send, audio
	private static String PLAY_NAME	= "play_value";
	private static String PLAY_KEY 	= "play_key";
	private static String PLAY_ADDR = "play_addr";
	public static int getSavePlay(Context context)
	{
		int iResult = 0x7;//0111
		if(context==null)
			return iResult;
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    iResult = sharedPreferences.getInt(PLAY_KEY, iResult);
	    
	    return iResult;
	}

	public static int setSavePlay(Context context, int value)
	{
		int iResult = -1;
		if(context==null)
			return iResult;

		SharedPreferences sp = context.getSharedPreferences(PLAY_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    Editor editor = sp.edit();
	    editor.putInt(PLAY_KEY, value);
	    editor.commit();

	    return 0;
	}
	
	public static String getSaveAddr(Context context)
	{
		String iResult = "";//
		if(context==null)
			return iResult;
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    iResult = sharedPreferences.getString(PLAY_ADDR, iResult);
	    
	    return iResult;
	}

	public static int setSaveAddr(Context context, String value)
	{
		if(context==null)
			return -1;

		SharedPreferences sp = context.getSharedPreferences(PLAY_NAME, Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
	    Editor editor = sp.edit();
	    editor.putString(PLAY_ADDR, value);
	    editor.commit();

	    return 0;
	}
	
}
