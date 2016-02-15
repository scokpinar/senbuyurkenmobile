package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SametCokpinar on 08/03/15.
 */
public class ParentInfoActivity extends Fragment {

    private View view;
    private ParentInfoWrapper piw = new ParentInfoWrapper();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_parent_info, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Button button = (Button) view.findViewById(R.id.parentInfoSaveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveParentInfo();
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
            ParentInfoFetchTask pift = new ParentInfoFetchTask();
            pift.execute();


        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void setParentInfoData() {

        TextView mother_name = ((EditText) view.findViewById(R.id.mother_name));
        mother_name.setText(piw.getMother_name());
        TextView mother_surname = ((EditText) view.findViewById(R.id.mother_surname));
        mother_surname.setText(piw.getMother_surname());

        TextView father_name = ((EditText) view.findViewById(R.id.father_name));
        father_name.setText(piw.getFather_name());
        TextView father_surname = ((EditText) view.findViewById(R.id.father_surname));
        father_surname.setText(piw.getFather_surname());

        TextView wedding_anniversary = ((EditText) view.findViewById(R.id.wedding_anniversary));
        wedding_anniversary.setText(piw.getWedding_anniversary());

    }

    public void saveParentInfo() {
        Activity activity = getActivity();

        SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String email = sp.getString("userName", null);

        String mother_name = ((EditText) activity.findViewById(R.id.mother_name)).getText().toString();
        String mother_surname = ((EditText) activity.findViewById(R.id.mother_surname)).getText().toString();
        String father_name = ((EditText) activity.findViewById(R.id.father_name)).getText().toString();
        String father_surname = ((EditText) activity.findViewById(R.id.father_surname)).getText().toString();
        String wedding_anniversary = ((EditText) activity.findViewById(R.id.wedding_anniversary)).getText().toString();

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (AppUtility.isNotNull(mother_name) && AppUtility.isNotNull(mother_surname)) {

            // Put http parameters
            params.put("email", email);
            params.put("mother_name", mother_name);
            params.put("mother_surname", mother_surname);
            params.put("father_name", father_name);
            params.put("father_surname", father_surname);
            params.put("wedding_anniversary", wedding_anniversary);

            // Invoke RESTFull Web Service with Http parameters
            invokeSaveParentInfoWS(params);

        }
        // When any of the Edit View control left blank
        else {
            Toast.makeText(activity.getApplicationContext().getApplicationContext(), "Please fill the fields", Toast.LENGTH_LONG).show();
        }

    }

    public void invokeSaveParentInfoWS(RequestParams params) {
        // Show Progress Dialog
        //prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(AppUtility.APP_URL + "rest/parentRegistrationRest/createParentInfo", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // Hide Progress Dialog
                //prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(new String(response));
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("result")) {
                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();

                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_existinguser_msg), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getView().getContext().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // Hide Progress Dialog
                //prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class ParentInfoFetchTask extends AsyncTask<String, Void, ParentInfoWrapper> {

        private ProgressDialog progressDialog;

        public ParentInfoFetchTask() {
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            progressDialog.setContentView(R.layout.progress_layout);
        }

        @Override
        protected void onPostExecute(ParentInfoWrapper piw) {
            progressDialog.dismiss();
            setParentInfoData();

        }

        protected ParentInfoWrapper doInBackground(String... urls) {
            Activity activity = getActivity();
            SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", sp.getString("userName", null)));
            invokeRestWS(params);
            return piw;
        }

        public void invokeRestWS(List<NameValuePair> params) {
            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/parentRegistrationRest/getParentInfo").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                    JSONObject obj = new JSONObject(responseStr);

                    if (obj.getString("parentInfoId") != null) {
                        piw.setMother_name(obj.getString("motherName"));
                        piw.setMother_surname(obj.getString("motherSurname"));
                        piw.setFather_name(obj.getString("fatherName"));
                        piw.setFather_surname(obj.getString("fatherSurname"));
                        piw.setWedding_anniversary(obj.getString("weddingAnniversary"));
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
    }

}
