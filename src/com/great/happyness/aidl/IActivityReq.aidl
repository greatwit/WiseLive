// IMusicPlayer.aidl
package com.great.happyness.aidl;
import  com.great.happyness.aidl.IServiceListen;


// Declare any non-default types here with import statements

interface IActivityReq {
        void action(in int action ,in String datum);
        void registerListener(IServiceListen listener);
        void unregisterListener(IServiceListen listener);
}
