package com.great.happyness.evenbus.subscribe;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.great.happyness.evenbus.event.DataEvent;


/**
 * Created by lgq on 2016/12/2.
 */

public class EventThread extends Thread{

    public EventThread(){
        EventBus.getDefault().register(this);
    }

    public void unregister(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void run() {
       while (true){ }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void  onEventBackgroundThread(DataEvent event){
        System.out.println("onEventBackgroundThread::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(DataEvent event){
        System.out.println("onEventAsync::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(DataEvent event){
        System.out.println("onEventMain::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventPosting(DataEvent event){
        System.out.println("onEventPosting::"+" "+Thread.currentThread().getName());
    }

}
