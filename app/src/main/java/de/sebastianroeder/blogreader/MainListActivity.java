package de.sebastianroeder.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainListActivity extends ListActivity {

    public static final String FEED_URL =
            "http://blog.teamtreehouse.com/api/get_recent_summary/?count=20";
    public static final String TAG = MainListActivity.class.getSimpleName();
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TITLE = "title";
    public static final String KEY_POSTS = "posts";
    protected ArrayList<HashMap<String, String>> mBlogPosts;
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            try {
                URL feedURL = new URL(FEED_URL);
                new FetchBlogDataTask().execute(feedURL);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException caught: ", e);
            }
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


    private void handleBlogData() {
        if (mBlogData != null) {
            displayBlogData();
        } else {
            displayAlertDialog();
        }
    }

    private void displayBlogData() {
        try {
            JSONArray jsonPosts = mBlogData.getJSONArray(KEY_POSTS);
            mBlogPosts = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < jsonPosts.length(); i++) {
                String blogTitle = getBlogTitle(jsonPosts, i);
                String blogAuthor = getBlogAuthor(jsonPosts, i);
                mBlogPosts.add(createBlogPostHashMap(blogAuthor, blogTitle));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException caught: ", e);
        }

        setupListAdapter();
    }

    private void setupListAdapter() {
        String[] keys = {KEY_TITLE, KEY_AUTHOR};
        int[] ids = { android.R.id.text1, android.R.id.text2};
        SimpleAdapter simpleAdapter= new SimpleAdapter(
                        this, mBlogPosts, android.R.layout.simple_list_item_2, keys, ids);
        setListAdapter(simpleAdapter);
    }

    private HashMap<String, String> createBlogPostHashMap(String blogAuthor, String blogTitle) {
        HashMap<String, String> blogPost = new HashMap<String, String>();
        blogPost.put(KEY_TITLE, blogTitle);
        blogPost.put(KEY_AUTHOR, blogAuthor);
        return blogPost;
    }

    private String getBlogTitle(JSONArray jsonPosts, int i) throws JSONException {
        return getJSONField(jsonPosts, i, KEY_TITLE);
    }

    private String getBlogAuthor(JSONArray jsonPosts, int i) throws JSONException {
        return getJSONField(jsonPosts, i, KEY_AUTHOR);
    }

    private String getJSONField(JSONArray jsonPosts, int i, String fieldName) throws JSONException {
        String escapedFieldContent = jsonPosts.getJSONObject(i).getString(fieldName);
        return Html.fromHtml(escapedFieldContent).toString();
    }

    private void displayAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_alert_title);
        builder.setMessage(R.string.blog_data_is_null_alert_message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();

        TextView emptyListTextView = (TextView) getListView().getEmptyView();
        emptyListTextView.setText(getString(R.string.no_blog_posts));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private class FetchBlogDataTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... feedURLs) {
            mProgressBar.setVisibility(View.VISIBLE);
            return fetchBlogData(feedURLs[0]);
        }

        @Override
        protected void onPostExecute(JSONObject blogData) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mBlogData = blogData;
            handleBlogData();
        }

        private JSONObject fetchBlogData(URL feedURL) {
            JSONObject blogData = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) feedURL.openConnection();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                blogData = new JSONObject(reader.readLine());
            } catch (java.io.IOException e) {
                Log.e(TAG, "IOException caught: ", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException caught: ", e);
            }
            return blogData;
        }
    }
}
