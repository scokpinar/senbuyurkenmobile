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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

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
public class BabyInfoActivity extends Fragment {

    private BabyInfoWrapper babyInfo = new BabyInfoWrapper();
    private View view;
    private String gender;
    private ImageButton maleButton;
    private ImageButton femaleButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_baby_info, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Button button = (Button) view.findViewById(R.id.babyInfoSaveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBabyInfo();
            }
        });

        maleButton = (ImageButton) view.findViewById(R.id.gender_male);
        femaleButton = (ImageButton) view.findViewById(R.id.gender_female);

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "E";
                maleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_boy_selected));
                femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_girl_not_selected));
            }
        });


        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "K";
                femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_girl_selected));
                maleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_boy_not_selected));
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
            BabyInfoFetchTask bift = new BabyInfoFetchTask();
            bift.execute();

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
    }

    /**
     * Method gets triggered when save button is clicked
     */
    public void saveBabyInfo() {
        Activity activity = getActivity();

        SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String email = sp.getString("username", null);

        String name = ((EditText) activity.findViewById(R.id.name)).getText().toString();
        String surname = ((EditText) activity.findViewById(R.id.surname)).getText().toString();
        String birthDate = ((EditText) activity.findViewById(R.id.birthDate)).getText().toString();
        String birthHour = ((EditText) activity.findViewById(R.id.birthHour)).getText().toString();
        String birthWeight = ((EditText) activity.findViewById(R.id.birthWeight)).getText().toString();
        String birthLength = ((EditText) activity.findViewById(R.id.birthLength)).getText().toString();
        String birthPlace = ((EditText) activity.findViewById(R.id.birthPlace)).getText().toString();
        String hospital = ((EditText) activity.findViewById(R.id.hospital)).getText().toString();
        String gynecologyDoctor = ((EditText) activity.findViewById(R.id.gynecologyDoctor)).getText().toString();
        String pediatricianDoctor = ((EditText) activity.findViewById(R.id.pediatricianDoctor)).getText().toString();

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (AppUtility.isNotNull(name) && AppUtility.isNotNull(surname) && AppUtility.isNotNull(gender)
                && AppUtility.isNotNull(birthDate)) {

            // Put http parameters
            params.put("email", email);
            params.put("name", name);
            params.put("surname", surname);
            params.put("gender", gender);
            params.put("birth_date", birthDate);
            params.put("birth_hour", birthHour);
            params.put("birth_weight", birthWeight);
            params.put("birth_length", birthLength);
            params.put("birth_place", birthPlace);
            params.put("hospital", hospital);
            params.put("gynecology_doctor", gynecologyDoctor);
            params.put("pediatrician_doctor", pediatricianDoctor);

            // Invoke RESTFull Web Service with Http parameters
            invokeWS(activity, params);
        }
        // When any of the Edit View control left blank
        else {
            Toast.makeText(activity.getApplicationContext().getApplicationContext(), "Please fill the fields", Toast.LENGTH_LONG).show();
        }
    }

    public void setBabyInfoData() {
        TextView name = ((EditText) view.findViewById(R.id.name));
        name.setText(babyInfo.getName());
        TextView surname = ((EditText) view.findViewById(R.id.surname));
        surname.setText(babyInfo.getSurname());
        if (babyInfo.getGender().equals("E"))
            maleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_boy_selected));
        if (babyInfo.getGender().equals("K"))
            femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_girl_selected));
        TextView birthDate = ((EditText) view.findViewById(R.id.birthDate));
        birthDate.setText(babyInfo.getBirthDate());
        TextView birthHour = ((EditText) view.findViewById(R.id.birthHour));
        birthHour.setText(babyInfo.getBirthHour());
        TextView birthWeight = ((EditText) view.findViewById(R.id.birthWeight));
        birthWeight.setText(babyInfo.getBirthWeight().toString());
        TextView birthLength = ((EditText) view.findViewById(R.id.birthLength));
        birthLength.setText(babyInfo.getBirthLength().toString());
        TextView birthPlace = ((EditText) view.findViewById(R.id.birthPlace));
        birthPlace.setText(babyInfo.getBirthPlace());
        TextView hospital = ((EditText) view.findViewById(R.id.hospital));
        hospital.setText(babyInfo.getHospital());
        TextView gynecologyDoctor = ((EditText) view.findViewById(R.id.gynecologyDoctor));
        gynecologyDoctor.setText(babyInfo.getGynecologyDoctor());
        TextView pediatricianDoctor = ((EditText) view.findViewById(R.id.pediatricianDoctor));
        pediatricianDoctor.setText(babyInfo.getPediatricianDoctor());
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param view
     * @param params
     */

    public void invokeWS(final Activity view, RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(AppUtility.APP_URL + "rest/babyRegistrationRest/createBabyInfo", params, new AsyncHttpResponseHandler() {

            @Override
            public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    JSONObject obj = new JSONObject(new String(response));
                    if (obj.getBoolean("result")) {
                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_existinguser_msg), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (statusCode == 404) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class BabyInfoFetchTask extends AsyncTask<String, Void, BabyInfoWrapper> {

        private ProgressDialog progressDialog;

        public BabyInfoFetchTask() {
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            progressDialog.setContentView(R.layout.progress_layout);
        }

        @Override
        protected void onPostExecute(BabyInfoWrapper babyInfo) {
            progressDialog.dismiss();
            setBabyInfoData();

        }

        protected BabyInfoWrapper doInBackground(String... urls) {
            Activity activity = getActivity();
            SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", sp.getString("username", null)));
            invokeRestWS(params);
            return babyInfo;
        }

        public void invokeRestWS(List<NameValuePair> params) {
            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/babyRegistrationRest/getBabyInfo").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                    JSONObject obj = new JSONObject(responseStr);

                    if (obj.getString("babyInfoId") != null) {

                        babyInfo.setName(obj.getString("name"));
                        babyInfo.setSurname(obj.getString("surname"));
                        babyInfo.setGender(obj.getString("gender"));
                        babyInfo.setBirthDate(obj.getString("birthDate"));
                        babyInfo.setBirthHour(obj.getString("birthHour"));
                        babyInfo.setBirthWeight(obj.getInt("birthWeight"));
                        babyInfo.setBirthLength(obj.getInt("birthLength"));
                        babyInfo.setBirthPlace(obj.getString("birthPlace"));
                        babyInfo.setHospital(obj.getString("hospital"));
                        babyInfo.setGynecologyDoctor(obj.getString("gynecologyDoctor"));
                        babyInfo.setPediatricianDoctor(obj.getString("pediatricianDoctor"));

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
