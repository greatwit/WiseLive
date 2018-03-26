package com.great.happyness.network;

import com.great.happyness.utils.AbLogUtil;

/**
 * 
 * 接收处理逻辑，握手逻辑在本线程中运行
 * 
 * 
 * @author pengweiping
 * 
 */


public class ProtocolHandle extends Thread {

	private String TAG = ProtocolHandle.class.getSimpleName();

	ProtocolHandle(ProtocolEngine engine){
		mRefProtocolEngine  = engine;
	}


	@Override
	public void run() {
		super.run();  

		while(true){
			/**
			 * 当队列为空时，popPackage位置会能阻塞，直到队列里面有数据为止
			 */
			UdpPackageInfo recv = null;
			if(mRefProtocolEngine != null)
				recv = mRefProtocolEngine.getMgPackBuffer().popPackage();
			if(recv != null){
			}
		}
	}	

	private ProtocolEngine mRefProtocolEngine = null;
}
