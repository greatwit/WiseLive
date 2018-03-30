package com.great.happyness.network;


/**
 * 
 * 网络模块 包接口抽象
 * 
 * @author  pengweiping
 *
 */


public interface IPackageOperation {
	/**
	 *  将当期消息包打包成string数据流
	 *  
	 * @return
	 */
	public byte[] encodePack(String data);
	
	
	/***
	 *     将数据流解析为包
	 * @param strBuffer
	 * @return
	 *      true:解析成功
	 *      false:解析失败
	 */
	public boolean decodePack(byte[] buffer);
	
	
	/**
	 *   请求应对包的应对处理
	 *   
	 *   应答包中不做处理
	 * 
	 * @return
	 */
	public IPackageOperation ackPackage();
	
}
