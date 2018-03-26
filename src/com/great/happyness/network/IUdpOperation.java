package com.great.happyness.network;


 /**
  * 
  * 网络层操作接口抽象
  * 
  * @author pengweiping
  *
  */
public interface IUdpOperation {
	
	 
	/**
	 * 监听端口，读网络端口数据，本函数为阻塞函数
	 * 
	 * 
	 * 有数据就读，当读到的数据不足一个合法的包时；
	 *   继续读数据，直到读一个完整的包为止
	 * 
	 * @return
	 * @throws Exception
	 */
	public UdpPackageInfo read() throws Exception;
	
	/**
	 * 监听端口，发送数据网络
	 * 
	 * @param packBuffer
	 * @return
	 * @throws Exception
	 */
	public boolean send(UdpPackageInfo udpPackage) throws Exception;
}
