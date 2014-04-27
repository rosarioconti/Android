package com.company.animationpool;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

/* Rosario Conti
 * Animation pool
 * Yo uare free to make your own modification!   
 */

public class AnimPoolUtil {	 

	public static void runAnimation(final Activity act, final int viewId, int anim, int startOffset, Rect pos) {
        Animation animation  = AnimationUtils.loadAnimation(act, anim);
        animation.reset();

        animation.setAnimationListener(new AnimationListener() {
        	 
			public void onAnimationEnd(Animation anim)
			{
				//Do something else with the view when finished
				//act.findViewById(viewId)....
			}
	
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
			}

        });
        animation.setStartOffset(startOffset);

        View view = act.findViewById(viewId);
        if (view != null){
        	view.clearAnimation();
        	view.startAnimation(animation);
        }
    }
}

