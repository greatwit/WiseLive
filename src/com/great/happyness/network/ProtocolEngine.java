package com.great.happyness.network;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;

import com.great.happyness.service.ListenerManager;
import com.great.happyness.service.WiFiAPService;
import com.great.happyness.utils.AbLogUtil;


/**
 * 
 * 主要负责收发出来
 * 
 * @author
 * 
 */

public class ProtocolEngine extends Thread {
	
	private String TAG = getClass().getSimpleName();
	
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

	private  Thread mSendThrad = null;
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
	
	public ProtocolEngine() {
		mProtocolHandle = new ProtocolHandle(this);
	}

	public boolean StartEngine() {
		if (!getflag()) {
			setflag(true);
			//打开自己的线程
			start(); 
			/**
			 * 打开应当处理线程
			 */
			mProtocolHandle.start();
			
			mSendThrad = new Thread(new Runnable() {
				@Override
				public void run() {
					while(mRunflag){
					UdpPackageInfo udpPackage = mgSendBuffer.popPackage();
					try{
						mUdpOpt.send(udpPackage);
					} catch (Exception e) {
						AbLogUtil.e(TAG,"error:"+e);
					}
				}
			}});
			mSendThrad.start();
			return true;
		}	
		return false;
	}

	public boolean StopEngine() {
		if(mSendThrad!=null && !mSendThrad.isAlive()){
			try {
				interrupt();
			} catch (Exception e) {

			}
		}
		
		if (!isAlive()) {
			try {
				interrupt();
				AbLogUtil.d(TAG,"try interrupt 关闭udp接收服务");
			} catch (Exception e) {
				AbLogUtil.e(TAG,"error:"+e);
			}
		
			return false;
		}
		
		if (getflag()) {
			setflag(false);
			try {
				mUdpOpt.closeSocket();
				sleep(200);
				AbLogUtil.d(TAG,"try StopEngine 已经关闭udp接收服务");
			} catch (Exception e) {

			}
			return true;
		}
		
		mSendThrad = null;
		
		return false;
	}

	@Override
	public void run() {
		while (mRunflag) {
			try {
				/**
				 * 线程可能阻塞在此处
				 * 
				 */
				AbLogUtil.d(TAG,"单播 等待接收数据");
				UdpPackageInfo recv = mUdpOpt.read();
				AbLogUtil.e(TAG, "recv len:"+ recv.getData().length + " data:"+recv.getData());
				/**
				 * 
				 *  加入到协议缓冲队列,接受和处理在异步出来
				 */
				//mgPackBuffer.pushPackage(recv);
				Bundle bundle = new Bundle();      
                bundle.putByteArray("data",recv.getData());  //往Bundle中存放数据        
                Message msg = new Message();
                msg.setData(bundle);
                ListenerManager.Instance().sendMessage(WiFiAPService.NET_CMD, msg);
			} catch (Exception e) {
				AbLogUtil.e(TAG,"单播 接收错误" + e.getMessage());
				break;
			}
		}
		AbLogUtil.d(TAG,"退出接收线程");
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
	 * @throws Exception 
	 */
	public synchronized boolean SendPack(UdpPackageInfo udpPackage) throws Exception {
		/**
		 *  加入发送缓冲区，异步与主线程
		 * 
		 */
		return mgSendBuffer.pushPackage(udpPackage);
		//return mUdpOpt.send(udpPackage);
	}
	
	public int SendData(String addr, int port, byte[] data) throws Exception {
		/**
		 *  加入发送缓冲区，异步与主线程
		 * 
		 */
		//return mgSendBuffer.pushPackage(udpPackage);
		return mUdpOpt.send(addr, port, data);
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

}

