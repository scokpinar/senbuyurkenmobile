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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class DiaryPageActivity extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private ListView de_list_view;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeLayout;

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

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //Toast.makeText(getActivity(), "Back Pressed", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
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
            }

            if (list.size() > 0) {
                tw.setVisibility(View.INVISIBLE);
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

        @DebugLog
        public void invokeRestWS(List<NameValuePair> params) {
            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/listDiaryEntry/").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String subFolder = sp.getString("uid", "") + "/";

            AWSTempToken awsTempToken = AppUtility.getAWSTempToken(sp.getString("username", null), sp.getString("token", null));
            BasicSessionCredentials basicSessionCredentials =
                    new BasicSessionCredentials(awsTempToken.getAccessKeyId(),
                            awsTempToken.getSecretAccessKey(),
                            awsTempToken.getSessionToken());
            AmazonS3 s3Client = new AmazonS3Client(basicSessionCredentials);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {

                    JSONArray values = new JSONArray(responseStr);

                    for (int i = 0; i < values.length(); i++) {
                        JSONObject o = (JSONObject) values.get(i);

                        DiaryEntryWrapper dew = new DiaryEntryWrapper();
                        dew.setEntry_content(o.getString("entry_content"));
                        dew.setEntry_date(o.getString("entry_date"));
                        InputStream inputStream = loadFromAWSS3(o.getString("photo_url"), subFolder, s3Client);

                        byte[] data = IOUtils.toByteArray(inputStream);
                        ByteArrayInputStream bin = new ByteArrayInputStream(data);

                        Bitmap bm = loadFast(bin);
                        dew.setImage(bm);
                        list.add(dew);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @DebugLog
        private InputStream loadFromAWSS3(String photoURL, String subFolder, AmazonS3 s3Client) {
            try {
                System.out.println("Getting objects from S3 as stream\n");

                S3Object object = s3Client.getObject(new GetObjectRequest(
                        AppUtility.existingBucketName, subFolder + photoURL));

                return object.getObjectContent();

            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
            return null;
        }

        @DebugLog
        private Bitmap loadFast(ByteArrayInputStream byteArrayInputStream) {
            int DESIRED_MAX_SIZE = 1080;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(byteArrayInputStream, null, options);

            int h = options.outHeight;
            int w = options.outWidth;

            byteArrayInputStream.reset();

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

            return BitmapFactory.decodeStream(byteArrayInputStream, null, options);

        }

    }

}