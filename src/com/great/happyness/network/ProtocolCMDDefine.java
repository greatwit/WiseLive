package com.great.happyness.network;

/**
 * 
 * 网络消息CMD 定义
 * 
 * @author 
 * 
 */
public class ProtocolCMDDefine {

	// 开锁请求
	public static final int REQ_UNLOCK_CMD_FORSAFE = 2;

	// 开锁应答
	public static final int ACK_UNLOCK_CMD_FORSAFE = 3;

	//4.13.	文件分发（命令：13）
	public static final byte CMD_FILE_SEND = 13;

}
