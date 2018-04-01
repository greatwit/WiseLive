package com.great.happyness.popwin;


import com.great.happyness.R;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
  

public class CameraPopwin extends PopupWindow{
	private View mainView;
	private LinearLayout layout_touch, layout_camera;

  public CameraPopwin(Activity paramActivity, View.OnClickListener paramOnClickListener, int width, int height){
		 super(paramActivity);
		 //窗口布局
	    mainView = LayoutInflater.from(paramActivity).inflate(R.layout.popwin_camera, null);
	    //分享布局
	    layout_touch = ((LinearLayout)mainView.findViewById(R.id.layout_touch));
	    //复制布局 
	    layout_camera  = (LinearLayout)mainView.findViewById(R.id.layout_camera);
	    //设置每个子布局的事件监听器
	    if (paramOnClickListener != null){
	    	layout_touch.setOnClickListener(paramOnClickListener);
	    	layout_camera.setOnClickListener(paramOnClickListener);
	    }
	    setContentView(mainView);
	    //设置宽度
	    setWidth(width);
	    //设置高度
	    setHeight(height);
	    //设置显示隐藏动画
	    setAnimationStyle(R.style.animpopwin);
	    //设置背景透明
        // 实例化一个ColorDrawable颜色为半透明
	    setBackgroundDrawable(new ColorDrawable(0xb0000000));
  	}
}
