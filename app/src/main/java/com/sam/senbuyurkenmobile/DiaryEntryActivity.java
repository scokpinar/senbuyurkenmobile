package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class DiaryEntryActivity extends Activity {

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;
    private static int SELECT_PICTURE = 1;
    private static int CROP_IMG = 3;
    ImageView viewImage;
    OutputStream outFile = null;
    Bitmap bitmap;
    Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);


        viewImage = (ImageView) findViewById(R.id.imageView);
        viewImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera));

        viewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

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
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                selectedImage = data.getData();

                int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

                Picasso.with(this.getApplicationContext())
                        .load(selectedImage)
                        .transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .resize(size, size)
                        .centerInside()
                        .into(viewImage);
            }
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(DiaryEntryActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //if (options[item].equals("Take Photo")) {
                //    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                //    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                //    startActivityForResult(intent, 1);
                //} else if (options[item].equals("Choose from Gallery")) {
                if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public void cancelButtonClick(View view) {
        finish();
    }

    public void saveDiaryEntry(View view) {
        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        String entry_text = ((EditText) findViewById(R.id.diary_entry)).getText().toString();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("un", sp.getString("username", null));
        params.put("t", sp.getString("token", null));

        params.put("entry_text", entry_text);
        params.put("image", new File(AppUtility.getPathFromUri(getContentResolver(), selectedImage)));

        new SaveTask().execute(params);


    }



   /* public void saveDiaryEntry() {
        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        String entry_text = ((EditText) findViewById(R.id.diary_entry)).getText().toString();

        Map<String,Object> params = new HashMap<String,Object>();

        params.put("un", sp.getString("username", null));
        params.put("t", sp.getString("token", null));

        params.put("entry_text", entry_text);
        params.put("image", new File(AppUtility.getPathFromUri(getContentResolver(),selectedImage)));

                invokeWS(params);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

*/

    public void invokeWS(Map<String, Object> params) {

        Uri.Builder builder = Uri.parse(AppUtility.APP_URL + "rest/diaryEntryRest/createDiaryEntry").buildUpon();

        HttpPost httpPost = new HttpPost(builder.toString());
        HttpClient client = new DefaultHttpClient();

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setCharset(Charset.forName("UTF-8"));
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.addBinaryBody
                ("image", ((File) params.get("image")), ContentType.DEFAULT_BINARY, "" + System.currentTimeMillis() + ".jpg");
        entityBuilder.addTextBody("entry_text", ((String) params.get("entry_text")), ContentType.create("text/plain", Charset.forName("UTF-8")));
        entityBuilder.addTextBody("un", ((String) params.get("un")));
        entityBuilder.addTextBody("t", ((String) params.get("t")));
        HttpEntity entity = entityBuilder.build();

        httpPost.setEntity(entity);

        try {
            HttpResponse response = client.execute(httpPost);

            String responseStr = EntityUtils.toString(response.getEntity());

            if (responseStr != null && !responseStr.equals("null") && !responseStr.equals("")) {
                JSONObject obj = new JSONObject(responseStr);

                if (obj.getBoolean("result")) {
                    //Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.register_success_msg), Toast.LENGTH_LONG).show();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    class SaveTask extends AsyncTask<Map<String, Object>, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DiaryEntryActivity.this, "Progress Dialog Title Text", "Process Description Text", true);
            progressDialog.setContentView(R.layout.progress_layout);
        }

        @Override
        protected Boolean doInBackground(Map<String, Object>... params) {
            invokeWS(params[0]);
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
