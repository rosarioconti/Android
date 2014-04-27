package com.ytdemo;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An Activity showing multiple YouTubeThumbnailViews in an adapter for display in a List. 
 * Tthe video is played by using a YouTubePlayerFragment.
 */
@TargetApi(13)
public final class VideoListActivity extends Activity implements OnFullscreenListener {
 
  /** The duration of the animation sliding up the video in portrait. */
  private static final int ANIMATION_DURATION_MILLIS = 300;
  /** The padding between the video list and the video in landscape orientation. */
  private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;

  public static VideoListFragment listFragment;
  private VideoFragment videoFragment;
  public static Context context;
  
  private View videoBox;
  private View closeButton;

  private boolean isFullscreen;
  private static boolean isPortrait;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
 
    setContentView(R.layout.video_list);
 
    listFragment = (VideoListFragment) getFragmentManager().findFragmentById(R.id.list_fragment);
    videoFragment =
        (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);

    videoBox = findViewById(R.id.video_box);
    closeButton = findViewById(R.id.close_button);
    closeButton.setVisibility(View.INVISIBLE);
    
    if (!isTabletDevice(this.getApplicationContext() )) //Check if is a "phone"...
     	videoBox.setVisibility(View.INVISIBLE);
    
    context = this;
    Button sb = (Button) findViewById(R.id.searchBtn);
    sb.setOnClickListener(searchList);
	
    layout();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
	
    layout();
  }

  @Override
  public void onFullscreen(boolean isFullscreen) {
    this.isFullscreen = isFullscreen;

    layout();
  }

	public OnClickListener searchList = new OnClickListener() 
	{
	   	@Override
		public void onClick(View v) 
	   	{	   		
	   		TextView searchText = (TextView) findViewById(R.id.searchText);
	   		searchVideos sv = new searchVideos();
			if (!searchText.getText().toString().isEmpty())
			{
				String normStr = searchText.getText().toString().replaceAll(" ", "+"); 
				sv.execute(normStr, null, null);
			}
	   	}
	};

  
  /**
   * Sets up the layout programatically for the three different states. Portrait, landscape or
   * fullscreen+landscape. This has to be done programmatically because we handle the orientation
   * changes ourselves in order to get fluent fullscreen transitions, so the xml layout resources
   * do not get reloaded.
   */
  public void layout() {
    isPortrait =
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

    listFragment.getView().setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
    listFragment.setLabelVisibility(true);//isPortrait); //Always show the label (Port/Land)
    closeButton.setVisibility(isPortrait ? View.VISIBLE : View.GONE);

    int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
    int screenHeight = dpToPx(getResources().getConfiguration().screenHeightDp);

    if (isFullscreen) {
      videoBox.setTranslationY(0); // Reset any translation that was applied in portrait.
      setLayoutSize(videoFragment.getView(), MATCH_PARENT, MATCH_PARENT);
      setLayoutSizeAndGravity(videoBox, MATCH_PARENT, MATCH_PARENT, Gravity.TOP | Gravity.LEFT);
    } else if (isPortrait) {
        if(!isTabletDevice(this.getApplicationContext()))
            setLayoutSizeAndGravity(listFragment.getView(), screenWidth , screenHeight - 200, Gravity.BOTTOM );
        else
            setLayoutSizeAndGravity(listFragment.getView(), screenWidth /2, screenHeight - 200, Gravity.BOTTOM );
    	
      setLayoutSize(videoFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
      setLayoutSizeAndGravity(videoBox, MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM);
 
    } else {
      videoBox.setTranslationY(0); // Reset any translation that was applied in portrait.
      
      if(!isTabletDevice(this.getApplicationContext()))
          setLayoutSizeAndGravity(listFragment.getView(), screenWidth , screenHeight - 200, Gravity.BOTTOM );
      else
          setLayoutSizeAndGravity(listFragment.getView(), screenWidth /2, screenHeight - 200, Gravity.BOTTOM );

      //Video Fragment
      int videoWidth = screenWidth - screenWidth / 2 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP); 
      setLayoutSize(videoFragment.getView(), videoWidth, WRAP_CONTENT);
      
      setLayoutSizeAndGravity(videoBox, videoWidth, WRAP_CONTENT,
          Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    }
    


  }

  @SuppressLint("NewApi")
public void onClickClose(@SuppressWarnings("unused") View view) {
    listFragment.getListView().clearChoices();
    listFragment.getListView().requestLayout();
    videoFragment.pause();
    videoBox.animate()
        .translationYBy(videoBox.getHeight())
        .setDuration(ANIMATION_DURATION_MILLIS)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            videoBox.setVisibility(View.INVISIBLE);
          }
        });
  }

  /**
   * A fragment that shows the list of videos.
   */
  public static class VideoListFragment extends ListFragment {

    private static List<VideoEntry> VIDEO_LIST;
    private static List<VideoEntry> list;
    private static PageAdapter adapter;
    private static View videoBox;
    private static ListFragment activity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      list = new ArrayList<VideoEntry>();
      VIDEO_LIST = Collections.unmodifiableList(list);
      adapter = new PageAdapter(getActivity(), VIDEO_LIST);
      activity = this;
    }
   
    public static void setList(List<VideoEntry> l)
    {
    	list = new ArrayList<VideoEntry>();
    	list = l;
    	VIDEO_LIST = Collections.unmodifiableList(list);  
    	adapter = new PageAdapter(VideoListActivity.context, VIDEO_LIST);
    	((ListFragment) activity).setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      videoBox = getActivity().findViewById(R.id.video_box);
      getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
      String videoId = VIDEO_LIST.get(position).videoId;

      VideoFragment videoFragment =
          (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
      videoFragment.setVideoId(videoId);

      // The videoBox is INVISIBLE if no video was previously selected, so we need to show it now.
      if(isTabletDevice(getActivity())){	  
	      if (videoBox.getVisibility() != View.VISIBLE) {
	        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	          // Initially translate off the screen so that it can be animated in from below.
	          videoBox.setTranslationY(videoBox.getHeight());
	        }
	        videoBox.setVisibility(View.VISIBLE);
	      }
	      // If the fragment is off the screen, we animate it in.
	      if (videoBox.getTranslationY() > 0) {
	        videoBox.animate().translationY(0).setDuration(ANIMATION_DURATION_MILLIS);
	      }
      } else {
    	  //open a new Activity to show the video
	        videoBox.setVisibility(View.INVISIBLE);
		    Intent myIntent = new Intent(getActivity(), FullscreenActivity.class);
		    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		    myIntent.putExtra("videoId", videoId);
		    this.startActivityForResult(myIntent, 0);
      	
      }
    }

    @Override
    public void onDestroyView() {
      super.onDestroyView();

      adapter.releaseLoaders();
    }

    public void setLabelVisibility(boolean visible) {
      adapter.setLabelVisibility(visible);
    }

  }
  
  
