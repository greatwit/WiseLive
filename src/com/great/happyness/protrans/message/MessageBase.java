package com.great.happyness.protrans.message;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageBase
{
	protected int mType = 0;
	protected int mVers = 0;
	protected String mData;
	MessageBase(){
	}
	
	public void inflateData(String data){
		JSONObject obj;
		try {
			obj = new JSONObject(data);
			mVers = obj.optInt("v");
			mType = obj.optInt("t");
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
	
	public String getData(){
		return mData;
	}

}
