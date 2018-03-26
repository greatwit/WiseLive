package com.great.happyness.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.great.happyness.utils.SysConfig;


/**
 * 
 * UDP 网络层的封装
 * 
 * UDP链路的封装
 * 
 * @author 
 * 
 */
public class UdpOperation implements IUdpOperation {
	private String TAG = "forsafe UdpOperation";

	private DatagramSocket datagramSocket = null;


	protected UdpOperation() {
		/***
		 * TODO 建立网络读写对象
		 */
		try {
			datagramSocket = new DatagramSocket(SysConfig.UDP_TALK_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public UdpPackageInfo read() throws Exception {
		/**
		 * TODO
		 * 
		 */
		// 接收的字节大小，客户端发送的数据不能超过这个大小
		byte[] message = new byte[1024];

		// 建立Socket连接
		DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
		try {
			// 准备接收数据
			datagramSocket.receive(datagramPacket);
			UdpPackageInfo recv = new UdpPackageInfo();

			recv.setData(datagramPacket.getData());
			recv.setFromAddr(datagramPacket.getAddress());

			// AbLogUtil.d(TAG,datagramPacket.getAddress().getHostAddress().toString()
			// + ":" + SignUtils.bytesToHexString(datagramPacket.getData())
			// + " addr:" + DeviceInfo.getFullAddr());

			return recv;
		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}
	@Override
	public boolean send(UdpPackageInfo udpPackage) throws Exception {
		/**
		 * TODO
		 * 
		 */
		InetAddress local = udpPackage.getFromAddr();
		DatagramPacket p = new DatagramPacket(udpPackage.getData(), udpPackage.getData().length, local, udpPackage.getPort());
		try {
//			AbLogUtil.d(TAG, local.getHostAddress().toString() + ":" + udpPackage.getPort() + " buf:" + SignUtils.bytesToHexString(udpPackage.getData()));
			datagramSocket.send(p);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static UdpOperation gSingleton = new UdpOperation();

}
