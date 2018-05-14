package com.great.happyness.ui.bannerView;


public interface DataSetSubject {
    void registerSubscriber(DataSetSubscriber subscriber);
    void removeSubscriber(DataSetSubscriber subscriber);
    void notifySubscriber();
}
