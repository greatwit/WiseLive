package com.great.happyness.protrans.message;

public class ConstDef{
	public static final float VERSION 			= 12;
	public static final int UDP_BIND_PORT 		= 18612;
	
	public static final int TYPE_CMD  			= 1;
	public static final int TYPE_DATA 			= 2;
	
	public static final int CMD_CONNED_SYN 		= 10; //连接成功命令，需要对方回ack
	public static final int CMD_CONNED_ACK 		= 11; //收到连接成功命令，回ack
	public static final int CMD_SHAKE_SYN 		= 20; //发送触电命令，需要对方收到回ack
	public static final int CMD_SHAKE_ACK 		= 21; //收到触电命令后回ack
	
	public static final int CMD_CAMOPEN_SYN 	= 100;//打开摄像头
	public static final int CMD_CAMOPEN_ACK 	= 101;
	public static final int CMD_CAMEXIT_SYN 	= 110;//关闭摄像头
	public static final int CMD_CAMEXIT_ACK 	= 111;
	public static final int CMD_TAKEPIC_SYN 	= 120;//拍照命令，需要对方回ack
	public static final int CMD_TAKEPIC_ACK 	= 121;//收到拍照命令，回ack
	public static final int CMD_CAMDIREC_SYN 	= 130;//摄像头方向
	public static final int CMD_CAMDIREC_ACK 	= 131;
	public static final int CMD_CAMFLASH_SYN	= 140;//摄像头灯光
	public static final	int CMD_CAMFLASH_ACK	= 141;
	public static final int CMD_CAMFOCUS_SYN	= 150;//摄像头聚焦
	public static final	int CMD_CAMFOCUS_ACK	= 151;
	public static final int CMD_CAMZOOM_SYN		= 160;//摄像头放大缩小
	public static final	int CMD_CAMZOOM_ACK		= 161;
	
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

