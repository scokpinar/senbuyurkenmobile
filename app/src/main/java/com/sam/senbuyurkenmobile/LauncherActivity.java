package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launcher);

        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 0.5 seconds
                    sleep(1 * 500);

                    SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                    boolean userLoggedIn = sp.getBoolean("userLoggedIn", false);

                    if (userLoggedIn) {
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(i);
                    }
                    finish();

                } catch (Exception e) {
                }
            }
        };

        // start thread
        background.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_launcher, menu);
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

}