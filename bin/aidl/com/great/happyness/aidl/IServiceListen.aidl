// IMusicPlayer.aidl
package com.great.happyness.aidl;



// Declare any non-default types here with import statements

interface IServiceListen {
        void onAction(in int action , in Message msg);
}
