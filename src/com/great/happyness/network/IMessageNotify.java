package com.great.happyness.network;


/**
 * 
 * 网络模块通知其他模块接口
 * 
 * @author  pengweiping
 *
 */
public interface IMessageNotify {

	/**
	 * 
	 * 网络异常消息通知
	 * 
	 */
	public void NotifyError(int code);
	
}
