package com.project.tom.purpleclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DrawerActivity";
    SharedPreferences preferences;
    ImageView userAvatarImageView;
    TextView userName;
    TextView userDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new DrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

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
    }

    private class DrawerToggle extends ActionBarDrawerToggle{

        DrawerActivity drawerActivity;
        ImageLoader imageLoader;
        DisplayImageOptions displayImageOptions;

        public DrawerToggle(DrawerActivity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
            this.drawerActivity = activity;
        }

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
            final String user_avatar_url = preferences.getString("user_avatar_url", "");
            final String username = preferences.getString("username", "未登录");
            final String html = preferences.getString("html_url", "点击登录");

            userName.setText(username);
            userDescription.setText(html);

            imageLoader = ImageLoader.getInstance();
            if (imageLoader.isInited()){
                imageLoader.destroy();
            }
            imageLoader.init(ImageLoaderConfiguration.createDefault(drawerActivity));
            displayImageOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.dribbble_default_avatar)
                    .showImageOnFail(R.drawable.dribbble_default_avatar)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new CircleBitmapDisplayer(Color.WHITE,5))
                    .build();
            imageLoader.displayImage(user_avatar_url, userAvatarImageView,displayImageOptions,new AnimateFirstDisplayListener());

            if (!signedIn){
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
                userAvatarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setTransitionName("user_avatar");
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(drawerActivity,v,v.getTransitionName());

                        startActivity(new Intent(DrawerActivity.this, PersonalInfoActivity.class),activityOptionsCompat.toBundle());
                    }
                });

                userName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userAvatarImageView.setTransitionName("user_avatar");
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(drawerActivity,userAvatarImageView,userAvatarImageView.getTransitionName());

                        startActivity(new Intent(DrawerActivity.this, PersonalInfoActivity.class),activityOptionsCompat.toBundle());
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
            imageLoader.destroy();
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

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
