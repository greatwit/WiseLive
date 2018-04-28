package com.great.happyness.ui.fragment;

import org.webrtc.webrtcdemo.SpinnerAdapter;
import org.webrtc.webrtcdemo.VideoEngine;

import com.great.happyness.R;
import com.great.happyness.utils.SysConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;


/**
 * 个人中心的Fragment
 * 
 * @author yanfa06
 * 
 */
public class PersonalFragment extends Fragment
		implements OnClickListener {
	private Context mContext;
	private final String TAG = "PersonalFragment";
	private View view;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = getActivity();

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		   Bundle savedInstanceState) 
	{
		view = LayoutInflater.from(mContext).inflate( R.layout.fragment_personal, null);
		Spinner spCodecSize = (Spinner) view.findViewById(R.id.spCodecSize);
	    SpinnerAdapter adapter = new SpinnerAdapter(mContext);
	    spCodecSize.setAdapter(adapter);
	    adapter.setDatas(VideoEngine.resolutionsAsString());
	    spCodecSize.setSelection(SysConfig.getSaveResolution(mContext));
	    spCodecSize.setOnItemSelectedListener(new OnItemSelectedListener() 
	    {
	        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) 
	        {
	          Log.w(TAG, "ItemSelected ResolutionIndex:"+position);
	          SysConfig.setSaveResolution(mContext, position);
	        }
	        public void onNothingSelected(AdapterView<?> arg0) 
	        {
	          Log.d(TAG, "No setting selected");
	        }
	    });
	    int play = SysConfig.getSavePlay(mContext);
	    CheckBox cbVideoReceive = (CheckBox) view.findViewById(R.id.cbVideoReceive);
	    if((play&0x1)==0x1)cbVideoReceive.setChecked(true);
	    cbVideoReceive.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View checkBox) {
	          CheckBox cbVideoReceive = (CheckBox) checkBox;
	          int temp = SysConfig.getSavePlay(mContext);
	          if(cbVideoReceive.isChecked()==true)
	        	  temp|=0x1;
	          else
	        	  temp&=0x6;
	          SysConfig.setSavePlay(mContext, temp);
	        }
	      });
	    
	    CheckBox cbVideoSend = (CheckBox) view.findViewById(R.id.cbVideoSend);
	    if((play&0x2)==0x2)cbVideoSend.setChecked(true);
	    cbVideoSend.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View checkBox) {
	          CheckBox cbVideoSend = (CheckBox) checkBox;
	          int temp = SysConfig.getSavePlay(mContext);
	          if(cbVideoSend.isChecked()==true)
	        	  temp|=0x2;
	          else
	        	  temp&=0x5;
	          SysConfig.setSavePlay(mContext, temp);
	        }
	      });
	    
	    CheckBox cbAudio = (CheckBox) view.findViewById(R.id.cbAudio);
	    if((play&0x4)==0x4)cbAudio.setChecked(true);
	    cbAudio.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View checkBox) {
	          CheckBox cbAudio = (CheckBox) checkBox;
	          int temp = SysConfig.getSavePlay(mContext);
	          if(cbAudio.isChecked()==true)
	        	  temp|=0x4;
	          else
	        	  temp&=0x3;
	          SysConfig.setSavePlay(mContext, temp);
	        }
	      });
	    
	    final EditText remoteAddr	 = (EditText)view.findViewById(R.id.remoteAddr);
		remoteAddr.setText("" + SysConfig.getSaveAddr(mContext));
		remoteAddr.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub
			}
	
			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				// TODO Auto-generated method stub
				Log.e("MainActivity", "emoteAddr: " + remoteAddr.getText().toString() );
				String addr = remoteAddr.getText().toString();
				SysConfig.setSaveAddr(mContext, addr);
			}
	
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

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
