package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DiaryPageActivity extends Activity {

    public static final String base = "https://s3-eu-west-1.amazonaws.com/senbuyurken-photos/";
    private ListView de_list_view;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_page);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        DiaryEntryFetchTask deft = new DiaryEntryFetchTask();
        deft.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_diary_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        de_list_view.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    class DiaryEntryFetchTask extends AsyncTask<String, Void, List<DiaryEntryWrapper>> {

        private ProgressDialog progressDialog;
        private List<DiaryEntryWrapper> list = new ArrayList<DiaryEntryWrapper>();

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<DiaryEntryWrapper> list) {
            MyListViewAdapter adapter = new MyListViewAdapter(DiaryPageActivity.this, list);
            de_list_view = (ListView) findViewById(R.id.de_listview);
            de_list_view.setAdapter(adapter);
            progressBar.setVisibility(View.INVISIBLE);
        }

        protected List<DiaryEntryWrapper> doInBackground(String... urls) {

            SharedPreferences sp = DiaryPageActivity.this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

            // Instantiate Http Request Param List
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("un", sp.getString("username", null)));
            params.add(new BasicNameValuePair("t", sp.getString("token", null)));
            // Invoke RESTFull Web Service with Http parameters
            invokeRestWS(params);
            return list;
        }

        public void invokeRestWS(List<NameValuePair> params) {

            Uri.Builder builder = Uri.parse("http://82.222.86.241/senbuyurken/rest/diaryEntryRest/listDiaryEntry/").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));
                JSONArray arr = obj.getJSONArray("diaryEntry");
                for (int i = 0; i < arr.length(); i++) {
                    DiaryEntryWrapper dew = new DiaryEntryWrapper();
                    JSONObject o = (JSONObject) arr.get(i);
                    dew.setEntry_content(o.getString("entry_content"));
                    dew.setEntry_date(o.getString("entry_date"));
                    try {
                        URL newURL = new URL(base + o.getString("photo_url"));
                        Bitmap bm = BitmapFactory.decodeStream(newURL.openConnection().getInputStream());
                        dew.setImage(bm);
                        list.add(dew);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}