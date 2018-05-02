package com.great.happyness.protrans.message;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgZoom extends BaseMessage
{
	private int mCmd = 0;
	private int mVal = 0;
	JSONObject object = new JSONObject();
	
	MsgZoom(){
	}
	
	public String encodeData(int command, int value) {
        
        //写入对应属性
        try {
			object.put("v", ConstDef.VERSION);	//version
	        object.put("t", ConstDef.TYPE_DATA); //type
	        object.put("c", command); //command
	        object.put("d", String.format("{'val':%s}", value)); //data
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return object.toString();
	}
	
	public void decodeData(String data){
		JSONObject obj;
		try {
			obj = new JSONObject(data);
			mCmd = obj.optInt("cmd");
			mVal = obj.optInt("val");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//最外层的JSONObject对象
	}
	
	public int getCmd(){
		return mCmd;
	}
	
	public int getValue() {
		return mVal;
	}
	
}