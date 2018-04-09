/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\weipengWorkspace\\AppWorkspace\\WiseLive\\src\\com\\great\\happyness\\aidl\\IServiceListen.aidl
 */
package com.great.happyness.aidl;
// Declare any non-default types here with import statements

public interface IServiceListen extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.great.happyness.aidl.IServiceListen
{
private static final java.lang.String DESCRIPTOR = "com.great.happyness.aidl.IServiceListen";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.great.happyness.aidl.IServiceListen interface,
 * generating a proxy if needed.
 */
public static com.great.happyness.aidl.IServiceListen asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.great.happyness.aidl.IServiceListen))) {
return ((com.great.happyness.aidl.IServiceListen)iin);
}
return new com.great.happyness.aidl.IServiceListen.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onAction:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.Message _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.Message.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.onAction(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.great.happyness.aidl.IServiceListen
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onAction(int action, android.os.Message msg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(action);
if ((msg!=null)) {
_data.writeInt(1);
msg.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onAction, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onAction(int action, android.os.Message msg) throws android.os.RemoteException;
}
