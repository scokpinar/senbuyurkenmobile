package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.view.inputmethod.InputMethodManager;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
        String email = sp.getString("userName", null);

        TextView baby_name = (TextView) activity.findViewById(R.id.baby_name);
        baby_name.setTextColor(Color.BLACK);

        TextView baby_surname = (TextView) activity.findViewById(R.id.baby_surname);
        baby_surname.setTextColor(Color.BLACK);

        TextView birth_Date = (TextView) activity.findViewById(R.id.birthDate);
        birth_Date.setTextColor(Color.BLACK);

        TextView birth_Hour = (TextView) activity.findViewById(R.id.birthHour);
        birth_Hour.setTextColor(Color.BLACK);


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

        RequestParams params = new RequestParams();
        if (AppUtility.isNotNull(name) && AppUtility.isNotNull(surname) && AppUtility.isNotNull(gender)
                && AppUtility.isNotNull(birthDate) && AppUtility.isNotNull(birthHour)) {
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

            invokeWS(params);
        } else {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            if (name.equals("")) {
                baby_name.setTextColor(Color.RED);

            }
            if (surname.equals("")) {
                baby_surname.setTextColor(Color.RED);

            }
            if (birthDate.equals("")) {
                birth_Date.setTextColor(Color.RED);

            }
            if (birthHour.equals("")) {
                birth_Hour.setTextColor(Color.RED);

            }
            Toast.makeText(activity.getApplicationContext(), activity.getApplicationContext().getText(R.string.required_fields), Toast.LENGTH_LONG).show();
        }
    }

    public void setBabyInfoData() {
        TextView name = ((EditText) view.findViewById(R.id.name));
        name.setText(babyInfo.getName());
        TextView surname = ((EditText) view.findViewById(R.id.surname));
        surname.setText(babyInfo.getSurname());
        if (babyInfo.getGender() != null && babyInfo.getGender().equals("E")) {
            gender = "E";
            maleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_boy_selected));
        }
        if (babyInfo.getGender() != null && babyInfo.getGender().equals("K")) {
            gender = "K";
            femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.baby_girl_selected));
        }
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
     * @param params
     */

    public void invokeWS(RequestParams params) {
        final Context ac = getActivity().getApplicationContext();

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
                        Toast.makeText(ac, ac.getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(ac, ac.getString(R.string.register_fail_msg), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ac, ac.getString(R.string.register_fail_msg), Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (statusCode == 404) {
                    Toast.makeText(ac, ac.getString(R.string.register_fail_msg), Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(ac, ac.getString(R.string.register_fail_msg), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ac, ac.getString(R.string.register_fail_msg), Toast.LENGTH_LONG).show();
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
            params.add(new BasicNameValuePair("email", sp.getString("userName", null)));
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

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
