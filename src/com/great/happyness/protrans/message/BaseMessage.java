package com.great.happyness.protrans.message;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseMessage
{
	protected int mType = 0;
	protected int mVers = 0;
	protected int mCmd	= 0;
	protected String mData;
	JSONObject obj 		= null;
	BaseMessage(){
	}
	
	public void inflateData(String data){
		try {
			obj = new JSONObject(data);
			mVers = obj.optInt("v");
			mType = obj.optInt("t");
			mCmd  = obj.optInt("c");
			if(mType == ConstDef.TYPE_DATA)
				mData = obj.optString("d");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//最外层的JSONObject对象
	}
	
	public int getVersion(){
		return mVers;
	}
	
	public int getType(){
		return mType;
	}
	
	public int getCmd(){
		return mCmd;
	}
	
	public String getData(){
		return mData;
	}

}
