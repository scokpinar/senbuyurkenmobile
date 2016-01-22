package com.sam.senbuyurkenmobile;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hugo.weaving.DebugLog;

/**
 * Created by SametCokpinar on 17/12/14.
 */
public class AppUtility {

    public static final String APP_URL = "http://ws.senbuyurken.com/";
    //public static final String APP_URL = "https://afternoon-citadel-9635.herokuapp.com/";
    //public static final String APP_URL = "http://176.40.111.86/senbuyurken/";

    public static final String existingBucketName = "c79d97161ef8f66e341b304673c24ce7";

    //Email Pattern
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern pattern;
    private static Matcher matcher;

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
        return txt != null && txt.trim().length() > 0 ? true : false;
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
    public static AWSTempToken getAWSTempToken(String userName, String token) {
        AWSTempToken tempToken = new AWSTempToken();

        Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/appUtilityRest/getToken").buildUpon();

        HttpPost httpPost = new HttpPost(builder.toString());
        HttpClient client = new DefaultHttpClient();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("un", userName));
        params.add(new BasicNameValuePair("t", token));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);

            String responseStr = EntityUtils.toString(response.getEntity());

            if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                JSONObject obj = new JSONObject(responseStr);
                JSONObject credentials = obj.getJSONObject("credentials");
                tempToken.setAccessKeyId(credentials.getString("accessKeyId"));
                tempToken.setSecretAccessKey(credentials.getString("secretAccessKey"));
                tempToken.setSessionToken(credentials.getString("sessionToken"));
                tempToken.setExpiration(credentials.getLong("expiration"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tempToken;
    }
}


