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
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainListActivity extends ListActivity {

    public static final String FEED_URL =
            "http://blog.teamtreehouse.com/api/get_recent_summary/?count=20";
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected String[] mBlogPostTitles;
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            try {
                mProgressBar.setVisibility(View.VISIBLE);
                URL feedURL = new URL(FEED_URL);
                FetchBlogDataTask fetchBlogDataTask = new FetchBlogDataTask();
                fetchBlogDataTask.execute(feedURL);
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


    private void updateList() {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (mBlogData != null) {
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                int numberOfPosts = jsonPosts.length();
                mBlogPostTitles = new String[numberOfPosts];
                for (int i = 0; i < numberOfPosts; i++) {
                    String escapedTitle = jsonPosts.getJSONObject(i).getString("title");
                    String unescapedTitle = Html.fromHtml(escapedTitle).toString();
                    mBlogPostTitles[i] = unescapedTitle;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException caught: ", e);
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,android.R.layout.simple_list_item_1, mBlogPostTitles);
            setListAdapter(arrayAdapter);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error_alert_title);
            builder.setMessage(R.string.blog_data_is_null_alert_message);
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();

            TextView emptyListTextView = (TextView) getListView().getEmptyView();
            emptyListTextView.setText(getString(R.string.no_blog_posts));
        }
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
            return fetchBlogData(feedURLs[0]);
        }

        @Override
        protected void onPostExecute(JSONObject blogData) {
            mBlogData = blogData;
            updateList();
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
