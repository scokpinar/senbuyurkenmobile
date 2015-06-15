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
import android.os.Debug;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DiaryPageActivity extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private ListView de_list_view;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeLayout;

    public static void logHeap() {
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
        Double available = new Double(Debug.getNativeHeapSize()) / 1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize()) / 1048576.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        Log.d("tag", "debug. =================================");
        Log.d("tag", "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free)");
        Log.d("tag", "debug.memory: allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_diary_page, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setBackgroundColor(Color.TRANSPARENT);
        swipeLayout.setRefreshing(false);
        swipeLayout.setOnRefreshListener(this);

        ImageButton newNote = (ImageButton) view.findViewById(R.id.new_note);
        newNote.bringToFront();
        newNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDiaryEntryActivity();
            }
        });

        de_list_view = (ListView) view.findViewById(R.id.de_listview);

        de_list_view.setClickable(false);

        de_list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (de_list_view.getChildCount() == 0)
                    swipeLayout.setEnabled(true);
                else if (firstVisibleItem == 0 && visibleItemCount > 0 && de_list_view.getChildAt(0).getTop() >= 0)
                    swipeLayout.setEnabled(true);
                else
                    swipeLayout.setEnabled(false);
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
        swipeLayout.setRefreshing(false);
        de_list_view.setAdapter(null);
        DiaryEntryFetchTask deft = new DiaryEntryFetchTask();
        deft.execute();
    }

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
                //ib.setVisibility(View.VISIBLE);
            }

            if (list.size() > 0) {
                tw.setVisibility(View.INVISIBLE);
                //ib.setVisibility(View.INVISIBLE);
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
                            byte[] bitmapByte = EntityUtils.toByteArray(response2.getEntity());
                            Bitmap bm = loadFast(new ByteArrayInputStream(bitmapByte), new ByteArrayInputStream(bitmapByte));
                            response2 = null;
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
                                byte[] bitmapByte = EntityUtils.toByteArray(response2.getEntity());
                                Bitmap bm = loadFast(new ByteArrayInputStream(bitmapByte), new ByteArrayInputStream(bitmapByte));
                                response2 = null;
                                if (bm != null) {
                                    System.out.println("Image Byte Count: " + bm.getByteCount());
                                    dew.setImage(bm);
                                }
                                list.add(dew);
                                logHeap();
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


        private Bitmap loadFast(ByteArrayInputStream byteArrayInputStream, ByteArrayInputStream arrayInputStream) {
            int DESIRED_MAX_SIZE = 500;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(byteArrayInputStream, null, options);

            int h = options.outHeight;
            int w = options.outWidth;

            byteArrayInputStream = null;

            // Find best sample size
            int sampling = 1;

            if (h > DESIRED_MAX_SIZE || w > DESIRED_MAX_SIZE) {

                final int halfHeight = h / 2;
                final int halfWidth = w / 2;

                while ((halfHeight / sampling) > DESIRED_MAX_SIZE
                        && (halfWidth / sampling) > DESIRED_MAX_SIZE) {
                    sampling *= 2;
                }
            }

            options.inSampleSize = sampling;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;

            return BitmapFactory.decodeStream(arrayInputStream, null, options);
        }

    }

}