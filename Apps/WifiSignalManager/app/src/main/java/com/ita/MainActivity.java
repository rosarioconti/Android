package com.ita;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ita.models.WifiConnection;
import com.ita.wifisignallevel.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer timer, timerB;
    private static Context context;
    private static TextView tv,tvconnections, tvip;
    private static Button btn;

    private Handler handler = new Handler();
    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();
        tv = (TextView) findViewById(R.id.label);
        tvip = (TextView) findViewById(R.id.ip);
        tvconnections = (TextView) findViewById(R.id.connections);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiConnectionManager.createSharedInstanceWifiConnectionManager(context.getApplicationContext(), true);
        registerReceiver(WifiConnectionManager.sharedInstance(), new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("DISCONNECT BUTTON---", " disconnecting... : " + wifi.disconnect());

            }
        });

        timer = new Timer();
        timer.schedule(new MyTimerTask(), 1000, 4000);

        timerB = new Timer();
        timerB.schedule(new MyTimerTaskB(), 1000,1000);
    }

    private class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WifiConnection result = WifiConnectionManager.sharedInstance().searchAndConnectToStrongestBSSID();
                    if (result != null) {
                        tv.setText(result.toString());
                    } else {
                        tv.setText("not connected!");
                    }
            }
            });
        }
    }

    private class MyTimerTaskB extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvip.setText("ip: " + "  " + WifiConnectionManager.sharedInstance().getCurrentWifiInfo());
                    tvconnections.setText(formatText(WifiConnectionManager.sharedInstance().getCurrentConnections()));
                }
            });
        }
    }



    private String formatText(List<ScanResult> scans) {
        String temp = "";
        if (scans != null) {
            for (ScanResult scanResult : scans) {
                if (scanResult.SSID.contains("MDC-ENT-DEVICES")) {
                    int signalLevel = WifiManager.calculateSignalLevel(scanResult.level, 10);
                    Log.d("CURRENT RESULTS", scanResult.SSID + " - " + scanResult.BSSID + " Level is " + signalLevel + " out of 5");
                    temp += "CURRENT RESULTS" + scanResult.SSID + " - " + scanResult.BSSID + " Level is " + signalLevel + " out of 5.\n";
                }
            }
        }
        return temp;
    }
}
