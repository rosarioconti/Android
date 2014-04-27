package com.company;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
	
	private WebView myWebView = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        myWebView = (WebView) findViewById(R.id.webView1);
        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        ((Button) findViewById(R.id.btn_asset)).setOnClickListener(BtnAssetListener);
        ((Button) findViewById(R.id.btn_sdcard)).setOnClickListener(BtnSDCardListener);
        
        String Path = "file:///android_asset/testing.htm";
        Log.d("[[Path]]", Path);
        myWebView.loadUrl(Path);
    } 
    
	public OnClickListener BtnAssetListener = new OnClickListener() 
	{

	    @Override
	    public void onClick(View buttonView) { 
	    	String Path = "file:///android_asset/testing.htm";
	        Log.d("[[Path]]", Path);
	        myWebView.loadUrl(Path);
	    }
	};

	/*
	 * You need to specify your own page on the sdcard or use the same from the 
	 * asset.
	 */
	public OnClickListener BtnSDCardListener = new OnClickListener() 
	{

	    @Override
	    public void onClick(View buttonView) { 
	        String Path = "file:///" + Environment.getExternalStorageDirectory() + 
	        		"/" + "index.html";
	        Log.d("[[Path]]", Path);
	        myWebView.loadUrl(Path);
	    }
	};

}
