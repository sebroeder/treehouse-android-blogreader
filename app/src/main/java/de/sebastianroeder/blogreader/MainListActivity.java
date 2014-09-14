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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


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
                Log.i(TAG, "HTTP Response Code: " + responseCode);
            } catch (java.io.IOException e) {
                Log.e(TAG, "IOException caught: ", e);
            }
            return null;
        }
    }
}
