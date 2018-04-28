package com.great.happyness.protrans.message;

public class ConstDef
{
	public static final float VERSION = 12;
	public static final int UDP_BIND_PORT = 18612;
	
	public static final int TYPE_CMD  = 1;
	public static final int TYPE_INFO = 2;
	
	public static final int CMD_CONNED_SYN 	= 10; //连接成功命令，需要对方回ack
	public static final int CMD_CONNED_ACK 	= 11; //收到连接成功命令，回ack
	public static final int CMD_SHAKE_SYN 	= 12; //发送触电命令，需要对方收到回ack
	public static final int CMD_SHAKE_ACK 	= 13; //收到触电命令后回ack
	
	public static final int CMD_PHOTO_SYN = 20;   //拍照命令，需要对方回ack
	public static final int CMD_PHOTO_ACK = 21;   //收到拍照命令，回ack
	
	public static final int UI_SER_CONNED = 10000;//后台服务连接成功事件
	
	public ConstDef(){
	}
	
	 public static boolean isWholeNumeric(String str) {  
		 boolean flag = true;   
		 if(str.length() == 0)  
			 flag = false;
		 else{  
			 for (int i = str.length();--i>=0;) {  
				 if (!Character.isDigit(str.charAt(i))) 
					 flag = false;   
			 }
		 }
		 return flag;   
	 }
}

