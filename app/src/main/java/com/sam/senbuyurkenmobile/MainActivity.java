package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    DiaryPageActivity fragmentDiaryPage;
    BabyInfoActivity fragmentBabyInfo;
    ParentInfoActivity fragmentParentInfo;
    AppInfoActivity fragmentAppInfo;
    Fragment[] fragments;
    String[] fragmentTAGS = new String[]{"fragment_1", "fragment_2", "fragment_3", "fragment_4"};
    ArrayList<NavigationItem> mNavItems = new ArrayList<NavigationItem>();
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerPane;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String drawerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerTitle = getResources().getString(R.string.app_name);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        fragmentDiaryPage = new DiaryPageActivity();
        ft.add(R.id.content_frame, fragmentDiaryPage, fragmentTAGS[0]);
        ft.commit();

        fragmentAppInfo = new AppInfoActivity();
        fragmentBabyInfo = new BabyInfoActivity();
        fragmentParentInfo = new ParentInfoActivity();

        fragments = new Fragment[]{fragmentDiaryPage, fragmentBabyInfo, fragmentParentInfo, fragmentAppInfo};

        getActionBar().setTitle(drawerTitle);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();

            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        String[] menuNames = getResources().getStringArray(R.array.menu);

        for (int i = 0; i < menuNames.length; i++) {
            String menuName = menuNames[i];
            mNavItems.add(new NavigationItem(menuName, "", R.drawable.ic_camera));
        }

        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);

        mDrawerList.setAdapter(adapter);

        SharedPreferences sp = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        TextView userName = (TextView) findViewById(R.id.userName);
        userName.setText(sp.getString("username", null));

        getActionBar().setHomeButtonEnabled(true);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String[] menuItems = getResources().getStringArray(R.array.menu);

                drawerTitle = menuItems[position];

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if (getFragmentManager().findFragmentByTag(fragmentTAGS[position]) == null) {
                    ft.add(R.id.content_frame, fragments[position], fragmentTAGS[position]);
                }
                for (int i = 0; i < fragments.length; i++) {
                    if (i == position) {
                        ft.show(fragments[i]);
                    } else {
                        if (getFragmentManager().findFragmentByTag(fragmentTAGS[i]) != null) {
                            ft.hide(fragments[i]);
                        }
                    }
                }
                ft.commit();

                //mDrawerLayout.closeDrawer(mDrawerList);
                mDrawerLayout.closeDrawer(mDrawerPane);

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}