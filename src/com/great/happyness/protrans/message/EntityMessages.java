package com.great.happyness.protrans.message;

public class EntityMessages extends BaseMessage {
	
	private static EntityMessages mEntityInst = null;
	private MsgCommand 	mComMsg 	= null;
	private MsgFocus	mMsgFocus	= null;
	private MsgZoom		mMsgZoom	= null;
	
	public static EntityMessages getInst() {
		if(mEntityInst==null)
			mEntityInst = new EntityMessages();
		return mEntityInst;
	}
	
	public EntityMessages(){
	}
	
	public MsgCommand getMsgCommand() {
		if(mComMsg==null)
			mComMsg = new MsgCommand();
		return mComMsg;
	}
	
	public MsgFocus	getMsgFocus(){
		if(mMsgFocus==null)
			mMsgFocus = new MsgFocus();
		return mMsgFocus;
	}
	
	public MsgZoom	getMsgZoom(){
		if(mMsgZoom==null)
			mMsgZoom = new MsgZoom();
		return mMsgZoom;
	}
}

