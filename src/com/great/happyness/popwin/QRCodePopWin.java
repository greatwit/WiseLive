package com.great.happyness.popwin;

import com.great.happyness.R;
import com.great.happyness.qrcode.QRCodeUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QRCodePopWin extends PopupWindow {

    private View view;
    private ImageView mImgqr;
    private TextView  mTxtqr;
    
	private String TAG = "QRCodePopWin";

    public QRCodePopWin(Context mContext, View.OnClickListener itemsOnClick, int width, int height) 
    {
        this.view = LayoutInflater.from(mContext).inflate(R.layout.popwin_create_qr, null);

        mImgqr = (ImageView)view.findViewById(R.id.imgqr);
        mTxtqr = (TextView)view.findViewById(R.id.txtqr);
		
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(width);
        this.setWidth(height);

        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) mImgqr.getLayoutParams();
        params.width =(int) (width*0.8);
        params.height=(int) (height*0.8);
        mImgqr.setLayoutParams(params);
        // 设置弹出窗体可点击
        //this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.take_photo_anim);
    }
    
	public void createQR(Context context, String data, int width, int height) {
		try {
			Bitmap bitmap = QRCodeUtil.createQRImage(data, width, height,
			BitmapFactory.decodeResource(context.getResources(), R.drawable.qr_mobile));

			mImgqr.setImageBitmap(bitmap);
			mTxtqr.setText(data);
		} catch (Exception e) {
			Log.d(TAG, "绑定生成错误：" + e.getMessage());
		}

	}

}
