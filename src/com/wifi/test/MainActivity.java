package com.wifi.test;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
	private ProgressBar progress;
	
	private int status = 0;
	
    private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		check();
		
		progress = (ProgressBar) findViewById(R.id.progress);
		new Thread(new Runnable() {
            public void run() {
                while (true) {
                    status = doWork();

                    handler.post(new Runnable() {
                        public void run() {
                        	progress.setProgress(status);
                        }
                    });
                    
                    try {
                		Thread.sleep(5000);
                	} catch(Exception ex) { }
                }
            }
        }).start();
		
		findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				check();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.action_settings) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void check() {
		((LinearLayout)findViewById(R.id.radios)).removeAllViews();
		
		getPreferences(Context.MODE_PRIVATE).edit().putString("bssid", "").commit();
		
		RadioGroup group = new RadioGroup(MainActivity.this);
		
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		manager.startScan();
		
		List<ScanResult> result = manager.getScanResults();
		for(ScanResult r : result) {
			final String bssid = r.BSSID;
			
			RadioButton btn = new RadioButton(MainActivity.this);
			btn.setText(String.format("[%s] %s - %d\n", r.BSSID, r.SSID, WifiManager.calculateSignalLevel(r.level, 100)));
			btn.setPadding(0, 10, 0, 10);
			btn.setHeight(50);
			
			LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
	                RadioGroup.LayoutParams.MATCH_PARENT,
	                RadioGroup.LayoutParams.MATCH_PARENT);
	        
			btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						getPreferences(Context.MODE_PRIVATE).edit().putString("bssid", bssid).commit();
					}					
				}
			});
			
			group.addView(btn, 0, layoutParams);
		}
		
		((LinearLayout)findViewById(R.id.radios)).addView(group);
	}
	
	private int doWork() {
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		
		String bssid = preferences.getString("bssid", ""); 
		
		if(bssid == "") {
			return 0;
		}
		
		ScanResult mb = null;
		
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		manager.startScan();
		
		List<ScanResult> result = manager.getScanResults();
		for(ScanResult r : result) {
			if(r.BSSID.equals(bssid)) {
				mb = r;
				
				break;
			}
		}
		
		if(mb != null) {
			return WifiManager.calculateSignalLevel(mb.level, 100);
		} else {
			return 0;
		}
	}
}
