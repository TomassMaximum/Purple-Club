package com.project.tom.purpleclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DrawerActivity";
    SharedPreferences preferences;
    RoundImage roundImage;
    public static final int UPDATE_DRAWER = 1;
    public static final int UPDATE_AVATAR = 2;

    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase db;

    ImageView userAvatarImageView;
    TextView userName;
    TextView userDescription;
    Fragment fragment;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_DRAWER:
                    userAvatarImageView.setImageDrawable(roundImage);
                    break;
                case UPDATE_AVATAR:
                    userAvatarImageView.setImageDrawable(roundImage);
                default:
                    break;
            }
        }
    };

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

                //找到用户头像，用户ID，用户描述三个控件
                userAvatarImageView = (ImageView) findViewById(R.id.user_avatar);
                userName = (TextView) findViewById(R.id.text_not_signed);
                userDescription = (TextView) findViewById(R.id.text_press_to_sign_in);

                //如果用户未登录，显示Dribbble的默认头像，点击引导用户进入登录界面进行授权
                preferences = getSharedPreferences("NerdPool", MODE_PRIVATE);
                Boolean signedIn = preferences.getBoolean("SignedIn",false);

                if (!signedIn){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap userAvatar = Utils.getResizedBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dribbble_default_avatar),180,180);
                            roundImage = new RoundImage(userAvatar);
                            Message message = new Message();
                            message.what = UPDATE_DRAWER;
                            handler.sendMessage(message);
                        }
                    }).start();

                    userDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });
                    userAvatarImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });
                    userName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });

                }else {
                    String name = preferences.getString("username","");
                    String html = preferences.getString("html_url","");
                    userName.setText(name);
                    userDescription.setText(html);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setLocalAvatar();
                            Message message = new Message();
                            message.what = UPDATE_AVATAR;
                            handler.sendMessage(message);
                        }
                    }).start();

                    userAvatarImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DrawerActivity.this, PersonalInfoActivity.class));
                        }
                    });

                    userName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DrawerActivity.this,PersonalInfoActivity.class));
                        }
                    });

                    userDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String html_url = preferences.getString("html_url","");
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(html_url));
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_top);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","top");
            fragmentContainer.setArguments(args);
            transaction.add(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        }
//        if (savedInstanceState == null) {
//            navigationView.setCheckedItem(R.id.nav_new_data);
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            FragmentPage fragment = new FragmentPage();
//            transaction.replace(R.id.fragment_container, fragment);
//            transaction.commit();
//        }
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

        if (id == R.id.nav_top) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","top");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        } else if (id == R.id.nav_new_show) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","new_show");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        } else if (id == R.id.nav_gif_animation) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","gif_animation");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        } else if (id == R.id.nav_season_winner) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","season_winner");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        } else if (id == R.id.nav_team_work) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","team_work");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        }else if (id == R.id.nav_second_production){
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            FragmentContainer fragmentContainer = new FragmentContainer();
            Bundle args = new Bundle();
            args.putString("drawerPosition","second_production");
            fragmentContainer.setArguments(args);
            transaction.replace(R.id.fragment_container, fragmentContainer);
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void setLocalAvatar(){
        String avatarLocalUrl = preferences.getString("avatarLocalUrl", "");
        Uri avatarUri = Uri.parse(avatarLocalUrl);
        Bitmap localAvatar = null;
        try {
            localAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(),avatarUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (localAvatar != null){
            roundImage = new RoundImage(localAvatar);
        }
    }
}
