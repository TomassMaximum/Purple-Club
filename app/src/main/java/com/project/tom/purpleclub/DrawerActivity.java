package com.project.tom.purpleclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.json.JSONObject;

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

    private static final String TAG = "DrawerActivity";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            //当用户点击特定项时，引导用户进行登录。
            @Override
            public void onDrawerOpened(View drawerView) {
                //将Dribbble默认头像Round获取圆形头像并取代丑陋的安卓小头
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = false;
                Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.drawable.dribbble_default_avatar, options);
                AvatarDrawableFactory avatarFactory = new AvatarDrawableFactory(getResources());
                Drawable avatarDrawable = avatarFactory.getSquaredAvatarDrawable(avatar, avatar);
                ImageView avatarView = (ImageView)findViewById(R.id.user_avatar);
                avatarView.setImageDrawable(avatarDrawable);

                findViewById(R.id.text_press_to_sign_in).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                    }
                });
                findViewById(R.id.user_avatar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                    }
                });
                findViewById(R.id.text_not_signed).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                    }
                });
            }
        };

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

        preferences = getSharedPreferences("NerdPool", MODE_PRIVATE);
        String pref = preferences.getString(AuthorizationActivity.SHARED_PREFERENCE_KEY,"默认值");

        //调用方法请求用户基本信息
        getUserInfo(pref);

        Toast.makeText(DrawerActivity.this, "preference:" + pref, Toast.LENGTH_SHORT).show();

    }

    public void getUserInfo(String accessToken){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = GsonData.DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN + GsonData.BUCKETS_ID + "?" + GsonData.ACCESS_TOKEN + accessToken;
        Log.e(TAG,"URL:" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,"请求出错");
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
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
}
