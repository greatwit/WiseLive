package com.great.happyness.popwin;

import com.great.happyness.R;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ProcessBarPopWin extends PopupWindow {

    private View view;
    private TextView  mTxtstat;
    
	private String TAG = "ProcessBarPopWin";

    public ProcessBarPopWin(Context mContext, int width, int height) 
    {
        this.view = LayoutInflater.from(mContext).inflate(R.layout.popwin_processbar, null);


        mTxtstat = (TextView)view.findViewById(R.id.txtstat);
		
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(width);
        this.setWidth(height);

        // 设置弹出窗体可点击
        //this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.take_photo_anim);
    }
    
    public void setState(String str)
    {
    	mTxtstat.setText(str);
    }

}

