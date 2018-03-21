package com.great.happyness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.great.happyness.wifi.WifiListAdapter;




public class HotSpotMainActivity extends Activity implements View.OnClickListener 
{
	private static String TAG = "HotSpotMainActivity";
	
    private ListView listView;
    private Button btn_create_hostspot;
    private Button btn_close_hostspot;
    
    private Button btn_native_send;
    private Button btn_upper_listen;
    private Button btn_upper_send;
    private Button btn_search;
    private TextView tvhot_state;
    private TextView tvconn_state;

    private WifiManager mWifiManager;
    private WifiListAdapter wifiListAdapter;
    private WifiConfiguration config;
    private int wcgID;

    /**
     * 热点名称
     */
    private final String WIFI_HOTSPOT_SSID 		= "great_"+android.os.Build.MODEL;
    private static final String WIFI_SHARE_KEY  = "123456789";
    
    /**
     * 端口号
     */
    private static final int UDP_PORT = 8010;

    private static final int WIFICIPHER_NOPASS 	= 1;
    private static final int WIFICIPHER_WEP 	= 2;
    private static final int WIFICIPHER_WPA 	= 3;

    public static final int DEVICE_CONNECTING 	= 1;	//有设备正在连接热点
    public static final int DEVICE_CONNECTED 	= 2;	//有设备连上热点
    public static final int SEND_MSG_SUCCSEE 	= 3;	//发送消息成功
    public static final int SEND_MSG_ERROR 		= 4;	//发送消息失败
    public static final int GET_MSG 			= 6;	//获取新消息

    private boolean 	mbListen				= false, mSending = false;
    
    
    /**
     * 连接线程
     */
    //private ConnectThread connectThread;

