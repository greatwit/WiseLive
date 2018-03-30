// IMusicPlayer.aidl
package com.great.happyness.aidl;
import  com.great.happyness.aidl.IServiceListen;


// Declare any non-default types here with import statements

interface IActivityReq {
        void registerListener(IServiceListen listener);
        void unregisterListener(IServiceListen listener);
        void action(in int action ,in String datum);
        int  sendData(String addr, int port, String data);
}
