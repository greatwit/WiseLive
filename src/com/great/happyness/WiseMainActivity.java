package com.great.happyness;


import java.util.ArrayList;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;


import com.great.happyness.aidl.IActivityReq;
import com.great.happyness.aidl.IServiceListen;
import com.great.happyness.fragment.CommonTabLayout;
import com.great.happyness.fragment.CustomTabEntity;
import com.great.happyness.fragment.HomeFragment;
import com.great.happyness.fragment.OnTabSelectListener;
import com.great.happyness.fragment.PersonalFragment;
import com.great.happyness.fragment.ServiceFragment;
import com.great.happyness.fragment.TabEntity;
import com.great.happyness.service.WiFiAPService;
import com.great.happyness.utils.SysConfig;




public class WiseMainActivity extends FragmentActivity
{
	private static String TAG = "WiseMainActivity";

    private CommonTabLayout tabLayout;
    private OnTabSelectListener mTabSelectListener;
    private int currentTabPosition = 1;// 当前Tab选中的位置
	private HomeFragment homeFragment;
	private ServiceFragment serviceFragment;
	private PersonalFragment personalFragment;

	private String[] mTitles = { "主页", "服务","设置" };//"服务", "购物"
	private int[] mIconUnselectIds = { R.drawable.ic_home_normal,
			R.drawable.ic_service_normal, 
			R.drawable.ic_personal_normal };//R.drawable.ic_service_normal, R.drawable.ic_shopping_normal,
	private int[] mIconSelectIds = { R.drawable.ic_home_select,
			R.drawable.ic_service_select,
			R.drawable.ic_personal_select };//R.drawable.ic_service_select, R.drawable.ic_shopping_select,

	IActivityReq mActivityReq;
	WiseMainActivity mWiseThis;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mWiseThis = this;
        bindService();
        
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
    }

	@Override
	protected void onResume() {
		// 如果debug模式被打开，显示监控
		// AbMonitorUtil.openMonitor(this);
		super.onResume();
	}
    
    private void initVIew()
    {
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
    
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.w(TAG, "onServiceConnected 1");
        	mActivityReq = IActivityReq.Stub.asInterface(service);
        	try {
				mActivityReq.action(255, "abc");
				mActivityReq.registerListener(mServListener);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Log.w(TAG, "onServiceConnected 2");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        	Log.w(TAG, "onServiceDisconnected");
        }
    };
    
    private void bindService() {
        Intent intent = new Intent(this, WiFiAPService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        Log.w(TAG, "bindService");
    }
    
    IServiceListen mServListener = new IServiceListen.Stub() {
		@Override
		public void onAction(int action, Message msg) throws RemoteException {
			// TODO Auto-generated method stub
			Log.w(TAG, "IServiceListen onAction:"+action);
		}
    };
    
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
		Log.w(TAG,"onDestroy");
    	try {
			mActivityReq.unregisterListener(mServListener);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unbindService(mServiceConnection);
		super.onDestroy();
	}

}

