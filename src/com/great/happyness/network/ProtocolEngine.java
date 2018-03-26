package com.great.happyness.network;

import java.util.ArrayList;

import com.great.happyness.utils.AbLogUtil;


/**
 * 
 * 主要负责收发出来
 * 
 * @author
 * 
 */

public class ProtocolEngine extends Thread {
	
	
	
	private String TAG = "ProtocolEngine";
	public ProtocolEngine() {
		mProtocolHandle = new ProtocolHandle(this);
	}

	public void StartEngine() {
		if (!getflag()) {
			setflag(true);
			//打开自己的线程
			start(); 
			/**
			 * 打开应当处理线程
			 */
			mProtocolHandle.start();

		}	 
	}

	public void StopEngine() {
		if (!isAlive()) {
			try {
				interrupt();
			} catch (Exception e) {
			}
		
			return;
		}
		
		if (getflag()) {
			setflag(false);
			try {
				sleep(500);
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void run() {
		while (mRunflag) {
			try {
				/**
				 * 线程可能阻塞在此处
				 * 
				 */
//				AbLogUtil.d(TAG,"单播 接收");
				UdpPackageInfo recv = mUdpOpt.read();
				/**
				 * 
				 *  加入到协议缓冲队列,接受和处理在异步出来
				 */
				mgPackBuffer.pushPackage(recv);
			} catch (Exception e) {
				AbLogUtil.d(TAG,"单播 接收错误" + e.getMessage());
				break;
			}
		}
		super.run();
	}

	/**
	 *  网络异常通知
	 *  
	 * @param code
	 */
	private void notifyNetError(int code) {
//		for(IMessageNotify it:notifyObjList){
//			it.NotifyError(code);
//		}
	}

	/**
	 * 
	 * 对外部也可单独发送包数据接口
	 * Good idea!!!!
	 * 
	 * @param udpPackage
	 * @return
	 */
	public synchronized boolean SendPack(UdpPackageInfo udpPackage) {
		/**
		 *  加入发送缓冲区，异步与主线程
		 * 
		 */
		return mgSendBuffer.pushPackage(udpPackage);
	}


 

	/**
	 * 
	 * 注册引擎回调接口
	 * 
	 * 将网络引擎解析的包，发送到对讲控制状态机模块中
	 * 
	 * 
	 * @param notifyObj
	 * @return
	 */
	public boolean registCallback(IMessageNotify notifyObj) {
		if (null != notifyObjList) {
			synchronized (notifyObjList) {
				notifyObjList.add(notifyObj);
			}
			return true;
		}
		return false;
	}

	public boolean unRegistCallback(IMessageNotify notifyObj) {
		if (null != notifyObjList) {
			synchronized (notifyObjList) {
				notifyObjList.remove(notifyObj);
			}
			return true;
		}
		return false;
	}

	protected void setflag(boolean bflag) {
		synchronized (mMutex) {
			mRunflag = bflag;
		}
	}

	protected boolean getflag() {
		boolean bflag = false;
		synchronized (mMutex) {
			bflag = mRunflag;
		}
		return bflag;
	}

	/**
	 * 网络基本收发 链路相关
	 * 
	 */
	private UdpOperation mUdpOpt = UdpOperation.gSingleton;

//	private MulticastUdpOperation mMulticastUdpOpt = MulticastUdpOperation.gSingleton;

	/**
	 * 接收其他分机控制消息，通知给ControlManger 网络异常消息，通知给ControlManger
	 */
	private ArrayList<IMessageNotify> notifyObjList = new ArrayList<IMessageNotify>();

	public UdpPackageInfoQueue getMgPackBuffer() {
		return mgPackBuffer;
	}

	private boolean mRunflag = false;

	private Object mMutex = new Object();

	private  UdpPackageInfoQueue mgPackBuffer = new UdpPackageInfoQueue();

	//private  Thread mSendThrad = null;
	
	private  UdpPackageInfoQueue mgSendBuffer = new UdpPackageInfoQueue();
	
	private  ProtocolHandle mProtocolHandle = null;

	//public static ProtocolEngine gProtocolEngine = new ProtocolEngine();

	private static ProtocolEngine gProtocolEngine=null;  
    //静态工厂方法   
    public static ProtocolEngine getInstance() {  
         if (gProtocolEngine == null) {    
        	 gProtocolEngine = new  ProtocolEngine();
         }    
        return gProtocolEngine;  
    }
}
