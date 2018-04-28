package com.great.happyness;


import java.util.ArrayList;

import org.greenrobot.eventbus.EventBus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.great.happyness.service.aidl.IActivityReq;
import com.great.happyness.service.aidl.IServiceListen;
import com.great.happyness.evenbus.event.CmdEvent;
import com.great.happyness.protrans.message.CommandMessage;
import com.great.happyness.protrans.message.ConstDef;
import com.great.happyness.protrans.message.MessagesEntity;
import com.great.happyness.service.aidl.IBindListen;
import com.great.happyness.service.aidl.ServiceControl;
import com.great.happyness.ui.fragment.CommonTabLayout;
import com.great.happyness.ui.fragment.CustomTabEntity;
import com.great.happyness.ui.fragment.HomeFragment;
import com.great.happyness.ui.fragment.OnTabSelectListener;
import com.great.happyness.ui.fragment.PersonalFragment;
import com.great.happyness.ui.fragment.ServiceFragment;
import com.great.happyness.ui.fragment.TabEntity;


public class WiseMainActivity extends FragmentActivity 
								implements IBindListen
{
	private String TAG = getClass().getSimpleName();

    private CommonTabLayout tabLayout;
    private OnTabSelectListener mTabSelectListener;
    private int currentTabPosition = 1;// 当前Tab选中的位置
	private HomeFragment homeFragment;
	private ServiceFragment serviceFragment;
	private PersonalFragment personalFragment;

	private String[] mTitles = { "文件", "相机", "设置" };//"服务", "购物"
	private int[] mIconUnselectIds = { R.drawable.ic_home_normal,
			R.drawable.ic_service_normal, 
			R.drawable.ic_personal_normal };//R.drawable.ic_service_normal, R.drawable.ic_shopping_normal,
	private int[] mIconSelectIds = { R.drawable.ic_home_select,
			R.drawable.ic_service_select,
			R.drawable.ic_personal_select };//R.drawable.ic_service_select, R.drawable.ic_shopping_select,

	
	private ServiceControl mServCont = ServiceControl.getInstance();
    private boolean mServiceRegisted = false;
    
    private MessagesEntity mEntityMsg = new MessagesEntity();
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main_fragment);
        initVIew();
        initFragment(savedInstanceState);
        
		ArrayList<CustomTabEntity> data = new ArrayList<CustomTabEntity>();
		for (int i = 0; i < mTitles.length; i++) {
			data.add(new TabEntity(mTitles[i], mIconSelectIds[i],
					mIconUnselectIds[i]));
		}
		tabLayout.setTabData(data);
		tabLayout.setCurrentTab(currentTabPosition);
		
        mServCont.startService(this, this);
        mServCont.bindService(this);
    }

	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub
		getBinderReq();
		EventBus.getDefault().post(new CmdEvent(ConstDef.UI_SER_CONNED));
		Log.e(TAG, "onServiceConnected()");
	}
    
	private IActivityReq getBinderReq(){
		IActivityReq actReq = ServiceControl.getInstance().getActivityReq();
		if(actReq!=null && mServiceRegisted == false){
			try {
				actReq.registerListener(mServListener);
				mServiceRegisted = true;
				Log.i(TAG, "actReq.registerListener");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			Log.e(TAG, "mActReq == null");
		
		return actReq;
	}
	
    IServiceListen mServListener = new IServiceListen.Stub() {
		@Override
		public void onAction(int action, Message msg) throws RemoteException {
			// TODO Auto-generated method stub
			//msg.what = action;
			String str = msg.getData().getString("data");
			mEntityMsg.inflateData(str);
			Log.i(TAG, "action:"+action + " data:"+str);
			
			switch(mEntityMsg.getType())
			{
				case ConstDef.TYPE_CMD:
			        CommandMessage comm = mEntityMsg.getCommandMessage();
			        comm.decodeData(mEntityMsg.getData());
					EventBus.getDefault().post(new CmdEvent(comm.getCmd()));
					Log.w(TAG, "CmdEvent:"+comm.getCmd());
					break;
			}
		}
    };
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
    private void initVIew(){
		mTabSelectListener = new OnTabSelectListener() {
			@Override
			public void onTabSelect(int position) {
				SwitchTo(position);
			}
			@Override
			public void onTabReselect(int position) {
			}
		};
		tabLayout = (CommonTabLayout) findViewById(R.id.tab_layout);
		// 点击监听
		tabLayout.setOnTabSelectListener(mTabSelectListener);
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 1200) {
                //弹出提示，可以有多种方式
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
	/** 初始化碎片 */
	private void initFragment(Bundle savedInstanceState) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (savedInstanceState != null) {
			homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
			serviceFragment = (ServiceFragment) getSupportFragmentManager().findFragmentByTag("serviceFragment");
			personalFragment = (PersonalFragment) getSupportFragmentManager().findFragmentByTag("personalFragment");
			currentTabPosition = savedInstanceState.getInt("HOME_CURRENT_TAB_POSITION");
		} else {
			homeFragment = new HomeFragment();
			serviceFragment = new ServiceFragment();
			personalFragment = new PersonalFragment();

			transaction.add(R.id.fl_body, homeFragment, "homeFragment");
			transaction.add(R.id.fl_body, serviceFragment, "serviceFragment");
			transaction.add(R.id.fl_body, personalFragment, "personalFragment");
		}
		transaction.commit();
		SwitchTo(currentTabPosition);
	}
    
	/** 切换 */
	private void SwitchTo(int position) {
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		switch (position) {
		// 主页
		case 0:
			transaction.show(homeFragment);
			transaction.hide(serviceFragment);
			// transaction.hide(shoppingFragment);
			transaction.hide(personalFragment);
			transaction.commitAllowingStateLoss();
			//ChangeTitleLayout(position);
			currentTabPosition = 0;
			// 重新获得已绑房产列表
			new Handler().post(new Runnable() {
				@Override
				public void run() {
				}
			});
			break;
		// 服务
		case 1:
			transaction.hide(homeFragment);
			transaction.show(serviceFragment);
			// transaction.hide(shoppingFragment);
			transaction.hide(personalFragment);
			transaction.commitAllowingStateLoss();
			//ChangeTitleLayout(position);
			currentTabPosition = 1;
			break;

		case 2:
			transaction.hide(homeFragment);
			transaction.hide(serviceFragment);
			// transaction.hide(shoppingFragment);
			transaction.show(personalFragment);
			transaction.commitAllowingStateLoss();
			//ChangeTitleLayout(position);
			currentTabPosition = 2;
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onDestroy() {
    	IActivityReq actReq = ServiceControl.getInstance().getActivityReq();
    	if(actReq!=null){
				try {
					if(actReq.isUdpServerRunning())
						actReq.stopUdpServer();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	}
    	
		if(getBinderReq()!=null && mServiceRegisted)
		try {
			getBinderReq().unregisterListener(mServListener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mServCont.unbindService(this);
		Log.w(TAG,"onDestroy");
		
		super.onDestroy();
	}

}

