package com.great.happyness.network;
 

import java.util.LinkedList;
import java.util.Queue;

import com.great.happyness.utils.AbLogUtil;


/**
 * 
 * 生产消耗缓冲管理
 * 
 * @author pengweping 
 *
 */

public class UdpPackageInfoQueue {
	private static String TAG = "forsafe UdpPackageInfoQueue";
	public UdpPackageInfoQueue(){
		
	}
	public UdpPackageInfo popPackage(){
//		Log.d(TAG,"-------- Start popPackage --------");
		UdpPackageInfo pack = null;
		synchronized (mQueuebuffer) {
//			AbLogUtil.d(TAG,"mQueuebuffer.size()：" + mQueuebuffer.size());
			if(mQueuebuffer.size()>0){
				pack = mQueuebuffer.poll();
			}else{
			}
		}
		
		if(pack == null ){
			synchronized(mMutex){
				try {
					mMutex.wait();
					/**
					 *  重新获得一个包
					 */
					pack = mQueuebuffer.poll();
				} catch (InterruptedException e) {
					AbLogUtil.d(TAG,"-------- mMutex Error --------");
				}

			}
		}
		/**
		 * pack 可能为空
		 */

//		AbLogUtil.d(TAG,"-------- return pack --------");
		return pack;
	}
	
	
	public boolean  pushPackage(UdpPackageInfo pack){
    	boolean bRet = false;
    	synchronized (mQueuebuffer) {
    		bRet = mQueuebuffer.add(pack);
		}
    	
    	synchronized(mMutex){

//    		AbLogUtil.d(TAG,"-------- pushPackage mMutex--------");
    	   	mMutex.notifyAll();
    	}
		return bRet;
    }
	
	private Object mMutex = new Object();
	private Queue<UdpPackageInfo> mQueuebuffer = new LinkedList<UdpPackageInfo>();
}
