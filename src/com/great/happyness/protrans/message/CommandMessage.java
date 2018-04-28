package com.great.happyness.protrans.message;

import org.json.JSONException;
import org.json.JSONObject;

public class CommandMessage extends MessageBase
{
	private int mCmd = 0;
	
	CommandMessage(){
	}
	
	public String encodeData(int command)
	{
        JSONObject object = new JSONObject();
        //写入对应属性
        try {
			object.put("v", ConstDef.VERSION);	//version
	        object.put("t", ConstDef.TYPE_CMD); //type
	        object.put("d", String.format("{'cmd':%s}",command)); //data
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
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//最外层的JSONObject对象
	}
	
	public int getCmd(){
		return mCmd;
	}
	
}

