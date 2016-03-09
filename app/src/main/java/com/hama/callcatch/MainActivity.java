package com.hama.callcatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends Activity {

	private String html = "";
	private Handler mHandler;

	private Socket socket;

	private BufferedReader networkReader;
	private BufferedWriter networkWriter;

	private String ip = null; //
	private int port = 0; // PORT번호
// 자꾸 터져서 지움
	@Override
	protected void onStop() {
		super.onStop();
		try {
			checkUpdate.interrupt();
			socket.close();
			networkWriter.close();
			networkReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),"Error!", Toast.LENGTH_LONG).show();
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn1 = (Button)findViewById(R.id.button);
		final EditText ips = (EditText)findViewById(R.id.editText);
		final EditText ports = (EditText)findViewById(R.id.editText2);
		final TextView tv = (TextView) findViewById(R.id.textView);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		// register PhoneStateListener
		btn1.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (ips.getText().toString() != null && ports.getText().toString() != null || !ips.getText().toString().equals("") && !ports.getText().toString().equals("")) {
					ip = String.valueOf(ips.getText());
					port = Integer.valueOf(String.valueOf(ports.getText()));
					Toast.makeText(getApplicationContext(), ip + " / " + port, Toast.LENGTH_LONG).show();
//					Intent intent = new Intent();
//					intent.setAction(Intent.ACTION_MAIN);
//					intent.addCategory(Intent.CATEGORY_HOME);
//					startActivity(intent);
				}
			}
		});


	}

	private PhoneStateListener callStateListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber)
		{
			//  React to incoming call.
			String number=incomingNumber;
			// If phone ringing
			if(state==TelephonyManager.CALL_STATE_RINGING)
			{
				Toast.makeText(getApplicationContext(),"전화가 왔습니다.", Toast.LENGTH_LONG).show();
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
				mHandler = new Handler();

				try {
					setSocket(ip, port);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(checkUpdate != null && checkUpdate.isAlive()) {
					/** 이부분이 변경됨 */
					checkUpdate.interrupt();
				}

// 쓰레드를 다시 구성하고 돌림
				checkUpdate = new Thread(new Runnable() {
					@Override
					public void run() {

						// 데이터 정리후 3초후에 결과를 보여줌
						SystemClock.sleep(5000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// 화면의 UI에 변경된 사항을 업데이트함

								try {
									String line = null;
									Log.w("ChattingStart", "Start Thread");
									while (line == null) {
										Log.w("Chatting is running", "chatting is running");
										line = networkReader.readLine();
										html = line;
										mHandler.post(showUpdate);

									}
								} catch (Exception e) {
									Log.i("Exception", "PhoneStateListener() e = " + e);
								}

							}
						});
					}
				});
				checkUpdate.start();

				PrintWriter out = new PrintWriter(networkWriter, true);
				String return_msg = "dd ";
				out.println(return_msg + number);

			}
			// If incoming call received
			if(state== TelephonyManager.CALL_STATE_OFFHOOK)
			{

			}


			if(state==TelephonyManager.CALL_STATE_IDLE)
			{

			}
		}
	};

	private Thread checkUpdate = new Thread() {

		public void run() {
			try {
				String line = null;
				Log.w("ChattingStart", "Start Thread");
				while (line == null) {
					Log.w("Chatting is running", "chatting is running");
					line = networkReader.readLine();
					html = line;
					mHandler.post(showUpdate);
				}
			} catch (Exception e) {
				Log.i("Exception", "PhoneStateListener() e = " + e);
			}
		}

	};

	private Runnable showUpdate = new Runnable() {

		public void run() {
			Toast.makeText(MainActivity.this, "PC로 전송되었습니다.", Toast.LENGTH_SHORT).show();
		}

	};
//sssssss
	public void setSocket(String ip, int port) throws IOException {

		try {
			socket = new Socket(ip, port);
			networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}

	}

}
