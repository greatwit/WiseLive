package com.great.happyness.network;

import com.great.happyness.utils.SignUtils;


public class CameraReqPack extends BasePackage {

	@Override
	public boolean decodePack(byte[] buffer) {
		if(mCmdId == ProtocolCMDDefine.CMD_FILE_SEND){
		}
		return super.decodePack(buffer);
	}

		@Override
		public byte[] encodePack(String data) {
			// TODO Auto-generated method stub
			initHead();
 
			byteHead[6] = ProtocolCMDDefine.CMD_FILE_SEND; // 命令13
			byteHead[7] = (byte) 0x02; // 命令类型02		

			//包长度
			int nLen = nHeadLen + data.length();
			SignUtils.putShort(byteHead, nLen, 8);//
			
			byte[] bytePost = new byte[nLen];
			System.arraycopy(byteHead, 0, bytePost, 0, nHeadLen);	//copy head
			
			System.arraycopy(data.getBytes(), 0, bytePost, nHeadLen, data.length()); //copy data
			
		
			return bytePost;
		}


}
