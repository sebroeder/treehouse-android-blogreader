package de.sebastianroeder.blogreader;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;


public class MainListActivity extends ListActivity {

    public static final int NUMBER_OF_POSTS = 20;
    public static final String FEED_URL =
            "http://blog.teamtreehouse.com/api/get_recent_summary/?count=";
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected String[] mBlogPostTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        if (isNetworkAvailable()) {
            try {
                URL feedURL = new URL(FEED_URL + NUMBER_OF_POSTS);
                GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
                getBlogPostsTask.execute(feedURL);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException caught: ", e);
            }
        } else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private class GetBlogPostsTask extends AsyncTask<URL, Void, Void> {

        @Override
        protected Void doInBackground(URL... feedURLs) {
            try {
                HttpURLConnection connection = (HttpURLConnection) feedURLs[0].openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String response = reader.readLine();
                    JSONObject jsonResponse = new JSONObject(response);
                    Log.v(TAG, "JSON status: " + jsonResponse.getString("status"));
                    JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
                    Log.i(TAG, "Number of posts in feed: " + jsonPosts.length());
                } else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            } catch (java.io.IOException e) {
                Log.e(TAG, "IOException caught: ", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException caught: ", e);
            }
            return null;
        }
    }
}
