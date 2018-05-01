package com.great.happyness.camera;

import android.hardware.Camera;

/**
 * CameraHelper的统一接口
 * @author 
 * @date 2015-09-01
 */
@SuppressWarnings("deprecation")
public interface ICameraHelper {

    int getNumberOfCameras();

    
	Camera openCameraFacing(int facing) throws Exception;

    boolean hasCamera(int facing);

    void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo);
}
