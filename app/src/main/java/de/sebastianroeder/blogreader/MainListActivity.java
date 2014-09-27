package de.sebastianroeder.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
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
    protected ArrayList<HashMap<String, String>> mBlogTitleAuthorMap;
    protected JSONObject mBlogData;
    protected JSONArray mBlogPosts;
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
                logException(e);
            }
        }
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            JSONObject clickedPost = mBlogPosts.getJSONObject(position);
            String clickedPostURL = clickedPost.getString("url");
            Intent intent = new Intent(this, BlogWebViewActivity.class);
            intent.setData(Uri.parse(clickedPostURL));
            startActivity(intent);
        } catch (JSONException e) {
            logException(e);
        }

    }

    private void logException(Exception e) {
        String exceptionType = e.getClass().getSimpleName();
        Log.e(TAG, exceptionType + " caught: ", e);
    }

    private void handleBlogData() {
        if (mBlogData != null) {
            displayBlogData();
        } else {
            displayAlertDialog();
        }
    }

    private void displayBlogData() {
        createTitleAuthorMap();
        setupListAdapter();
    }

    private void createTitleAuthorMap() {
        try {
            mBlogTitleAuthorMap = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < mBlogPosts.length(); i++) {
                String blogTitle = getBlogTitleAtIndex(i);
                String blogAuthor = getBlogAuthorAtIndex(i);
                mBlogTitleAuthorMap.add(createBlogPostHashMap(blogAuthor, blogTitle));
            }
        } catch (JSONException e) {
            logException(e);
        }
    }

    private void setupListAdapter() {
        String[] keys = {KEY_TITLE, KEY_AUTHOR};
        int[] ids = { android.R.id.text1, android.R.id.text2};
        SimpleAdapter simpleAdapter= new SimpleAdapter(
                        this, mBlogTitleAuthorMap, android.R.layout.simple_list_item_2, keys, ids);
        setListAdapter(simpleAdapter);
    }

    private HashMap<String, String> createBlogPostHashMap(String blogAuthor, String blogTitle) {
        HashMap<String, String> blogPost = new HashMap<String, String>();
        blogPost.put(KEY_TITLE, blogTitle);
        blogPost.put(KEY_AUTHOR, blogAuthor);
        return blogPost;
    }

    private String getBlogTitleAtIndex(int i) throws JSONException {
        return getJSONField(i, KEY_TITLE);
    }

    private String getBlogAuthorAtIndex(int i) throws JSONException {
        return getJSONField(i, KEY_AUTHOR);
    }

    private String getJSONField(int i, String fieldName) throws JSONException {
        String escapedFieldContent = mBlogPosts.getJSONObject(i).getString(fieldName);
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
            try {
                mBlogPosts = blogData.getJSONArray(KEY_POSTS);
            } catch (JSONException e) {
                logException(e);
            }

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
                logException(e);
            } catch (JSONException e) {
                logException(e);
            }
            return blogData;
        }
    }
}