    /**
     * 监听线程
     */
    //private ListenerThread listenerThread;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hostspot);
        initVIew();
        initBroadcastReceiver();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!isWifiApEnabled()) {
			tvhot_state.setText("SSID closeed");
		}else {
			tvhot_state.setText("SSID:" + WIFI_HOTSPOT_SSID );
		}
    }

    private void initVIew() 
    {
        listView = (ListView) findViewById(R.id.listView);
        btn_create_hostspot = (Button) findViewById(R.id.btn_create_hostspot);
        btn_close_hostspot 	= (Button) findViewById(R.id.btn_close_hostspot);
        btn_upper_send 		= (Button) findViewById(R.id.btn_upper_send);
        btn_search 			= (Button) findViewById(R.id.btn_search);
        btn_upper_listen	= (Button) findViewById(R.id.btn_upper_listen);
        btn_native_send		= (Button) findViewById(R.id.btn_native_send);
        tvhot_state 		= (TextView) findViewById(R.id.tvhot_state);
        tvconn_state 		= (TextView) findViewById(R.id.tvconn_state);

        btn_create_hostspot.setOnClickListener(this);
        btn_close_hostspot.setOnClickListener(this);
        btn_upper_send.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_upper_listen.setOnClickListener(this);
        btn_native_send.setOnClickListener(this);

        wifiListAdapter = new WifiListAdapter(this, R.layout.wifi_list_item);
        listView.setAdapter(wifiListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	mWifiManager.disconnect();
                final ScanResult scanResult = wifiListAdapter.getItem(position);
                String capabilities 		= scanResult.capabilities;
                int type = WIFICIPHER_WPA;
                if (!TextUtils.isEmpty(capabilities)) {
                    if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                        type = WIFICIPHER_WPA;
                    } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = WIFICIPHER_WEP;
                    } else {
                        type = WIFICIPHER_NOPASS;
                    }
                }
                config = isExsits(scanResult.SSID);
                if (config == null) 
                {
                    if (type != WIFICIPHER_NOPASS) 
                    {//需要密码
                        final EditText editText = new EditText(HotSpotMainActivity.this);
                        final int finalType = type;
                        new AlertDialog.Builder(HotSpotMainActivity.this).setTitle("请输入Wifi密码").setIcon(
                                android.R.drawable.ic_dialog_info).setView(
                                editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) 
                            {
                                Log.w("AAA", "editText.getText():" + editText.getText());
                                config = createWifiInfo(scanResult.SSID, editText.getText().toString(), finalType);
                                connect(config);
                            }
                        })
                                .setNegativeButton("取消", null).show();
                        return;
                    } else {
                        config = createWifiInfo(scanResult.SSID, "", type);
                        connect(config);
                    }
                } else {
                    connect(config);
                }
            }
        });
    }

    private void connect(WifiConfiguration config) 
    {
    	tvconn_state.setText("连接中...");
        wcgID = mWifiManager.addNetwork(config);
        mWifiManager.enableNetwork(wcgID, true);
    }

    private void initBroadcastReceiver() 
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        registerReceiver(receiver, intentFilter);
    }

    /**
     * 获取开启便携热点后自身热点IP地址
     * @param context
     * @return
     */
    public static String getHotspotLocalIpAddress(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifimanager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    public boolean isWifiApEnabled() 
    {  
        try 
        {  
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");  
            method.setAccessible(true);  
            return (Boolean) method.invoke(mWifiManager);  
        } catch (NoSuchMethodException e) 
        {  
            e.printStackTrace();  
        } catch (Exception e) 
        {  
            e.printStackTrace();  
        }  
        return false;  
    }  
    
    /**
     * 获取连接WiFi后的IP地址
     * @return
     */
    public String getIpAddressFromHotspot(Context context) {
    	WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifimanager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.gateway;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }
    
    public String getConnectInfo(Context context)
    {
    	WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo info = wifimanager.getConnectionInfo();  
        int strength = info.getRssi();  
        int speed = info.getLinkSpeed();  
        int ip 	  = info.getIpAddress(); 
        String bssid = info.getBSSID();  
        String ssid  = info.getSSID();  
        String units = WifiInfo.LINK_SPEED_UNITS; 
        String ipStr = ((ip) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "" +
                		"." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
        Log.w(TAG, "SSID:"+ssid + " BSSID:"+bssid + " speed:"+speed +"units:"+units +"ip:"+ip + "ipStr:"+ipStr);
        return ipStr;
    }
    
    @Override
    public void onClick(View v) 
    {
    	Intent intent = null;
        switch (v.getId()) 
        {
            case R.id.btn_create_hostspot:
                createWifiHotspot();
                break;
                
            case R.id.btn_close_hostspot:
                closeWifiHotspot();
                break;
                
            case R.id.btn_search:
                search();
                break;
              
        }
    }

    /**
     * 创建Wifi热点
     */
    private void createWifiHotspot() 
    {
        if (mWifiManager.isWifiEnabled()) 
        {
            //如果wifi处于打开状态，则关闭wifi,
        	mWifiManager.setWifiEnabled(false);
        }
        
        WifiConfiguration config = new WifiConfiguration();
        config.SSID 		= WIFI_HOTSPOT_SSID;
        config.preSharedKey = WIFI_SHARE_KEY;
        config.hiddenSSID 	= true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(mWifiManager, config, true);
            if (enable) 
            {
            	tvhot_state.setText("SSID:" + WIFI_HOTSPOT_SSID );
            } else {
            	tvhot_state.setText("创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvhot_state.setText("创建热点失败");
        }
    }

    /**
     * 关闭WiFi热点
     */
    public void closeWifiHotspot() 
    {
        try {
            Method method 				= mWifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config 	= (WifiConfiguration) method.invoke(mWifiManager);
            Method method2 				= mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(mWifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        tvhot_state.setText("热点已关闭");
        tvconn_state.setText("wifi已关闭");
    }

    /**
     * 获取连接到热点上的手机ip
     *
     * @return
     */
    private ArrayList<String> getConnectedIP() 
    {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) 
            {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    /**
     * 搜索wifi热点
     */
    private void search() 
    {
        if (!mWifiManager.isWifiEnabled()) 
        {
            //开启wifi
        	mWifiManager.setWifiEnabled(true);
        }
        mWifiManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private Handler handler = new Handler() 
    {
        @Override
        public void handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
                case DEVICE_CONNECTING:
                    //connectThread = new ConnectThread(listenerThread.getSocket(),handler);
                    //connectThread.start();
                	tvhot_state.setText("设备正在连接");
                    break;
                    
                case DEVICE_CONNECTED:
                    //listenerThread = new ListenerThread(PORT, handler);
                    //listenerThread.start();
                    
                    //connectThread = new ConnectThread(listenerThread.getSocket(),handler);
                    //connectThread.start();
                    tvhot_state.setText("设备连接成功");
                    break;
                    
                case SEND_MSG_SUCCSEE:
                	tvhot_state.setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                    
                case SEND_MSG_ERROR:
                	tvhot_state.setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                    
                case GET_MSG:
                	tvhot_state.setText("收到消息:" + msg.getData().getString("MSG"));
                    break;
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) 
            {
                Log.w("BBB", "SCAN_RESULTS_AVAILABLE_ACTION");
                // wifi已成功扫描到可用wifi。
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                wifiListAdapter.clear();
                wifiListAdapter.addAll(scanResults);
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) 
            {
                Log.w("BBB", "WifiManager.WIFI_STATE_CHANGED_ACTION");
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) 
                {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //获取到wifi开启的广播时，开始扫描
                    	mWifiManager.startScan();
                        break;
                        
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
                        break;
                }
            } 
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
            {
                Log.w("BBB", "WifiManager.NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) 
                {
                	tvconn_state.setText("连接已断开");
                } 
                else if (info.getState().equals(NetworkInfo.State.CONNECTED)) 
                {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    tvconn_state.setText("已连接到网络:" + wifiInfo.getSSID());
                    Log.w("AAA","wifiInfo.getSSID():"+wifiInfo.getSSID()+"  WIFI_HOTSPOT_SSID:"+WIFI_HOTSPOT_SSID);
                    if (wifiInfo.getSSID().equals(WIFI_HOTSPOT_SSID)) 
                    {
                        //如果当前连接到的wifi是热点,则开启连接线程
                        new Thread(new Runnable() 
                        {
                            @Override
                            public void run() 
                            {
                                //try {
                                    ArrayList<String> connectedIP = getConnectedIP();
                                    for (String ip : connectedIP) 
                                    {
                                        if (ip.contains(".")) 
                                        {
                                            Log.w("AAA", "IP:" + ip); 
                                            //Socket socket = new Socket(ip, PORT);
                                            //connectThread = new ConnectThread(socket, handler);
                                            //connectThread.start();
                                        }
                                    }

//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }).start();
                    }
                } 
                else 
                {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                    	tvconn_state.setText("连接中...");
                    } else if (state == state.AUTHENTICATING) {
                    	tvconn_state.setText("正在验证身份信息...");
                    } else if (state == state.OBTAINING_IPADDR) {
                    	tvconn_state.setText("正在获取IP地址...");
                    } else if (state == state.FAILED) {
                    	tvconn_state.setText("连接失败");
                    }
                }

            }
           /* else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                    text_state.setText("连接已断开");
                    wifiManager.removeNetwork(wcgID);
                } else {
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    text_state.setText("已连接到网络:" + wifiInfo.getSSID());
                }
            }*/
        }
    };

    /**
     * 判断当前wifi是否有保存
     *
     * @param SSID
     * @return
     */
    private WifiConfiguration isExsits(String SSID) 
    {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) 
        {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) 
            {
                return existingConfig;
            }
        }
        return null;
    }

    public WifiConfiguration createWifiInfo(String SSID, String password, int type) 
    {
        Log.w("AAA", "SSID = " + SSID + "password " + password + "type ="
                + type);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

}

