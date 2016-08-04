package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by SametCokpinar on 08/03/15.
 */
public class ParentInfoActivity extends Fragment {

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private View view;
    private ParentInfoWrapper piw = new ParentInfoWrapper();
    private ImageView mother_photo;
    private ImageView father_photo;
    private Uri mImageCaptureUri;
    private Uri fImageCaptureUri;
    private File mFile;
    private File fFile;
    private int selectedImageView = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_parent_info, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mother_photo = (ImageView) view.findViewById(R.id.mother_photo);
        father_photo = (ImageView) view.findViewById(R.id.father_photo);

        mother_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageView = 1;
                selectImage(selectedImageView);
            }
        });

        father_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageView = 2;
                selectImage(selectedImageView);
            }
        });

        Button button = (Button) view.findViewById(R.id.parentInfoSaveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveParentInfo();
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
            ParentInfoFetchTask pift = new ParentInfoFetchTask();
            pift.execute();


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

    private void selectImage(final int selectedImageView) {

        //final CharSequence[] options = {"Fotoğraf Çek", "Galeri", "İptal"};
        final CharSequence[] options = {"Galeri", "İptal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Fotoğraf Seç");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                /*if (options[item].equals("Fotoğraf Çek")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    if (selectedImageView == 1) {
                        mImageCaptureUri = Uri.fromFile(f);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    } else if (selectedImageView == 2) {
                        fImageCaptureUri = Uri.fromFile(f);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fImageCaptureUri);

                    }
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                } else*/
                if (options[item].equals("Galeri")) {

                    if (selectedImageView == 1) {
                        mImageCaptureUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        Intent intent = new Intent(Intent.ACTION_PICK, mImageCaptureUri);
                        startActivityForResult(intent, PICK_FROM_FILE);

                    } else if (selectedImageView == 2) {
                        fImageCaptureUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        Intent intent = new Intent(Intent.ACTION_PICK, fImageCaptureUri);
                        startActivityForResult(intent, PICK_FROM_FILE);


                    }

                } else if (options[item].equals("İptal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            /*case PICK_FROM_CAMERA:
                doCrop(selectedImageView);
                break;*/

            case PICK_FROM_FILE:
                if (selectedImageView == 1) {
                    mImageCaptureUri = data.getData();
                } else if (selectedImageView == 2) {
                    fImageCaptureUri = data.getData();

                }
                doCrop(selectedImageView);
                break;

            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
                String path = Environment.getExternalStorageDirectory()
                        + File.separator;

                OutputStream outFile;

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");

                    try {
                        outFile = new FileOutputStream(file);
                        if (photo != null) {
                            photo.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                        }
                        outFile.flush();
                        outFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (selectedImageView == 1) {
                        mother_photo.setImageBitmap(photo);
                        mFile = file;


                    } else if (selectedImageView == 2) {
                        father_photo.setImageBitmap(photo);
                        fFile = file;
                    }
                }
                break;
        }
    }

    private void doCrop(int selectedImageView) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "Can not find image crop app", Toast.LENGTH_SHORT).show();
        } else {
            if (selectedImageView == 1)
                intent.setData(mImageCaptureUri);
            else if (selectedImageView == 2)
                intent.setData(fImageCaptureUri);


            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("crop", "true");
            intent.putExtra("return-data", true);

            if (size > 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            }
        }
    }

    public void setParentInfoData() {

        TextView mother_name = ((EditText) view.findViewById(R.id.mother_name));
        mother_name.setText(piw.getMother_name());
        TextView mother_surname = ((EditText) view.findViewById(R.id.mother_surname));
        mother_surname.setText(piw.getMother_surname());
        ImageView mother_photo = (ImageView) view.findViewById(R.id.mother_photo);
        mother_photo.setImageBitmap(piw.getMother_photo());

        TextView father_name = ((EditText) view.findViewById(R.id.father_name));
        father_name.setText(piw.getFather_name());
        TextView father_surname = ((EditText) view.findViewById(R.id.father_surname));
        father_surname.setText(piw.getFather_surname());
        ImageView father_photo = (ImageView) view.findViewById(R.id.father_photo);
        father_photo.setImageBitmap(piw.getFather_photo());

        TextView wedding_anniversary = ((EditText) view.findViewById(R.id.wedding_anniversary));
        wedding_anniversary.setText(piw.getWedding_anniversary());

    }

    public void saveParentInfo() {
        Activity activity = getActivity();

        SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String email = sp.getString("userName", null);

        String mother_name = ((EditText) activity.findViewById(R.id.mother_name)).getText().toString();
        String mother_surname = ((EditText) activity.findViewById(R.id.mother_surname)).getText().toString();
        String father_name = ((EditText) activity.findViewById(R.id.father_name)).getText().toString();
        String father_surname = ((EditText) activity.findViewById(R.id.father_surname)).getText().toString();
        String wedding_anniversary = ((EditText) activity.findViewById(R.id.wedding_anniversary)).getText().toString();

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        //if (AppUtility.isNotNull(mother_name) && AppUtility.isNotNull(mother_surname)) {

            // Put http parameters
            params.put("email", email);
            params.put("mother_name", mother_name);
            params.put("mother_surname", mother_surname);
            params.put("father_name", father_name);
            params.put("father_surname", father_surname);
            params.put("wedding_anniversary", wedding_anniversary);
        if (mFile != null)
            params.put("mother_photo", mFile.getName());
        else if (piw != null)
            params.put("mother_photo", piw.getMother_photo_name());
        if (fFile != null)
            params.put("father_photo", fFile.getName());
        else if (piw != null)
            params.put("father_photo", piw.getFather_photo_name());

            invokeSaveParentInfoWS(params);

        //} else {
        //    Toast.makeText(activity.getApplicationContext().getApplicationContext(), "Please fill the fields", Toast.LENGTH_LONG).show();
        //}

    }

    public void invokeSaveParentInfoWS(final RequestParams params) {
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(AppUtility.APP_URL + "rest/parentRegistrationRest/createParentInfo", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(new String(response));
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("result")) {
                        if (mFile != null)
                            saveToAWSS3(mFile, mFile.getName());
                        if (fFile != null)
                            saveToAWSS3(fFile, fFile.getName());

                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getView().getContext().getApplicationContext(), getView().getContext().getApplicationContext().getString(R.string.register_existinguser_msg), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getView().getContext().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getView().getContext().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @DebugLog
    private void saveToAWSS3(File image, String fileName) {
        SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

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

    @DebugLog
    private InputStream loadFromAWSS3(String photoURL, String subFolder, AmazonS3 s3Client) {
        try {
            System.out.println("Getting objects from S3 as stream\n");

            S3Object object = s3Client.getObject(new GetObjectRequest(
                    AppUtility.existingBucketName4Thumbnail, subFolder + "medium/" + photoURL));
            return object.getObjectContent();

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
        return null;
    }

    public AmazonS3 getAWS3Client(String userName, String validUser) {
        AWSTempToken awsTempToken = AppUtility.getAWSTempToken(getActivity().getApplicationContext(), userName, validUser);
        BasicSessionCredentials basicSessionCredentials =
                new BasicSessionCredentials(awsTempToken.getAccessKeyId(),
                        awsTempToken.getSecretAccessKey(),
                        awsTempToken.getSessionToken());
        return new AmazonS3Client(basicSessionCredentials);
    }


    class ParentInfoFetchTask extends AsyncTask<String, Void, ParentInfoWrapper> {

        private ProgressDialog progressDialog;
        private SharedPreferences sp;

        public ParentInfoFetchTask() {
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            progressDialog.setContentView(R.layout.progress_layout);
        }

        @Override
        protected void onPostExecute(ParentInfoWrapper piw) {
            progressDialog.dismiss();
            setParentInfoData();

        }

        protected ParentInfoWrapper doInBackground(String... urls) {
            Activity activity = getActivity();
            sp = activity.getApplicationContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", sp.getString("userName", null)));
            invokeRestWS(params);
            return piw;
        }

        public void invokeRestWS(List<NameValuePair> params) {
            Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/parentRegistrationRest/getParentInfo").buildUpon();

            HttpPost httpPost = new HttpPost(builder.toString());
            HttpClient client = new DefaultHttpClient();

            String subFolder = sp.getString("userId", "") + "/";
            AmazonS3 s3Client = getAWS3Client(sp.getString("userName", null), (sp.getBoolean("validUser", false) + ""));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response = client.execute(httpPost);

                String responseStr = EntityUtils.toString(response.getEntity());

                if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                    JSONObject obj = new JSONObject(responseStr);

                    if (obj.getString("parentInfoId") != null) {
                        piw.setMother_name(obj.getString("motherName"));
                        piw.setMother_surname(obj.getString("motherSurname"));
                        piw.setFather_name(obj.getString("fatherName"));
                        piw.setFather_surname(obj.getString("fatherSurname"));
                        piw.setWedding_anniversary(obj.getString("weddingAnniversary"));
                        piw.setMother_photo_name(obj.getString("photoURLMother"));
                        piw.setFather_photo_name(obj.getString("photoURLFather"));

                        if (piw.getMother_photo_name() != null && !piw.getMother_photo_name().equals("null")) {
                            InputStream inputStream = loadFromAWSS3(piw.getMother_photo_name(), subFolder, s3Client);
                            //todo: Image not found gibi bir mesaj koymamız gerekiyor.
                            if (inputStream != null) {
                                //byte[] data = IOUtils.toByteArray(inputStream);
                                //ByteArrayInputStream bin = new ByteArrayInputStream(data);
                                //Bitmap bm = loadFast(bin);
                                piw.setMother_photo(BitmapFactory.decodeStream(inputStream));
                            }
                        }

                        if (piw.getFather_photo_name() != null && !piw.getFather_photo_name().equals("null")) {
                            InputStream inputStream = loadFromAWSS3(piw.getFather_photo_name(), subFolder, s3Client);
                            //todo: Image not found gibi bir mesaj koymamız gerekiyor.
                            if (inputStream != null) {
                                //byte[] data = IOUtils.toByteArray(inputStream);
                                //ByteArrayInputStream bin = new ByteArrayInputStream(data);
                                //Bitmap bm = loadFast(bin);
                                piw.setFather_photo(BitmapFactory.decodeStream(inputStream));
                            }
                        }

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
