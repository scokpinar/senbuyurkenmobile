package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity {

    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //new HttpRequestTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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


    /**
     * Method gets triggered when save button is clicked
     *
     * @param view
     */
    public void registerUser(View view) {
        // get name value
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        // get surname
        String surname = ((EditText) findViewById(R.id.surname)).getText().toString();
        // get gender
        RadioGroup radioGenderGroup = (RadioGroup) findViewById(R.id.radioGender);
        RadioButton radioGenderButton = (RadioButton) findViewById(radioGenderGroup.getCheckedRadioButtonId());
        String gender = radioGenderButton.getText().toString();
        // get birthDate
        String birthDate = ((EditText) findViewById(R.id.birthDate)).getText().toString();
        // get email
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        // get password
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        // get password2
        String password2 = ((EditText) findViewById(R.id.password2)).getText().toString();


        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (AppUtility.isNotNull(name) && AppUtility.isNotNull(surname) && AppUtility.isNotNull(gender)
                && AppUtility.isNotNull(birthDate) && AppUtility.isNotNull(email) && AppUtility.isNotNull(password)) {
            // When Email entered is Valid
            if (AppUtility.validate(email)) {
                if (AppUtility.comparePasswords(password, password2)) {
                    // Put http parameters
                    params.put("name", name);
                    params.put("surname", surname);
                    params.put("gender", gender.substring(0, 1));
                    params.put("birth_date", birthDate);
                    params.put("email", email);
                    params.put("password", AppUtility.passwordMD5(password));
                    params.put("user_type", UserType.FREE.getTypeCode());
                    params.put("active", "1");

                    // Invoke RESTFull Web Service with Http parameters
                    invokeWS(params);


                    // When Passwords not match
                } else {
                    Toast.makeText(getApplicationContext(), "Passwords not match, please check", Toast.LENGTH_LONG).show();
                }
            }
            // When Email is invalid
            else {
                Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            }
        }
        // When any of the Edit View control left blank
        else {
            Toast.makeText(getApplicationContext(), "Please fill the fields", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("https://afternoon-citadel-9635.herokuapp.com/rest/userRegistrationRest/createUser", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(new String(response));
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("result")) {
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        navigateToDiaryEntryActivity();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.register_existinguser_msg), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /*
    private class HttpRequestTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = "https://afternoon-citadel-9635.herokuapp.com/rest/userRestWS";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String greeting = restTemplate.getForObject(url, String.class);
                return greeting;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String greeting) {
            //TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.name);
            //greetingIdText.setText(greeting.getId());
            greetingContentText.setText(greeting);
        }

    }
    */

    /**
     * Method gets triggered when RegisterActivity button is clicked
     */
    public void navigateToDiaryEntryActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), DiaryEntryActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
}
