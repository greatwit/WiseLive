// IMusicPlayer.aidl
package com.great.happyness.service.aidl;
import  com.great.happyness.service.aidl.IServiceListen;


// Declare any non-default types here with import statements

interface IActivityReq {
        void registerListener(IServiceListen listener);
        void unregisterListener(IServiceListen listener);
        void action(in int action ,in String datum);
        
        boolean startUdpServer();
        boolean stopUdpServer();
        boolean isUdpServerRunning();
        int  sendData(String addr, int port, String data);
}
