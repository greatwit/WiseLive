package com.great.happyness.network;

import java.util.zip.CRC32;

import com.great.happyness.utils.SignUtils;


public class CameraReqPack extends BasePackage {

	@Override
	public boolean decodePack(byte[] buffer) {
		if(mCmdId == ProtocolCMDDefine.CMD_FILE_SEND){
			int nPosStart = nHeadLen;
			// 文件类型
			byte fileType = buffer[nPosStart];
			// 文件序号
			int fileId = SignUtils.bytes2Int(buffer, nPosStart + 1);
			// 文件名长度
			byte nTitleLen = buffer[nPosStart + 5];
			// 文件大小
			int fileSize = SignUtils.bytes2Int(buffer, nPosStart + 6);
	
			// 发布时间
			int fileTime = SignUtils.bytes2Int(buffer, nPosStart + 10);
			
			String md5 = SignUtils.byte2MD5(buffer, nPosStart + 14);//new String(buffer, nPosStart + 14, 32);
			// 文件名
			String fileName = SignUtils.byte2String(buffer,nPosStart + 46, nTitleLen);//new String(buffer, nPosStart + 46, nTitleLen);
	
	
			int nLen = SignUtils.bytes2Short(buffer, 8);
			nLen = nLen - nPosStart - 46 - nTitleLen;
			
			nExtandSeqID = fileId;
			// 文件url
			String fileUrl = SignUtils.byte2String(buffer,nPosStart + 46+ nTitleLen, nLen);//new String(buffer, nPosStart + 46 + nTitleLen, nLen);
			//AbLogUtil.d("FileSendReqPack","文件下发 md5=" + md5 + " fileName=" + fileName + " fileUrl="+fileUrl +  " fileSize=" + fileSize);
			//FileDBOper.addLocalFile(fileType, fileId, fileTime, fileSize, fileName, md5, fileUrl);
		}
		return super.decodePack(buffer);
	}

	public static class FileSendAckPack extends BasePackage {
		
		@Override
		public byte[] encodePack() {
			// TODO Auto-generated method stub
			initHead();

			byteHead[6] = ProtocolCMDDefine.CMD_FILE_SEND; // 命令13
			byteHead[7] = (byte) 0x02; // 命令类型02		

			//包长度
			int nLen = nHeadLen + 4;
			SignUtils.putShort(byteHead, nLen, 8);
			
			byte[] bytePost = new byte[nLen + 4];
			System.arraycopy(byteHead, 0, bytePost, 0, nHeadLen);
			
			SignUtils.putInt(bytePost, nSqenceNum, nHeadLen);
					
			
			CRC32 crc32 = new CRC32();
			crc32.update(bytePost, 0, bytePost.length - 4);
			long nCrcValue = crc32.getValue();
			SignUtils.putLong(bytePost, nCrcValue, nLen);
		
			return bytePost;
		}

	}

}
