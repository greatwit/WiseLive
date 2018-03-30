package com.great.happyness.network;

import com.great.happyness.utils.SignUtils;

 

public class BasePackage implements IPackageOperation {

	private final int MAX_COUNT = 1000; 
	/**
	 *  包命令ID
	 *  
	 */
	protected byte mCmdId 	= 0;
	protected byte mCmdType = 0;
	private byte[] buffer;
	protected byte[] byteHead;
	protected int nHeadLen 	= 14;
	protected int nExtandSeqID  = 0;
	
	/**
	 * 拆包时，计数当前的次序，方便异步发送
	 */
	protected int nSqenceNum = -1;
	
	
	public int getnSqenceNum() {
		return nSqenceNum;
	}
	public void setnSqenceNum(int nSqenceNum) {
		this.nSqenceNum = nSqenceNum;
	}
	
	public int getnExtandSeqID() {
		return nExtandSeqID;
	}
	public void setnExtandSeqID(int nExtandSeqID) {
		this.nExtandSeqID = nExtandSeqID;
	}
	public String getKey(){
		long epoch = System.currentTimeMillis();
		String key = mCmdId + "_" + mCmdType + "_" + epoch;
		return key;
	}
	
	/**
	 *  记录发送次数
	 */
	private int sendCount = 1;
	
	public int getSendCount() {
		return sendCount;
	}
	public void setSendCount() {
		this.sendCount++;
	}
	
	public void clearRetry(){
		this.sendCount = MAX_COUNT;
	}
	
	/**
	 * 初始化包头文件
	 * 包头	命令		命令类型	长度		时间戳	//来源设备
	 *6byte	1byte	1byte	2byte	4byte	//14byte
	 */
	protected void initHead(){
		byteHead = new byte[14];
		byteHead[0] = (byte) 0x6d;
		byteHead[1] = (byte) 0xf1;
		byteHead[2] = (byte) 0x8d;
		byteHead[3] = (byte) 0x4b;
		byteHead[4] = (byte) 0x5b;
		byteHead[5] = (byte) 0x89;
		
		//时间戳
		long epoch = System.currentTimeMillis() / 1000;		
		SignUtils.putLong(byteHead, epoch, 10);
		
		//本机设备信息
		//String treeNum = DeviceInfo.getNum();
		//if (treeNum != null && treeNum.length() > 1)
		//	System.arraycopy(SignUtils.hexStringToBytes(treeNum), 0, byteHead, 14,SignUtils.hexStringToBytes(treeNum).length);
	}
	/**
	 *  包头打包预处理
	 *  包头:
	 *  包尾:
	 * 
	 * @return
	 */
	private static void encodePreprocess(){
	}
	
	/**
	 *   解包包头包尾
	 * @param strBuffer
	 * @return
	 */
	private boolean decodePreprocess(byte[] buffer){
		mCmdId   	= buffer[6];
		mCmdType 	= buffer[7];
		return true;
	}
	
	
	
	public void setmCmdId(byte mCmdId) {
		this.mCmdId = mCmdId;
	}
	public void setmCmdType(byte mCmdType) {
		this.mCmdType = mCmdType;
	}
	public byte getmCmdId() {
		return mCmdId;
	}
	public byte getmCmdType() {
		return mCmdType;
	}
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}
	public byte[] getBuffer() {
		return buffer;
	}
	
	/**
	 *  打包数据区
	 * 
	 */
	@Override
	public byte[] encodePack(String data) {
		encodePreprocess();
		
		return buffer;
	}

	/**
	 * 解包数据区
	 * 
	 */
	@Override
	public boolean decodePack(byte[] buffer) {
		boolean bRet = decodePreprocess(buffer);
		
		return bRet;
	}
	
	/**
	 *  每个包的子类的应当实现
	 *  
	 */
	@Override
	public IPackageOperation ackPackage() {
		/**
		 * 
		 * 	TODO 
		 *  Step 1:
		 * 业务逻辑 
		 * 
		 *  根据请求做某些动作
		 *   doXXXXAction
		 * 
		 */
		BasePackage ackPackage = null;
		
		/**
		 * TODO
		 * Step 2:
		 * 
		 * 返回应答的状态包
		 * 
		 */
		return ackPackage;
	}
	

}
