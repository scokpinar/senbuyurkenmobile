package com.sam.senbuyurkenmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SametCokpinar on 25/01/15.
 */
public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_2);

        findViewById(R.id.sign_in_button_2).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(AppUtility.GOOGLE_APP_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button_2);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
    }

    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String email = acct.getEmail();
            GoogleTokenValidationTask task = new GoogleTokenValidationTask();
            task.execute(email);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_2:
                signIn();
                break;
        }
    }

    public void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class GoogleTokenValidationTask extends AsyncTask<String, Void, String> {

        Boolean userCreateResult = false;
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        String googleUId;
        String googleTempToken;

        public GoogleTokenValidationTask() {
            sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        }

        protected String doInBackground(String... strings) {
            String account = strings[0];

            editor = sp.edit();
            googleUId = AppUtility.getGoogleUId(LoginActivity.this, account);
            googleTempToken = AppUtility.getGoogleTempToken(LoginActivity.this, account);

            editor.putString("userId", googleUId);
            editor.putString("userName", account);
            editor.apply();

            RequestParams params = new RequestParams();
            params.put("userName", googleUId);
            params.put("token", googleTempToken);
            invokeGoogleTokenValidationWS(params);
            return null;
        }

        protected void onPostExecute(String result) {
            navigateToMainActivity();
        }

        public void invokeGoogleTokenValidationWS(RequestParams params) {
            SyncHttpClient client = new SyncHttpClient();
            client.post(AppUtility.APP_URL + "rest/appUtilityRest/googleTokenValidation/", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    editor = sp.edit();
                    try {
                        if (response.getBoolean("result")) {
                            editor.putBoolean("validUser", true);
                            editor.putBoolean("userLoggedIn", true);
                            editor.apply();

                            RequestParams params = new RequestParams();
                            params.put("userName", googleUId);
                            params.put("userType", UserType.FREE.getTypeCode());
                            params.put("active", "1");
                            invokeCreateUserWS(params);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        editor.putBoolean("validUser", false);
                        editor.putBoolean("userLoggedIn", false);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    if (statusCode == 404) {

                    } else if (statusCode == 500) {

                    } else {

                    }
                }
            });
        }

        public void invokeCreateUserWS(RequestParams params) {
            SyncHttpClient client = new SyncHttpClient();
            client.post(AppUtility.APP_URL + "rest/userRegistrationRest/createUser/", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        userCreateResult = response.getBoolean("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    if (statusCode == 404) {
                    } else if (statusCode == 500) {
                    } else {
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
