package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiaryPageActivity extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private ListView de_list_view;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_diary_page, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setBackgroundColor(Color.TRANSPARENT);
        swipeLayout.setRefreshing(false);
        swipeLayout.setOnRefreshListener(this);

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.new_note);
        imageButton.bringToFront();
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDiaryEntryActivity();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            DiaryEntryFetchTask deft = new DiaryEntryFetchTask();
            deft.execute();
        }
    }

    @Override
    public void onRefresh() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeLayout.setRefreshing(false);
        DiaryEntryFetchTask deft = new DiaryEntryFetchTask();
        deft.execute();
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_diary_entry:
                navigateToDiaryEntryActivity();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        if (de_list_view != null)
            de_list_view.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void navigateToDiaryEntryActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(), DiaryEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    class DiaryEntryFetchTask extends AsyncTask<String, Void, List<DiaryEntryWrapper>> {

        private List<DiaryEntryWrapper> list = new ArrayList<DiaryEntryWrapper>();

        private SharedPreferences sp;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            progressDialog.setContentView(R.layout.progress_layout);

        }

        @Override
        protected void onPostExecute(List<DiaryEntryWrapper> list) {
            Activity a = getActivity();
            MyListViewAdapter adapter = new MyListViewAdapter(a, list);
            de_list_view = (ListView) a.findViewById(R.id.de_listview);

            TextView tw = (TextView) a.findViewById(R.id.empty_msg);
            ImageButton ib = (ImageButton) a.findViewById(R.id.new_note);

            if (list.size() == 0) {

                tw.setVisibility(View.VISIBLE);
                ib.setVisibility(View.VISIBLE);
            }

            if (list.size() > 0) {
                tw.setVisibility(View.INVISIBLE);
                ib.setVisibility(View.INVISIBLE);
            }

            de_list_view.setAdapter(adapter);
            progressDialog.dismiss();
        }

        protected List<DiaryEntryWrapper> doInBackground(String... urls) {
            Activity a = getActivity();
            sp = a.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("un", sp.getString("username", null)));
            params.add(new BasicNameValuePair("t", sp.getString("token", null)));
            invokeRestWS(params);
            return list;
        }

        public void invokeRestWS(List<NameValuePair> params) {

            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/listDiaryEntry/").buildUpon();
            Uri.Builder builder2 = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/getDiaryEntryImage/").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            HttpPost httpPost2 = new HttpPost(builder2.toString());
            HttpClient client2 = new DefaultHttpClient();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {

                    JSONObject obj = new JSONObject(responseStr);
                    Object arr = obj.get("diaryEntry");

                    if (arr instanceof JSONObject) {
                        DiaryEntryWrapper dew = new DiaryEntryWrapper();
                        JSONObject o = (JSONObject) arr;
                        dew.setEntry_content(o.getString("entry_content"));
                        dew.setEntry_date(o.getString("entry_date"));
                        try {
                            params.add(new BasicNameValuePair("photo_url", o.getString("photo_url")));

                            httpPost2.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                            HttpResponse response2 = client2.execute(httpPost2);
                            Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(EntityUtils.toByteArray(response2.getEntity())));
                            dew.setImage(bm);
                            list.add(dew);
                            params.remove(params.size() - 1);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (arr instanceof JSONArray) {

                        for (int i = 0; i < ((JSONArray) arr).length(); i++) {
                            DiaryEntryWrapper dew = new DiaryEntryWrapper();
                            JSONObject o = (JSONObject) ((JSONArray) arr).get(i);
                            dew.setEntry_content(o.getString("entry_content"));
                            dew.setEntry_date(o.getString("entry_date"));
                            try {
                                params.add(new BasicNameValuePair("photo_url", o.getString("photo_url")));

                                httpPost2.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                                HttpResponse response2 = client2.execute(httpPost2);
                                Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(EntityUtils.toByteArray(response2.getEntity())));
                                dew.setImage(bm);
                                list.add(dew);
                                params.remove(params.size() - 1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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