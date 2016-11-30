package com.sam.senbuyurkenmobile;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hugo.weaving.DebugLog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by SametCokpinar on 17/12/14.
 */
public class AppUtility {

    public static final String APP_URL = "http://ws.senbuyurken.com/";
    public static final String existingBucketName = "c79d97161ef8f66e341b304673c24ce7";
    public static final String existingBucketName4Thumbnail = "c79d97161ef8f66e341b304673c24ce7-thumbnail";
    public static final String GOOGLE_APP_ID = "345121036471-p2rragjceuga9g0vrf04e8ml7komc07m.apps.googleusercontent.com";

    //Email Pattern
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern pattern;
    private static Matcher matcher;


    private static AWSTempToken awsTempToken = new AWSTempToken();


    /**
     * Validate Email with regular expression
     *
     * @param email
     * @return true for Valid Email and false for Invalid Email
     */
    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }

    /**
     * Checks for Null String object
     *
     * @param txt
     * @return true for not null and false for null String object
     */
    public static boolean isNotNull(String txt) {
        return txt != null && txt.trim().length() > 0;
    }

    public static boolean isNull(String txt) {
        return txt == null;
    }

    public static boolean comparePasswords(String p1, String p2) {
        return p1.equals(p2);

    }

    public static String passwordMD5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "errorInEncrypt";
    }

    public static String getPathFromUri(ContentResolver contentResolver, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @DebugLog
    public static AWSTempToken getAWSTempToken(Context context, String userName, String validUser) {
        if (awsTempToken.getAccessKeyId() == null || awsTempToken.getExpiration() < System.currentTimeMillis())
            createAWSTempToken(context, userName, validUser);
        return awsTempToken;
    }

    @DebugLog
    public static String getGoogleUId(Context context, String account) {
        String uid = "";
        try {
            uid = GoogleAuthUtil.getAccountId(context, account);
        } catch (GoogleAuthException | IOException e) {
            e.printStackTrace();
        }
        return uid;
    }


    @DebugLog
    public static String getGoogleTempToken(Context context, String account) {
        String scope = "audience:server:client_id:" + GOOGLE_APP_ID;
        String token = "";
        try {
            token = GoogleAuthUtil.getToken(context, new Account(account, "com.google"), scope);
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }
        return token;
    }

    static void createAWSTempToken(Context context, String userName, String validUser) {

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userName", userName)
                .add("validUser", validUser)
                .add("token", getGoogleTempToken(context, userName))
                .build();

        Request request = new Request.Builder()
                .url(AppUtility.APP_URL + "rest/appUtilityRest/getToken")
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();

            if (!responseStr.equals("null") && !responseStr.equals("")) {
                JSONObject obj = new JSONObject(responseStr);
                JSONObject credentials = obj.getJSONObject("credentials");
                awsTempToken.setAccessKeyId(credentials.getString("accessKeyId"));
                awsTempToken.setSecretAccessKey(credentials.getString("secretAccessKey"));
                awsTempToken.setSessionToken(credentials.getString("sessionToken"));
                awsTempToken.setExpiration(credentials.getLong("expiration"));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    static boolean hasActiveNetwork(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    static boolean hasInternetConnection() {
        try {
            URL url = new URL("http://ws.senbuyurken.com");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}


