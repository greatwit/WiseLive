package com.great.happyness.ui.fragment;

import com.great.happyness.R;
import com.great.happyness.tranfiles.core.FileInfo;
import com.great.happyness.tranfiles.navitab.NavigationTabStrip;
import com.great.happyness.tranfiles.ui.fragment.FileInfoFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * 主页的Fragment
 * 
 * @author yanfa06
 * 
 */
public class HomeFragment extends Fragment
		implements OnClickListener{
	private final String TAG = "HomeFragment";
	private Context mContext;
	private View view;

    private ViewPager mViewPager;
    private NavigationTabStrip mCenterNavigationTabStrip;

    /**
     * 应用，图片，音频， 视频 文件Fragment
     */
    FileInfoFragment mCurrentFragment;
    FileInfoFragment mApkInfoFragment;
    FileInfoFragment mJpgInfoFragment;
    FileInfoFragment mMp3InfoFragment;
    FileInfoFragment mMp4InfoFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = getActivity();
		// 注册EventBus
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(mContext).inflate(R.layout.fragment_home, null);
        mViewPager = (ViewPager)view.findViewById(R.id.vp);
        mCenterNavigationTabStrip = (NavigationTabStrip)view.findViewById(R.id.nts_center);
        initData();
        return view;
	}

    /**
     * 初始化数据
     */
    @SuppressWarnings("deprecation")
	private void initData() {
        mApkInfoFragment = FileInfoFragment.newInstance(FileInfo.TYPE_APK);
        mJpgInfoFragment = FileInfoFragment.newInstance(FileInfo.TYPE_JPG);
        mMp3InfoFragment = FileInfoFragment.newInstance(FileInfo.TYPE_MP3);
        mMp4InfoFragment = FileInfoFragment.newInstance(FileInfo.TYPE_MP4);
        mCurrentFragment = mApkInfoFragment;

        String[] titles = getResources().getStringArray(R.array.array_res);
        mViewPager.setAdapter(new ResPagerAdapter(getFragmentManager(), titles));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) { //应用

                } else if (position == 1) { //图片

                } else if (position == 2) { //音乐

                } else if (position == 3) { //视频

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setOffscreenPageLimit(4);
        mCenterNavigationTabStrip.setViewPager(mViewPager, 2);
    }

    /**
     * 资源的PagerAdapter
     */
    class ResPagerAdapter extends FragmentPagerAdapter {
        String[] sTitleArray;

        public ResPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public ResPagerAdapter(FragmentManager fm, String[] sTitleArray) {
            this(fm);
            this.sTitleArray = sTitleArray;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){ 
                mCurrentFragment = mApkInfoFragment;//应用
            }else if(position == 1){ 
                mCurrentFragment = mJpgInfoFragment;//图片
            }else if(position == 2){ 
                mCurrentFragment = mMp3InfoFragment;//音乐
            }else if(position == 3){ 
                mCurrentFragment = mMp4InfoFragment;//视频
            }
            return mCurrentFragment;
        }

        @Override
        public int getCount() {
            return sTitleArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sTitleArray[position];
        }
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
