package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class DiaryPageActivity extends Fragment implements MyListAdapterListener {

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

        de_list_view = (ListView) view.findViewById(R.id.de_listview);
        de_list_view.setClickable(false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            DiaryEntryFetchTask deft = new DiaryEntryFetchTask();
            deft.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_post:
                navigateToDiaryEntryActivity();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new_entry, menu);
        MenuItem item = menu.findItem(R.id.action_new_post);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void navigateToDiaryEntryActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(), DiaryEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void deleteProcess(final Integer entryId, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                MyListViewAdapter adapter = (MyListViewAdapter) de_list_view.getAdapter();
                List<DiaryEntryWrapper> data = adapter.getData();
                data.remove(position);
                adapter.setData(data);
                adapter.notifyDataSetChanged();

                SharedPreferences sp = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                final String userName = sp.getString("userName", null);
                params.add(new BasicNameValuePair("entryId", Integer.toString(entryId)));
                //todo: Token SharedPreferences den alınacak.
                //params.add(new BasicNameValuePair("token", AppUtility.getGoogleTempToken(getActivity(), userName)));
                params.add(new BasicNameValuePair("validUser", sp.getBoolean("validUser", false) + ""));
                invokeDeleteWS(params);
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void invokeDeleteWS(List<NameValuePair> params) {

        Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/deleteDiaryEntry").buildUpon();

        HttpPost httpPost = new HttpPost(builder.toString());
        HttpClient client = new DefaultHttpClient();

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);

            String responseStr = EntityUtils.toString(response.getEntity());

            if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                JSONObject obj = new JSONObject(responseStr);

                if (obj.getBoolean("result")) {
                    //Toast.makeText(getActivity().getApplicationContext(), "Kayıt silindi: " + params.get(0).getValue(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AmazonS3 getAWS3Client(String userName, String validUser) {
        AWSTempToken awsTempToken = AppUtility.getAWSTempToken(getActivity().getApplicationContext(), userName, validUser);
        BasicSessionCredentials basicSessionCredentials =
                new BasicSessionCredentials(awsTempToken.getAccessKeyId(),
                        awsTempToken.getSecretAccessKey(),
                        awsTempToken.getSessionToken());
        return new AmazonS3Client(basicSessionCredentials);
    }

    class DiaryEntryFetchTask extends AsyncTask<String, Void, List<DiaryEntryWrapper>> {

        private List<DiaryEntryWrapper> list = new ArrayList<>();
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
            de_list_view.setAdapter(adapter);
            if (progressDialog != null)
                progressDialog.dismiss();
        }

        protected List<DiaryEntryWrapper> doInBackground(String... urls) {
            Activity a = getActivity();
            sp = a.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            String userName = sp.getString("userName", null);
            params.add(new BasicNameValuePair("userName", userName));
            params.add(new BasicNameValuePair("token", AppUtility.getGoogleTempToken(getActivity(), userName)));
            params.add(new BasicNameValuePair("validUser", sp.getBoolean("validUser", false) + ""));
            invokeListDiaryEntryWS(params);
            return list;
        }

        @DebugLog
        public void invokeListDiaryEntryWS(List<NameValuePair> params) {
            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/listDiaryEntry/").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String subFolder = sp.getString("userId", "") + "/";

            System.out.println("Play Store Check --> subFolder = " + subFolder);

            AmazonS3 s3Client = getAWS3Client(params.get(0).getValue(), params.get(2).getValue());

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {

                    JSONArray values = new JSONArray(responseStr);
                    MyListViewAdapter adapter = null;


                    for (int i = 0; i < values.length(); i++) {
                        JSONObject o = (JSONObject) values.get(i);


                        if (de_list_view != null)
                            adapter = (MyListViewAdapter) de_list_view.getAdapter();

                        DiaryEntryWrapper dew = new DiaryEntryWrapper();
                        dew.setId(o.getInt("diary_entry_id"));

                        if (adapter == null) {
                            dew.setEntry_title(o.getString("entry_title"));
                            dew.setEntry_content(o.getString("entry_content"));
                            dew.setEntry_date(o.getString("entry_date"));
                            if (o.getString("photo_url") != null && !o.getString("photo_url").equals("null")) {
                                InputStream inputStream = loadFromAWSS3(o.getString("photo_url"), subFolder, s3Client);
                                //todo: Image not found gibi bir mesaj koymamız gerekiyor.
                                if (inputStream != null) {
                                    dew.setImage(BitmapFactory.decodeStream(inputStream));
                                    dew.setHasImage(true);
                                }
                            } else {
                                dew.setImage(null);
                                dew.setHasImage(false);
                            }

                            list.add(dew);
                        }

                        if (adapter != null) {
                            list = adapter.getData();
                            if (!list.contains(dew)) {

                                dew.setEntry_title(o.getString("entry_title"));
                                dew.setEntry_content(o.getString("entry_content"));
                                dew.setEntry_date(o.getString("entry_date"));
                                if (o.getString("photo_url") != null && !o.getString("photo_url").equals("null")) {
                                    InputStream inputStream = loadFromAWSS3(o.getString("photo_url"), subFolder, s3Client);
                                    //todo: Image not found gibi bir mesaj koymamız gerekiyor.
                                    if (inputStream != null) {
                                        dew.setImage(BitmapFactory.decodeStream(inputStream));
                                        dew.setHasImage(true);
                                    }
                                } else {
                                    dew.setImage(null);
                                    dew.setHasImage(false);
                                }
                                list.add(dew);
                            }
                        }
                    }


                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @DebugLog
        private InputStream loadFromAWSS3(String photoURL, String subFolder, AmazonS3 s3Client) {
            try {
                System.out.println("Getting objects from S3 as stream\n");

                S3Object object = s3Client.getObject(new GetObjectRequest(
                        AppUtility.existingBucketName4Thumbnail, subFolder + "medium/" + photoURL));
                S3ObjectInputStream objectContent = object.getObjectContent();
                return objectContent;

            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode().equals("NoSuchKey")) {
                    S3Object object = s3Client.getObject(new GetObjectRequest(
                            AppUtility.existingBucketName, subFolder + photoURL));
                    S3ObjectInputStream objectContent = object.getObjectContent();
                    return objectContent;
                }
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

    }


}