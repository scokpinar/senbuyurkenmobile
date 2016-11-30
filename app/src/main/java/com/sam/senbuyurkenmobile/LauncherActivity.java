package com.sam.senbuyurkenmobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
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

import io.fabric.sdk.android.Fabric;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LauncherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "LauncherActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launcher);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(AppUtility.GOOGLE_APP_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setVisibility(View.INVISIBLE);

        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                TextView mTextView = (TextView) v;
                mTextView.setText(getString(R.string.google_login));
                return;
            }
        }

        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(500);

                    Boolean userLoggedIn = sp.getBoolean("userLoggedIn", false);
                    String userName = sp.getString("userName", "");
                    Boolean validUser = sp.getBoolean("validUser", false);

                    if (userLoggedIn) {
                        signInButton.setVisibility(View.INVISIBLE);
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        AppUtility.createAWSTempToken(getApplicationContext(), userName, validUser + "");
                        startActivity(i);
                    }
                    signInButton.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    finish();
                }
            }
        };

        background.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
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
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
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

            GoogleTokenValidationTask task = new GoogleTokenValidationTask(this);
            task.execute(email);

            Uri profilePhotoUrl = acct.getPhotoUrl();
            editor = sp.edit();
            editor.putString("profilePhotoUrl", profilePhotoUrl.toString());
            editor.apply();

            System.out.println("SignIn Result = " + result.toString());
        } else {
            System.out.println("SignIn Result = " + result.toString());
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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
            case R.id.icon_signout:
                System.out.println("sign out");
                break;
            case R.id.text_signout:
                System.out.println("sign out");
                break;
        }

    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class GoogleTokenValidationTask extends AsyncTask<String, Void, Boolean> {
        Boolean userCreateResult = false;
        String googleUId;
        String googleTempToken;
        Context context;

        GoogleTokenValidationTask(Context context) {
            this.context = context;
            sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        }

        protected Boolean doInBackground(String... strings) {
            String account = strings[0];

            if (AppUtility.hasActiveNetwork(context) && AppUtility.hasInternetConnection()) {
                editor = sp.edit();
                googleUId = AppUtility.getGoogleUId(LauncherActivity.this, account);
                googleTempToken = AppUtility.getGoogleTempToken(LauncherActivity.this, account);

                editor.putString("userId", googleUId);
                editor.putString("userName", account);
                editor.apply();

                RequestParams params = new RequestParams();
                params.put("userName", googleUId);
                params.put("token", googleTempToken);
                invokeGoogleTokenValidationWS(googleUId, googleTempToken);
                return true;
            } else {
                return false;
            }

        }

        protected void onPostExecute(Boolean result) {
            if (result)
                navigateToMainActivity();
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.no_internet);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LauncherActivity.this.recreate();
                    }
                });
                builder.show();
            }
        }

        void invokeGoogleTokenValidationWS(String googleUId, String googleTempToken) {

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

        void invokeCreateUserWS(String userName, String userType, String active) {

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
                    JSONObject result = new JSONObject(responseStr);
                    userCreateResult = result.getBoolean("result");
                    System.out.println("userCreateResult = " + userCreateResult);
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

    }
}