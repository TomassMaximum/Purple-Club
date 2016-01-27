package com.project.tom.purpleclub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE = 1;
    private static final String TAG = "DrawerActivity";
    String requestURL;
    MenuItem navNewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_new_data);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewsFragment fragment = new NewsFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        findViewById(R.id.text_press_to_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
            }
        });

        if (id == R.id.nav_new_data) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewsFragment fragment = new NewsFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        } else if (id == R.id.nav_club_show) {

        } else if (id == R.id.nav_my_club) {

        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_message) {

        }else if (id == R.id.nav_settings){

        }else if (id == R.id.nav_version_info){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult被执行" + resultCode + "::" + RESULT_OK);
        if(resultCode == RESULT_OK){
            String url = data.getStringExtra("code");
            requestURL = "https://dribbble.com/oauth/token?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&client_secret=7260ba76972c21b693c6960d976f991454930ef19c69eb9e1ed944dee82a1feb" + url;

            //调用方法来通过获取的code来请求Access Token用于调用Dribbble的api。
            sendRequest();

        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String response = (String) msg.obj;
            Log.e("Access Token",response);
        }
    };

    public void sendRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("https://dribbble.com/oauth/token?");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&client_secret=7260ba76972c21b693c6960d976f991454930ef19c69eb9e1ed944dee82a1feb" + url);

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        response.append(line);
                    }
                    if (response == null){
                        Log.e(TAG,"response为空");
                    }else{
                        Log.e(TAG,response.toString());
                    }

                    Message message = new Message();
                    message.obj = response.toString();
                    handler.sendMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
