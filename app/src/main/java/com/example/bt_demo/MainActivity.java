package com.example.bt_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bt_demo.util.RFIDConnectToClipboard;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends Activity {

	private static final String TAG = "BT_Demo";
	private static final boolean D = true;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final byte LED_SERVO_COMMAND = 2;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "Device_name";
	public static final String TOAST = "Toast";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	boolean Savebytes = false;
	// Array adapter for the conversation thread
	// private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	public BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public BluetoothChatService mChatService = null;
	public final Handler tHandler = new Handler();

	Timer tmrBlink;
	Timer RtmrBlink;
	Timer tmrRFIDConnectToClipboard;
	Timer tmrClipboard;
	private RFIDConnectToClipboard nRFIDConnectToClipboard;
	private ClipboardManager mSysClipboardManager;
	private ClipData mSysClipData;
	private String mSysThisClipString = "";
	private int mSysClipRunCount = 0;
	static int RFID_Data_Length = 38;
	static int RX_Count = 0;
	static int TX_Count = 0;
	// Layout Views
	private TextView mTitle;
	private TextView RX;
	private TextView TX, insendst;
	private TextView mClipboardTimeText;
	LinearLayout mLayout;
	TextView my_Text;
	CheckBox HEX_EN;
	EditText Edit1;
	String last_ente_str;
	private ScrollView mScrollView;
	private Button Mconnectb, mSendButton;
	private Toast mToast;
	/*
	* RFID 与其他APP通讯协议
	* */


	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		my_Text = (TextView) findViewById(R.id.Text1);
		RX = (TextView) findViewById(R.id.RX_C);
		TX = (TextView) findViewById(R.id.TX_C);
		nRFIDConnectToClipboard = new RFIDConnectToClipboard();
		nRFIDConnectToClipboard.init();
		mSysClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		mScrollView = (ScrollView) findViewById(R.id.sv);
		mLayout = (LinearLayout) findViewById(R.id.layout);
		Edit1 = (EditText) findViewById(R.id.entry);
		mTitle = (TextView) findViewById(R.id.ST);
		HEX_EN = (CheckBox) findViewById(R.id.checkbox1);


		//初始化ClipData对象
		setClipoardData("StartScanData");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 如果设备没有蓝牙模块。程序退出
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "没有发现蓝牙模块,程序中止", Toast.LENGTH_LONG).show();

			finish();
			return;
		}

		tmrBlink = new Timer(100, new Runnable() {
			public void run() {
				RX.setBackgroundColor(getResources()
						.getColor(R.color.LED_G_OFF));
				RX.setTextColor(getResources().getColor(R.color.white_));

				tmrBlink.stop();
			}
		});
		RtmrBlink = new Timer(100, new Runnable() {
			public void run() {
				TX.setBackgroundColor(getResources()
						.getColor(R.color.LED_R_OFF));
				TX.setTextColor(getResources().getColor(R.color.white_));
				RtmrBlink.stop();
			}
		});


		tmrClipboard = new Timer(1000, new Runnable() {
			public void run() {
				tmrClipboard.stop();
				mClipboardTimeText.setBackgroundColor(getResources()
						.getColor(R.color.LED_G_OFF));
				mClipboardTimeText.setTextColor(getResources().getColor(R.color.white_));
				//处理剪贴板数据
				String tbx_Data = my_Text.getText().toString();	//获取接受区的字符串
				String now_ClipboardData = "";
				String newCopy_ClipboardData = "";
				int beginIndex = -1;
				int endIndex = -1;

				do {
					//获取现在剪贴板数据
					now_ClipboardData = getClipoardData();
					//
					if(now_ClipboardData.length() != 0){
						mSysClipRunCount = mSysClipRunCount + 1;
						break;
					}
					if(mSysThisClipString.equals(now_ClipboardData) && !mSysThisClipString.equals("")){
						mSysClipRunCount = mSysClipRunCount + 1;
						break;
					}else{
						mSysClipRunCount = 1;
					}

					Log.d("message", "mSysClipRunCount is: " + String.valueOf(mSysClipRunCount));
					Log.d("message", "mSysThisClipString.length() is: " + String.valueOf(mSysThisClipString.length()) + "::::mSysThisClipString is: " + mSysThisClipString);
					Log.d("message", "now_ClipboardData.length() is: " + String.valueOf(now_ClipboardData.length()) + "::::now_ClipboardData is: " + now_ClipboardData);
					Log.d("message", "tbx_Data.length() is: " + String.valueOf(tbx_Data.length()) + "::::tbx_Data is: " + tbx_Data);

					//如果接收区的数据框内无数据
					if(tbx_Data.length() <= 0) {
						break;
					}

					//indexOf方法就是查找tbx_Data数据中，出现#的字符位置在哪里
					beginIndex = tbx_Data.indexOf("#");
					endIndex = tbx_Data.indexOf("#\n");

					//indexOf如果没有找到字符串，则会返回状态码-1
					if(beginIndex == -1 || endIndex == -1 ) {
						break;
					}
					beginIndex += 1;
					endIndex += 2;

					//substring方法是截取字符串。
					newCopy_ClipboardData = tbx_Data.substring(beginIndex, endIndex - 2);
					//将接收数据框数据更新
					mSysThisClipString = newCopy_ClipboardData;
					//将新数据粘贴至剪贴板
					setClipoardData(newCopy_ClipboardData);
					//截断字符串
					tbx_Data = tbx_Data.substring(endIndex);
					Log.d("message", "newCopy_ClipboardData.length() is: " + String.valueOf(newCopy_ClipboardData.length()) + "::::newCopy_ClipboardData is: " + newCopy_ClipboardData);
					Log.d("message", "tbx_Data is: " + tbx_Data);
				}while(false);

				//将接收数据框数据更新
				if(tbx_Data.length() > 0) {
					my_Text.setText(tbx_Data);
					tmrClipboard.start();
					Log.d("message", "tmrClipboard.start() ---------------------------------------tbx_Data.length() > 0)");
				}else if(tbx_Data.length() == 0|| mSysClipRunCount == 30){
					//停止处理
					my_Text.setText("");
					mSysClipRunCount = 0;
					tmrClipboard.stop();
					Toast.makeText(getApplicationContext(), "传输RFID结束!", Toast.LENGTH_SHORT).show();
					Log.d("message", "tmrClipboard.stop()");
				}else{
					tmrClipboard.start();
					Log.d("message", "tmrClipboard.start()");
				}
			}
		});

		tmrRFIDConnectToClipboard = new Timer(1000, new Runnable() {
			public void run() {
				String connect_CMD = "";
				String retResult = "";
				tmrRFIDConnectToClipboard.stop();
				connect_CMD = getClipoardData();
				retResult = nRFIDConnectToClipboard.of_timer_cmd(getApplicationContext(), connect_CMD);
				if(retResult.length() > 0){
					setClipoardData(retResult);
					//指令集, 刷新界面可视记录
					refreshMyText(nRFIDConnectToClipboard.RFID_Result);
				}
				tmrRFIDConnectToClipboard.start();
			}
		});
		tmrRFIDConnectToClipboard.start();

		//##############################################跟蓝牙设备进行连接的方法#####################################################//


		Mconnectb = (Button) findViewById(R.id.nnectin); //连接设备的按钮
		Mconnectb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mChatService.getState() != 3) {
					Intent serverIntent = new Intent(getApplicationContext(),
							DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				} else {
					mChatService.stop();
				}

			}
		});
		mSendButton = (Button) findViewById(R.id.ok);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.entry);
				String message = view.getText().toString();
				if (message == last_ente_str)
					;
				else {
					last_ente_str = message;
					Save_config("last_str", last_ente_str);
				}
				sendMessage(message);
			}
		});

		HEX_EN.setTextColor(getResources().getColor(R.color.Baclk));

		HEX_EN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				String distext = " ";
				if (isChecked) {
					// Toast.makeText(this, "Hex display Enable",
					// Toast.LENGTH_SHORT).show();
					// HEX_EN.setTextColor(getResources().getColor(R.color.Baclk));
					// my_Text.setText(Checkon);
					// Toast.makeText(getApplicationContext(), "Enable HEX",
					// Toast.LENGTH_SHORT).show();
					distext = "31 FE 5B";
					Edit1.setHint(distext);
				} else {
					distext = "ASCII  ";
					// my_Text.setText(Checkoff);
					Edit1.setHint(distext);
				}
				// distext=distext+"TEWDFWE ";
				// my_Text.setText(distext);
			}
		});
		last_ente_str = "";
		SharedPreferences config = this.getSharedPreferences("perference",
				MODE_PRIVATE);
		last_ente_str = config.getString("last_str", "");
		Edit1.setText(last_ente_str);
		is_conect();// 询问连接设备

