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
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(AppUtility.GOOGLE_APP_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
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
            System.out.println("SignIn Result = " + result.toString());
        } else {
            System.out.println("SignIn Result = " + result.toString());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        System.out.println("connectionResult = " + connectionResult);
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
            case R.id.sign_in_button:
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
            invokeGoogleTokenValidationWS(googleUId, googleTempToken);
            return null;
        }

        protected void onPostExecute(String result) {
            navigateToMainActivity();
        }

        public void invokeGoogleTokenValidationWS(String googleUId, String googleTempToken) {

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("userName", googleUId)
                    .add("token", googleTempToken)
                    .build();
            Request request = new Request.Builder()
                    .url(AppUtility.APP_URL + "rest/appUtilityRest/googleTokenValidation")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                String responseStr = response.body().string();

                if (!responseStr.equals("null") && !responseStr.equals("")) {
                    editor = sp.edit();
                    JSONObject result = new JSONObject(responseStr);
                    if (result.getBoolean("result")) {
                        editor.putBoolean("validUser", true);
                        editor.putBoolean("userLoggedIn", true);
                        editor.apply();

                        RequestParams params = new RequestParams();
                        params.put("userName", sp.getString("userName", ""));
                        params.put("userType", UserType.FREE.getTypeCode());
                        params.put("active", "1");
                        invokeCreateUserWS(sp.getString("userName", ""), UserType.FREE.getTypeCode(), "1");

                    } else {
                        editor.putBoolean("validUser", false);
                        editor.putBoolean("userLoggedIn", false);
                        editor.apply();
                    }

                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }

        public void invokeCreateUserWS(String userName, String userType, String active) {

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("userName", userName)
                    .add("userType", userType)
                    .add("active", active)
                    .build();
            Request request = new Request.Builder()
                    .url(AppUtility.APP_URL + "rest/userRegistrationRest/createUser")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseStr = response.body().string();

                if (!responseStr.equals("null") && !responseStr.equals("")) {
                    editor = sp.edit();
                    JSONObject result = new JSONObject(responseStr);
                    userCreateResult = result.getBoolean("result");
                    System.out.println("userCreateResult = " + userCreateResult);
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        public Boolean getUserCreateResult() {
            return userCreateResult;
        }

        public void setUserCreateResult(Boolean userCreateResult) {
            this.userCreateResult = userCreateResult;
        }

    }
}
