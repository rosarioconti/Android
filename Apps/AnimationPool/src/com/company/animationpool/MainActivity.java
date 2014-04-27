package com.company.animationpool;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_vista);
        
	  	AnimPoolUtil.runAnimation(this, R.id.line1, R.anim.anim_alpha, 0, null);
	  	AnimPoolUtil.runAnimation(this, R.id.line2, R.anim.anim_alpha, 500, null);
	  	AnimPoolUtil.runAnimation(this, R.id.line3, R.anim.anim_alpha, 1000, null);
	  	AnimPoolUtil.runAnimation(this, R.id.line4, R.anim.anim_alpha, 1500, null);
	  	AnimPoolUtil.runAnimation(this, R.id.line5, R.anim.anim_alpha, 2000, null);
     
    	Handler handler=new Handler();
    	Runnable r=new Runnable()
    	{
    	    public void run() 
    	    {
    		  	AnimPoolUtil.runAnimation(MainActivity.this, R.id.line1, R.anim.anim_slide_down, 0, null);
    		  	AnimPoolUtil.runAnimation(MainActivity.this, R.id.line2, R.anim.anim_slide_down, 500, null);
    		  	AnimPoolUtil.runAnimation(MainActivity.this, R.id.line3, R.anim.anim_slide_down, 1000, null);
    		  	AnimPoolUtil.runAnimation(MainActivity.this, R.id.line4, R.anim.anim_slide_down, 1500, null);
    		  	AnimPoolUtil.runAnimation(MainActivity.this, R.id.line5, R.anim.anim_slide_down, 2000, null);
    	    }
    	};
    	handler.postDelayed(r, 2300);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
