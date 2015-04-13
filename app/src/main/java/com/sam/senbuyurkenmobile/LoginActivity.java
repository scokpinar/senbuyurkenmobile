package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by SametCokpinar on 25/01/15.
 */
public class LoginActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;


    /* Track whether the sign-in button has been clicked so that we know to resolve
 * all issues preventing sign-in without waiting.
 */
    private boolean mSignInClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_2);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        findViewById(R.id.sign_in_button_2).setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        Toast.makeText(this, "Login successfully!", Toast.LENGTH_LONG).show();

        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

        RetrieveIdTokenTask task = new RetrieveIdTokenTask();
        task.execute(email);

        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", email);
        editor.commit();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = connectionResult;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button_2
                && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }

    }

    public void navigateToDiaryPageActivity() {
        Intent intent = new Intent(getApplicationContext(), DiaryPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void navigateToBabyInfoActivity() {
        Intent intent = new Intent(getApplicationContext(), BabyInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private class RetrieveIdTokenTask extends AsyncTask<String, Void, String> {

        Boolean userCreateResult = false;
        SharedPreferences sp;

        protected String doInBackground(String... strings) {
            String account = strings[0];
            sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);


            RequestParams params = new RequestParams();
            params.put("email", account);
            params.put("user_type", UserType.FREE.getTypeCode());
            params.put("active", "1");
            invokeWS(params);

            String homeServerClient = "345121036471-p2rragjceuga9g0vrf04e8ml7komc07m.apps.googleusercontent.com";
            try {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("uid", GoogleAuthUtil.getAccountId(LoginActivity.this, account));
                editor.apply();
                String scope = "audience:server:client_id:" + homeServerClient;
                return GoogleAuthUtil.getToken(LoginActivity.this, account, scope);
            } catch (GooglePlayServicesAvailabilityException playEx) {
                // In this case you could prompt the user to upgrade.
            } catch (UserRecoverableAuthException userAuthEx) {
                // This should not occur for ID tokens.
            } catch (IOException transientEx) {
                // You could retry in this case.
            } catch (GoogleAuthException authEx) {
                // General auth error.
            }
            return null;
        }

        protected void onPostExecute(String result) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("token", result);
            editor.apply();

//            if (getUserCreateResult())
            //              navigateToBabyInfoActivity();
//            else
//                navigateToDiaryPageActivity();
            navigateToMainActivity();
        }


        public void invokeWS(RequestParams params) {
            // Make RESTful webservice call using SyncHttpClient object
            SyncHttpClient client = new SyncHttpClient();
            client.post(AppUtility.APP_URL + "rest/userRegistrationRest/createUser/", params, new JsonHttpResponseHandler() {


                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        // JSON Object
                        JSONObject obj = response;
                        // When the JSON response has status boolean value assigned with true
                        userCreateResult = obj.getBoolean("result");
//                            Toast.makeText(getApplicationContext(),getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();

                        // Else display error message
                        //else {
                        //errorMsg.setText(obj.getString("error_msg"));
//                            Toast.makeText(getApplicationContext(),getApplicationContext().getString(R.string.register_existinguser_msg), Toast.LENGTH_LONG).show();
                        //}
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
//                        Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();

                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    // When Http response code is '404'
                    if (statusCode == 404) {
                        //                      Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }
                    // When Http response code is '500'
                    else if (statusCode == 500) {
                        //                     Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }
                    // When Http response code other than 404, 500
                    else {
                        //                     Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        public Boolean getUserCreateResult() {
            return userCreateResult;
        }

        public void setUserCreateResult(Boolean userCreateResult) {
            this.userCreateResult = userCreateResult;
        }
    }
}
