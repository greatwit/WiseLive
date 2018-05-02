package com.great.happyness.protrans.message;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgFocus extends BaseMessage
{
	private int mCmd 	= 0;
	private int mX		= 0;
	private int mY		= 0;
	JSONObject object 	= new JSONObject();
	
	MsgFocus(){
	}
	
	public String encodeData(int command, int x, int y) {
        
        //写入对应属性
        try {
			object.put("v", ConstDef.VERSION);	//version
	        object.put("t", ConstDef.TYPE_DATA); //type
	        object.put("c", command); //command
	        object.put("d", String.format("{'x':%s,'y':%s}", x, y)); //data
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
			mX	 = obj.optInt("x");
			mY	 = obj.optInt("y");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//最外层的JSONObject对象
	}
	
	public int getCmd(){
		return mCmd;
	}
	
	public int getX(){
		return mX;
	}
	
	public int getY(){
		return mY;
	}
}

