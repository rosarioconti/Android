package com.ytdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.ytdemo.VideoListActivity.VideoListFragment;

class searchVideos extends AsyncTask<String, Void, JSONObject > {

 	final static String URL = "http://gdata.youtube.com/feeds/api/videos?max-results=10&v=2&alt=json&key="+DeveloperKey.DEVELOPER_KEY + "&q=";
    @Override
    protected JSONObject doInBackground(String... params) {
    		
		try {
            return execQuery(params[0]);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }      
 
    @Override
    protected void onPostExecute(JSONObject result) 
    {
 		JSONObject itemArray;
		try {
			List<VideoEntry> l = new ArrayList<VideoEntry>();
			if (result!=null) {
				itemArray = result.getJSONObject("feed");// getJSONArray("feed");
				if ( itemArray.length() > 0 ) {
					//Log.d("[itemArray.length]",""+itemArray.length());
					JSONArray item = itemArray.getJSONArray("entry");
					//Log.d("[item.length]",""+item.length());
					for(int i=0; i < item.length(); i++) 
					{
						JSONObject nodo    = item.getJSONObject(i);//.getJSONObject("title");
						JSONObject title   = nodo.getJSONObject("title");
						JSONObject media   = nodo.getJSONObject("media$group");
						JSONObject videoid = media.getJSONObject("yt$videoid");
						JSONArray thumb   = media.getJSONArray("media$thumbnail");
						JSONObject thumblink = thumb.getJSONObject(0);
						/*
					    Log.d("[result]"+i," title=" + title.getString("$t") + 
					    		" videoid: "+ videoid.getString("$t") +
					    		" thumb: "+ thumblink.getString("url")
					    		);
					    */
					    l.add(new VideoEntry(title.getString("$t"),videoid.getString("$t")));
					}
					VideoListFragment.setList(l);
				} 
			} 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("ClientErr: ", e.toString());
		}
    }
    
    /*
    @Override
    protected void onProgressUpdate(Void... values) {
    	
    }
    */
    
    public JSONObject execQuery(String v)  throws ClientProtocolException, IOException, JSONException {
        	StringBuilder url = new StringBuilder(URL);
        	url.append(v);
        	Log.d("[search string]",""+ url);
	        DefaultHttpClient client = new DefaultHttpClient();
	        HttpGet get = new HttpGet(url.toString());
	        HttpResponse r = client.execute(get);
		        int status = r.getStatusLine().getStatusCode();
		        if (status == 200) {
		            HttpEntity e = r.getEntity();
		            String data = EntityUtils.toString(e);
		            JSONObject timeline = new JSONObject(data);
		            return timeline;
		        } else {
		            //Toast.makeText(context.t, "error", Toast.LENGTH_SHORT);
			 		 Log.d("[error status]",""+ status);
		       	 return null;
		        }
    }
    
}   
