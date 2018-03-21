package com.great.happyness.fragment;

import com.great.happyness.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
	private FrameLayout progress;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = getActivity();
		// 注册EventBus
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(mContext).inflate(R.layout.fragment_home, null);
		init();
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void init() {
		progress = (FrameLayout) view.findViewById(R.id.progress);
		//item_opendoor_0 = (View) view.findViewById(R.id.item_opendoor_0);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}



}
