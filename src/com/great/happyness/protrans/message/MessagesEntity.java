package com.great.happyness.protrans.message;

public class MessagesEntity extends MessageBase
{
	private static MessagesEntity mEntityInst = null;
	private CommandMessage mComMsg = null;
	
	public static MessagesEntity getInst() {
		if(mEntityInst==null)
			mEntityInst = new MessagesEntity();
		return mEntityInst;
	}
	
	public MessagesEntity(){
	}
	
	public CommandMessage getCommandMessage() {
		if(mComMsg==null)
			mComMsg = new CommandMessage();
		return mComMsg;
	}
}

