package com.great.happyness.network;

import java.net.InetAddress;

import com.great.happyness.utils.SysConfig;


public class UdpPackageInfo {
	private InetAddress fromAddr;
	private byte[] data;
	private int sendCount = 1;
	private int port = SysConfig.UDP_TALK_PORT;
	
	public InetAddress getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(InetAddress fromAddr) {
		this.fromAddr = fromAddr;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getSendCount() {
		return sendCount;
	}
	public void setSendCount() {
		this.sendCount++;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
}