//		String ls_temp = "";
//		ls_temp = nRFIDConnectToClipboard.of_decode("AA 03 10 01 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 ");
//		my_Text.append(ls_temp);
//		ls_temp = nRFIDConnectToClipboard.of_decode("D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 04 D0 1E CA 80 00 00 17 85 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 03 0D D0 1E CA 80 00 00 17 95 55 AA 11 10 00 30 00 30 34 0");
//		my_Text.append(ls_temp);
//		tmrClipboard.start();
	}


	void Save_config(String name, String Value) {
		SharedPreferences share = this.getSharedPreferences("perference",
				MODE_PRIVATE);
		Editor editor = share.edit();// 取得编辑器
		editor.putString(name, Value);// 存储配置 参数1 是key 参数2 是值
		editor.commit();// 提交刷新数据
	}

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	String lastdevice;

	void is_conect() {
		SharedPreferences config = this.getSharedPreferences("perference",
				MODE_PRIVATE);
		lastdevice = config.getString("lastdevice_addr", "");

		//首页弹出的对话框方法
		ShowAlertDialog();

	}

	private void ShowAlertDialog() {
		new AlertDialog.Builder(this)
				.setTitle("连接请求")
				.setIcon(android.R.drawable.ic_menu_info_details)
				.setMessage("软件并没有连接蓝牙设备，请连接设备")

				//连接上次的设备的Button
				.setPositiveButton("连接上次的设备",
						//AlertDigLogButton的监听
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (lastdevice == "") { // 没有设备，那就连接新设备吧

									Intent serverIntent = new Intent(
											getApplicationContext(),
											DeviceListActivity.class);
									startActivityForResult(serverIntent,
											REQUEST_CONNECT_DEVICE);
								} else {
									Log.d(TAG, "连接到最近的设备" + lastdevice);
									// Get the BLuetoothDevice object
									BluetoothDevice device = mBluetoothAdapter
											.getRemoteDevice(lastdevice);
									// Attempt to connect to the device
									mChatService.connect(device);
								}
							}
						})
				.setNegativeButton("连接新的设备",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//
								Intent serverIntent = new Intent(
										getApplicationContext(),
										DeviceListActivity.class);
								startActivityForResult(serverIntent,
										REQUEST_CONNECT_DEVICE);
							}
						}).show();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)Log.e(TAG, "++ ON START ++");
		if
			 (mBluetoothAdapter == null)
			return;
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if ((mChatService == null))
				setupChat();
			if (D)
				Log.e(TAG, "++ setupChat ++");
		}
	}// ----------onStart End-------------

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null) {
			mChatService.stop();
		}
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}// ----------onDestroy End-------------

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (D)
			Log.e(TAG, "+ ON RESUME +");

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
				if (D)
					Log.e(TAG, "mChatService.start");
			}
		}

	}// ----------onResume End-------------

	@Override
	public void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}/**/

	private void setupChat() {
		Log.d(TAG, "setupConnect()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			// byte[] send = message.getBytes();
			// String Encode = "gbk";
			byte[] send = null;
			if (HEX_EN.isChecked() != true) {
				try {
					send = message.getBytes("GBK");
				} catch (UnsupportedEncodingException e) {
					//
					e.printStackTrace();
				}
			} else
				send = getStringhex(message);
			Trig_TXLED(send.length);
			mChatService.write(send);
			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	boolean firstconnect = false, trycon = false;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					Toast.makeText(getApplicationContext(),
							"已经连接到-" + mConnectedDeviceName, Toast.LENGTH_LONG)
							.show();
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					Mconnectb.setText("断开连接");
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					Toast.makeText(getApplicationContext(), "正在尝试连接远程设备...",
							Toast.LENGTH_LONG).show();
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					// mTitle.setText(R.string.title_not_connected);
					mTitle.setText(R.string.title_not_connected);
					Mconnectb.setText("连接设备");
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = null;

				if (D)
					Log.d("收到数据", msg.arg1 + "字节");
				if (my_Text.getText().toString().length() > 65530) {
					my_Text.setText("");
					RX_Count = 0;
				}
				//Trig_RXLED(msg.arg1);
				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				if (HEX_EN.isChecked() != true) { // ASC
					try {
						readMessage = new String(readBuf, 0, msg.arg1, "GBK");
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(
								"Unsupported   encoding   type.");
					}
				} else { // 16进制
					try {
						readMessage = getHexString(readBuf, 0, msg.arg1);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(
								"Unsupported   encoding   type.");
					}
				}

				readMessage = nRFIDConnectToClipboard.of_decode(readMessage);
				//my_Text.append(readMessage);
				if(readMessage.length() > 0){
					Trig_RXLED(readMessage.length());
				}

				/*
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				if(my_Text.getText().toString().length() == 0 ){
					Toast.makeText(getApplicationContext(), "扫描RFID开始，请在RFID标签盘点界面【启动】接收!", Toast.LENGTH_SHORT).show();
				}
				readMessage = dataDecode(readMessage);
				my_Text.append(readMessage);
				//my_Text.setText(my_Text.getText().toString() + readMessage);
				if(readMessage.length() > 0){
					Trig_RXLED(readMessage.length());
				}
				//启动剪贴板计时器
				if(my_Text.getText().toString().length() > 0 ) {
					tmrClipboard.start();
				}
				//doString();
				//Trig_RXLED(msg.arg1);
				*/
				tHandler.post(mScrollToBottom);// 更新Scroll
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);

				RX_Count = 0;
				TX_Count = 0;
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

	};

	private String dataDecode(String ag_str) {
		String str_ag_Str = "";
		String str_Tex_Result = my_Text.getText().toString();
		String str_Ret_Result = "";
		String str_RFID = "";
		int li_RFIDCount = 0;
		int li_FindBeginIndex = -1;
		int li_FindEndIndex = -1;
		int li_ModResult = 0;

		str_ag_Str = ag_str;
		//确保数据完整性
		do {
			//替换空白,数据头,数据结尾
			str_ag_Str = str_ag_Str.replaceAll(" ", "");
			str_ag_Str = str_ag_Str.replaceAll("AA03100155", "");//Begin
			str_ag_Str = str_ag_Str.replaceAll("AA03120055", "");//End
			//判断总长度是否大于/等于38
			if(str_ag_Str.length() < RFID_Data_Length){
				break;
			}
			//判断总长度是否成立
			li_ModResult = str_ag_Str.length() % RFID_Data_Length;
			if(li_ModResult != 0){
				if(!str_ag_Str.substring(0, 2).equals("AA")){
					if(str_ag_Str.substring(li_ModResult, li_ModResult + 2).equals("AA")){
						str_ag_Str = str_ag_Str.substring(li_ModResult);
					}else{
						li_FindBeginIndex = str_ag_Str.indexOf("55AA");
						if(li_FindBeginIndex != -1){
							li_FindBeginIndex += 2;
							str_ag_Str = str_ag_Str.substring(li_FindBeginIndex);
						}
					}
				}

				if(!str_ag_Str.substring(str_ag_Str.length() - 2).equals("55")){
					if(str_ag_Str.substring(str_ag_Str.length() - li_ModResult - 2, str_ag_Str.length() - li_ModResult).equals("55")){
						str_ag_Str = str_ag_Str.substring(0, str_ag_Str.length() - li_ModResult);
					}else{
						li_FindEndIndex = str_ag_Str.lastIndexOf("55");
						if(li_FindEndIndex != -1){
							str_ag_Str = str_ag_Str.substring(0,li_FindEndIndex + 2);
						}
					}
				}
			}
		}while(!str_ag_Str.substring(0, 2).equals("AA") && !str_ag_Str.substring(str_ag_Str.length() - 2).equals("55"));

		//解码数据,分解RFID
		while(str_ag_Str.length() >= RFID_Data_Length){
			//找到数据头分隔符
			if(str_ag_Str.substring(0, 2).equals("AA")){
				li_FindBeginIndex = 0;
			}else{
				li_FindBeginIndex = str_ag_Str.indexOf("AA");
				str_ag_Str = str_ag_Str.substring(li_FindBeginIndex, str_ag_Str.length() - li_FindBeginIndex);
				li_FindBeginIndex = 0;
			}

			//找到数据结尾分隔符
			if(str_ag_Str.length() >= RFID_Data_Length && str_ag_Str.substring(36, RFID_Data_Length).equals("55")){
				li_FindEndIndex = RFID_Data_Length;
			}

			//取截断数据，取中间数据
			str_RFID = str_ag_Str.substring(li_FindBeginIndex, li_FindEndIndex);
			str_RFID = str_ag_Str.substring(12, 36);
			//判断成立则输出结果
			if(str_Tex_Result.indexOf(str_RFID) == -1 && str_Ret_Result.indexOf(str_RFID) == -1){
				str_RFID = "#" + str_RFID + "#\n";
				str_Ret_Result = str_Ret_Result.concat(str_RFID);
				li_RFIDCount += 1;
				Log.d("message", "str_Ret_Result"+ String.valueOf(li_RFIDCount) +" is: " + str_Ret_Result);
			}
			//截断数据继续查找RFID
			if(str_ag_Str.length() / RFID_Data_Length > 1){
				str_ag_Str = str_ag_Str.substring(RFID_Data_Length, str_ag_Str.length() - RFID_Data_Length);
			}else{
				str_ag_Str = "";
			}
		};
		return str_Ret_Result;
	}




	//把初始化数据粘贴到剪贴板方法
	private void setClipoardData(String temp) {
		ClipData mSysClipData = ClipData.newPlainText("Text", temp);    //第一次初始化的值
		//Log.d("clipData", "传给剪贴板的值是" + mSysClipboardManager.getPrimaryClip().toString());
		mSysClipboardManager.setPrimaryClip(mSysClipData);
		//Toast.makeText(getApplicationContext(), "获取RFID成功!", Toast.LENGTH_SHORT).show();
	}

	private String getClipoardData() {
		//Log.d("status", "粘贴方法开始");
		/**
		 *  myClipboard.hasPrimaryClip()判断是否存在copy都值，我们把null传给了剪贴板。
		 *      所有是有个null值都
		 *
		 *      当第一个条件为false当时候，就不会执行第二条
		 */

		if (mSysClipboardManager.hasPrimaryClip() && mSysClipboardManager.equals(null)) {
			//Log.d("clipData", "mSysClipboard.hasPrimaryClip()的值是:" + String.valueOf(mSysClipboardManager.hasPrimaryClip()));
			//copy内容是null值，停止粘贴。
			//mSysClipData = mSysClipboard.getPrimaryClip();
			String textData1 = "adc";
			//ClipData.Item item = mSysClipData.getItemAt(0);
			//textData1 = item.coerceToText(this).toString();
			//Log.d("clipData", "剪贴板的是是空的,输出:" + mSysClipData.toString());
			//Log.d("clipData", String.valueOf(textData1.length()));
			//检测到null值了，把copy到第二个值传入剪贴板。
			//Toast.makeText(getApplicationContext(), "存在null值,不粘贴", Toast.LENGTH_SHORT).show();
			return textData1;
		} else {
			//copy内容不等于null值，需要粘贴。
			//Log.d("clipData", "开始粘贴");
			//获取copy都内容
			//Log.d("clipData", "mSysClipboard.hasPrimaryClip()的值:" + String.valueOf(mSysClipboardManager.hasPrimaryClip()));
			mSysClipData = mSysClipboardManager.getPrimaryClip();
			//Log.d("clipData", "剪贴板的值是：" + mSysClipData);
			String textData = null;
			ClipData.Item item = mSysClipData.getItemAt(0);
			textData = item.coerceToText(this).toString();
			//Log.d("clipData", "剪贴板的值是：" + textData);
			///Toast.makeText(getApplicationContext(), "粘贴成功", Toast.LENGTH_SHORT).show();

			return textData;
		}
	}

	private void refreshMyText(ArrayList<String> arrayList){
		my_Text.setText("");

		for (String i: arrayList) {
			my_Text.append(i+"\n");
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.exit:

			break;
		}
		return true;
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				Save_config("lastdevice_addr", address);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
				// ("蓝牙启动成功...");
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "Bluetooth not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	private Runnable mScrollToBottom = new Runnable() {
		@Override
		public void run() {
			//
			// Log.d("UI", "ScrollY: " + mScrollView.getScrollY());
			int off = mLayout.getMeasuredHeight() - mScrollView.getHeight();
			if (off > 0) {
				mScrollView.scrollTo(0, off);
			}
		}
	};

	public void Trig_RXLED(int c) {
		RX_Count += c;
		RX.setText("RX " + RX_Count);

		RX.setBackgroundColor(getResources().getColor(R.color.LED_G_ON));
		RX.setTextColor(getResources().getColor(R.color.Baclk));

		if (tmrBlink.getIsTicking())
			tmrBlink.restart();
		else {
			tmrBlink.start();
		}
	}

	public void Trig_TXLED(int c) {
		TX_Count += c;
		TX.setText("TX " + TX_Count);
		TX.setBackgroundColor(getResources().getColor(R.color.LED_R_ON));
		TX.setTextColor(getResources().getColor(R.color.Baclk));

		if (RtmrBlink.getIsTicking())
			RtmrBlink.restart();
		else {
			RtmrBlink.start();
		}
	}

	static final char[] HEX_CHAR_TABLE = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String getHexString(byte[] raw, int offset, int count)
			throws UnsupportedEncodingException {
		StringBuffer hex = new StringBuffer();
		for (int i = offset; i < offset + count; i++) {
			int v = raw[i] & 0xFF;
			hex.append(HEX_CHAR_TABLE[v >>> 4]);
			hex.append(HEX_CHAR_TABLE[v & 0xF]);
			hex.append(" ");
		}
		return hex.toString();
	}

	public byte[] getStringhex(String ST) {
		ST = ST.replaceAll(" ", "");
		// Log.v("getStringhex",ST);
		char[] buffer = ST.toCharArray();
		byte[] Byte = new byte[buffer.length / 2];
		int index = 0;
		int bit_st = 0;
		for (int i = 0; i < buffer.length; i++) {
			int v = (int) (buffer[i] & 0xFF);

			if (((v > 47) && (v < 58)) || ((v > 64) && (v < 71))
					|| ((v > 96) && (v < 103))) {
				if (bit_st == 0) {// 高位
					Log.v("getStringhex", "F True");
					Byte[index] |= (getASCvalue(buffer[i]) * 16);
					Log.v("getStringhex", String.valueOf(Byte[index]));
					bit_st = 1;
				} else {// 低位
					Byte[index] |= (getASCvalue(buffer[i]));
					Log.v("getStringhex", "F false");
					Log.v("getStringhex", String.valueOf(Byte[index]));
					bit_st = 0;
					index++;
				}
			} else if (v == 32) { // 空格
				Log.v("getStringhex", "spance");
				if (bit_st == 0)
					;
				else {
					index++;
					bit_st = 0;
				}
			} else
				continue;
		}
		bit_st = 0;
		return Byte;
	}

	public static byte getASCvalue(char in) {
		byte out = 0;
		switch (in) {
		case '0':
			out = 0;
			break;
		case '1':
			out = 1;
			break;
		case '2':
			out = 2;
			break;
		case '3':
			out = 3;
			break;
		case '4':
			out = 4;
			break;
		case '5':
			out = 5;
			break;
		case '6':
			out = 6;
			break;
		case '7':
			out = 7;
			break;
		case '8':
			out = 8;
			break;
		case '9':
			out = 9;
			break;
		case 'a':
			out = 10;
			break;
		case 'b':
			out = 11;
			break;
		case 'c':
			out = 12;
			break;
		case 'd':
			out = 13;
			break;
		case 'e':
			out = 14;
			break;
		case 'f':
			out = 15;
			break;
		case 'A':
			out = 10;
			break;
		case 'B':
			out = 11;
			break;
		case 'C':
			out = 12;
			break;
		case 'D':
			out = 13;
			break;
		case 'E':
			out = 14;
			break;
		case 'F':
			out = 15;
			break;
		}
		return out;
	}

}
