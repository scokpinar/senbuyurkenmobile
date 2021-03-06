package com.sam.senbuyurkenmobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.squareup.picasso.Picasso;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hugo.weaving.DebugLog;


public class DiaryEntryActivity extends Activity {

    private static final int MAX_WIDTH = 960;
    private static final int MAX_HEIGHT = 540;
    private static final int MAX_CHAR = 1024;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ImageView icon_camera;
    ImageView viewImage;
    OutputStream outFile = null;
    Bitmap bitmap;
    Uri selectedImage;
    private TextView char_count;
    private EditText note_area;

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);
        verifyStoragePermissions(this);

        icon_camera = (ImageView) findViewById(R.id.icon_camera);
        viewImage = (ImageView) findViewById(R.id.imageView);
        note_area = (EditText) findViewById(R.id.diary_entry);

        icon_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        char_count = (TextView) findViewById(R.id.char_count);


        final TextWatcher txtWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                char_count.setText(String.valueOf(MAX_CHAR - s.length()));
            }

            public void afterTextChanged(Editable s) {
            }
        };

        note_area.addTextChangedListener(txtWatcher);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, false);
                    viewImage.setImageBitmap(bitmap);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();

                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                selectedImage = data.getData();
                Picasso.with(this.getApplicationContext())
                        .load(selectedImage)
                        .resize(MAX_WIDTH, MAX_HEIGHT)
                        .onlyScaleDown()
                        .centerInside()
                        .into(viewImage);
            }
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Galeri", "İptal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(DiaryEntryActivity.this);
        builder.setTitle("Fotoğraf Ekle");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //if (options[item].equals("Take Photo")) {
                //    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                //    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                //    startActivityForResult(intent, 1);
                //} else if (options[item].equals("Choose from Gallery")) {
                if (options[item].equals("Galeri")) {


                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("İptal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void saveDiaryEntry(View view) {
        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        String entryText = ((EditText) findViewById(R.id.diary_entry)).getText().toString();
        String entryTitle = ((EditText) findViewById(R.id.entry_title)).getText().toString();

        List<NameValuePair> params = new ArrayList<>();


        params.add(new BasicNameValuePair("validUser", sp.getBoolean("validUser", false) + ""));
        params.add(new BasicNameValuePair("entryTitle", entryTitle));
        params.add(new BasicNameValuePair("entryText", entryText));
        params.add(new BasicNameValuePair("entryDate", Long.toString(Calendar.getInstance().getTimeInMillis())));
        params.add(new BasicNameValuePair("timeZone", Calendar.getInstance().getTimeZone().getID()));
        if (selectedImage != null)
            params.add(new BasicNameValuePair("photoURL", String.valueOf(System.currentTimeMillis()) + ".jpg"));

        if (AppUtility.hasActiveNetwork(this) && AppUtility.hasInternetConnection()) {
            new SaveTask().execute(params);
        } else {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.show();
        }
    }

    @DebugLog
    public void invokeWS(List<NameValuePair> params) {
        Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/createDiaryEntry").buildUpon();

        HttpPost httpPost = new HttpPost(builder.toString());
        HttpClient client = new DefaultHttpClient();

        try {

            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);

            String responseStr = EntityUtils.toString(response.getEntity());

            if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                JSONObject obj = new JSONObject(responseStr);

                if (obj.getBoolean("result") && selectedImage != null) {
                    Bitmap bitmap = loadFast(AppUtility.getPathFromUri(getContentResolver(), selectedImage));
                    File resizedFile = new File(android.os.Environment.getExternalStorageDirectory(), params.get(5).getValue());
                    OutputStream fOut;
                    try {
                        fOut = new BufferedOutputStream(new FileOutputStream(resizedFile));
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        bitmap.recycle();

                    } catch (Exception e) {
                        System.out.println("e = " + e);
                    }
                    saveToAWSS3(resizedFile, params.get(5).getValue());
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadFast(String byteArrayInputStream) {
        int DESIRED_MAX_SIZE = 1080;

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        //options.inJustDecodeBounds = false;

        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        //Bitmap decodeFile = BitmapFactory.decodeFile(byteArrayInputStream, options);
        Bitmap decodeFile = null;
        try {
            decodeFile = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //String fname=new File(getFilesDir(), "test.png").getAbsolutePath();

        //Bitmap bMap = BitmapFactory.decodeFile(fname);

        int h = decodeFile.getHeight();
        int w = decodeFile.getWidth();

        int sampling = 1;

        if (h > DESIRED_MAX_SIZE || w > DESIRED_MAX_SIZE) {

            final int halfHeight = h / 2;
            final int halfWidth = w / 2;

            while ((halfHeight / sampling) > DESIRED_MAX_SIZE
                    && (halfWidth / sampling) > DESIRED_MAX_SIZE) {
                sampling *= 2;
            }
        }

        ExifInterface exifReader;
        Matrix matrix = new Matrix();
        matrix.setScale((float) 1 / sampling, (float) 1 / sampling);

        try {
            exifReader = new ExifInterface(byteArrayInputStream);
            int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            System.out.println("orientation = " + orientation);

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int scaledWidth = w / sampling;
        int scaledHeight = h / sampling;

        Bitmap imageScaled = Bitmap.createScaledBitmap(decodeFile, scaledWidth, scaledHeight, true);
        Bitmap lastBitmap = Bitmap.createBitmap(imageScaled, 0, 0, imageScaled.getWidth(), imageScaled.getHeight(), matrix, false);
        return lastBitmap;

    }

    @DebugLog
    private void saveToAWSS3(File image, String fileName) {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        String subFolderOriginal = sp.getString("userId", "") + "/";

        AmazonS3 s3Client = getAWS3Client(sp.getString("userName", null), (sp.getBoolean("validUser", false) + ""));

        try {
            System.out.println("Uploading a new object to S3 from a file\n");

            PutObjectRequest por4Original = new PutObjectRequest(
                    AppUtility.existingBucketName, subFolderOriginal + fileName, image);

            por4Original.setCannedAcl(CannedAccessControlList.AuthenticatedRead);
            s3Client.putObject(por4Original);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public AmazonS3 getAWS3Client(String userName, String validUser) {
        AWSTempToken awsTempToken = AppUtility.getAWSTempToken(getApplicationContext(), userName, validUser);
        BasicSessionCredentials basicSessionCredentials =
                new BasicSessionCredentials(awsTempToken.getAccessKeyId(),
                        awsTempToken.getSecretAccessKey(),
                        awsTempToken.getSessionToken());
        return new AmazonS3Client(basicSessionCredentials);
    }

    class SaveTask extends AsyncTask<List<NameValuePair>, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DiaryEntryActivity.this, "", "", true);
            progressDialog.setContentView(R.layout.progress_layout);
        }

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<NameValuePair>... params) {
            List<NameValuePair> param = params[0];
            SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String userName = sp.getString("userName", null);
            param.add(new BasicNameValuePair("userName", userName));
            param.add((new BasicNameValuePair("token", AppUtility.getGoogleTempToken(getApplication().getApplicationContext(), userName))));
            invokeWS(param);
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

}