//----------------------------------
  
  
  public static final class VideoFragment extends YouTubePlayerFragment
      implements OnInitializedListener {

    private YouTubePlayer player;
    private String videoId;

    public static VideoFragment newInstance() {
      return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    @Override
    public void onDestroy() {
      if (player != null) {
        player.release();
      }
      super.onDestroy();
    }

    public void setVideoId(String videoId) {
      if (videoId != null && !videoId.equals(this.videoId)) {
        this.videoId = videoId;
        if (player != null) {
          player.cueVideo(videoId);
        }
      }
    }

    public void pause() {
      if (player != null) {
        player.pause();
      }
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean restored) {
      this.player = player;
      player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
      player.setOnFullscreenListener((VideoListActivity) getActivity());
      if (!restored && videoId != null) {
        player.cueVideo(videoId);
      }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
      this.player = null;
    }

  }

  
  // Utility methods for layouting.

  private int dpToPx(int dp) {
    return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
  }

  private static void setLayoutSize(View view, int width, int height) {
    LayoutParams params = view.getLayoutParams();
    params.width = width;
    params.height = height;
    view.setLayoutParams(params);
  }

  private static void setLayoutSizeAndGravity(View view, int width, int height, int gravity) {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
    params.width = width;
    params.height = height;
    params.gravity = gravity;
    view.setLayoutParams(params);
  }

   
  public static boolean isTabletDevice(Context context) {
	    TelephonyManager manager = 
	        (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
	        //Tablet
	        return true;
	    } else {
	        //Mobile
	        return false; 
	    }
  }
  
  //If we Want to check for tablet size too
  /*
  public static boolean isTabletDevice(Context activityContext) {
	    // Verifies if the Generalized Size of the device is XLARGE to be
	    // considered a Tablet
	  	// Also if telephony is supported
	    boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout & 
	                        Configuration.SCREENLAYOUT_SIZE_MASK) == 
	                        Configuration.SCREENLAYOUT_SIZE_XLARGE);

	    // If XLarge, checks if the Generalized Density is at least MDPI
	    // (160dpi)
	    if (xlarge) {
	        DisplayMetrics metrics = new DisplayMetrics();
	        Activity activity = (Activity) activityContext;
	        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

	        // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,
	        // DENSITY_TV=213, DENSITY_XHIGH=320
	        if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT
	                || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH
	                || metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM
	                || metrics.densityDpi == DisplayMetrics.DENSITY_TV
	                || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {

	            // Yes, this is a tablet!
	            return true;
	        }
	    }

	    // No, this is not a tablet!
	    return false;
  }
  */
  
}


