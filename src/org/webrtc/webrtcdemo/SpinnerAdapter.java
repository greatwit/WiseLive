/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc.webrtcdemo;

import java.util.ArrayList;
import java.util.List;

import com.great.happyness.R;


import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class SpinnerAdapter extends BaseAdapter 
{
  public SpinnerAdapter(Context context) {
	  this.mContext = context;
  }
  
  String[] datas = null;
  Context mContext;


  public void setDatas(String[] datas) {
      this.datas = datas;
      notifyDataSetChanged();
  }

  @Override
  public int getCount() {
      return datas==null?0:datas.length;
  }

  @Override
  public String getItem(int position) {
      return datas==null?null:datas[position];
  }

  @Override
  public long getItemId(int position) {
      return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) 
  {
      ViewHodler hodler = null;
      if (convertView == null) 
      {
          hodler = new ViewHodler();
          convertView = LayoutInflater.from(mContext).inflate(R.layout.dropdownitems, null);
          hodler.mTextView = (TextView) convertView;
          convertView.setTag(hodler);
      } else {
          hodler = (ViewHodler) convertView.getTag();
      }

      hodler.mTextView.setText(datas[position]);

      return convertView;
  }

  private static class ViewHodler{
      TextView mTextView;
  }
  
}

