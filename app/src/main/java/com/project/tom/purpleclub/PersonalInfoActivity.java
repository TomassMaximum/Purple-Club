package com.project.tom.purpleclub;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PersonalInfoActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ImageLoader imageLoader;
    DisplayImageOptions displayImageOptions;
    String access_token;

    OkHttpClient client;
    MyHandler myHandler;

    ImageView userAvatar;
    TextView idView;
    TextView usernameView;
    TextView html_urlView;
    TextView nameView;
    TextView locationView;
    TextView bioView;
    TextView webView;
    TextView twitterView;
    TextView followings_countView;
    TextView followers_countView;
    TextView buckets_countView;
    TextView projects_countView;
    TextView rebounds_received_countView;
    TextView comments_received_countView;
    TextView likes_countView;
    TextView likes_received_countView;
    TextView shots_countView;
    TextView can_upload_shotView;
    TextView teams_countView;
    TextView typeView;
    TextView proView;

    String name;
    String html_url;
    String avatar_url;
    String id;
    String username;
    String bio;
    String location;
    String buckets_count;
    String comments_received_count;
    String followers_count;
    String followings_count;
    String likes_count;
    String likes_received_count;
    String projects_count;
    String rebounds_received_count;
    String shots_count;
    String teams_count;
    String can_upload_shot;
    String type;
    String pro;
    String web;
    String twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("NerdPool", MODE_PRIVATE);

        getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));

        myHandler = new MyHandler();

        userAvatar = (ImageView) findViewById(R.id.user_avatar);
        idView = (TextView) findViewById(R.id.id);
        usernameView = (TextView) findViewById(R.id.user_name);
        html_urlView = (TextView) findViewById(R.id.html);
        nameView = (TextView) findViewById(R.id.name);
        locationView = (TextView) findViewById(R.id.location);
        bioView = (TextView) findViewById(R.id.bio);
        webView = (TextView) findViewById(R.id.web);
        twitterView = (TextView) findViewById(R.id.twitter);
        followings_countView = (TextView) findViewById(R.id.followings_count);
        followers_countView = (TextView) findViewById(R.id.followers_count);
        buckets_countView = (TextView) findViewById(R.id.buckets_count);
        projects_countView = (TextView) findViewById(R.id.projects_count);
        rebounds_received_countView = (TextView) findViewById(R.id.rebounds_received_count);
        comments_received_countView = (TextView) findViewById(R.id.comments_received_count);
        likes_countView = (TextView) findViewById(R.id.likes_count);
        likes_received_countView = (TextView) findViewById(R.id.likes_received_count);
        shots_countView = (TextView) findViewById(R.id.shots_count);
        can_upload_shotView = (TextView) findViewById(R.id.can_upload_shot);
        teams_countView = (TextView) findViewById(R.id.teams_count);
        typeView = (TextView) findViewById(R.id.type);
        proView = (TextView) findViewById(R.id.pro);

        imageLoader = ImageLoader.getInstance();
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.dribbble_default_avatar)
                .showImageOnFail(R.drawable.dribbble_default_avatar)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE,5))
                .build();
        if (imageLoader.isInited()){
            imageLoader.destroy();
        }
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

        String user_avatar_url = sharedPreferences.getString("user_avatar_url","");
        imageLoader.displayImage(user_avatar_url,userAvatar,displayImageOptions,new AnimateFirstDisplayListener());

        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        access_token = sharedPreferences.getString("access_token", "");
        final String url = Contract.DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN + "?" + Contract.ACCESS_TOKEN + access_token;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String JSONString = getJSONFromAPI(url);

                    JSONObject jsonObject = new JSONObject(JSONString);

                    name = jsonObject.getString("name");
                    html_url = jsonObject.getString("html_url");
                    avatar_url = jsonObject.getString("avatar_url");
                    id = jsonObject.getString("id");
                    username = jsonObject.getString("username");
                    bio = jsonObject.getString("bio");
                    location = jsonObject.getString("location");
                    buckets_count = jsonObject.getString("buckets_count");
                    comments_received_count = jsonObject.getString("comments_received_count");
                    followers_count = jsonObject.getString("followers_count");
                    followings_count = jsonObject.getString("followings_count");
                    likes_count = jsonObject.getString("likes_count");
                    likes_received_count = jsonObject.getString("likes_received_count");
                    projects_count = jsonObject.getString("projects_count");
                    rebounds_received_count = jsonObject.getString("rebounds_received_count");
                    shots_count = jsonObject.getString("shots_count");
                    teams_count = jsonObject.getString("teams_count");
                    can_upload_shot = jsonObject.getString("can_upload_shot");
                    type = jsonObject.getString("type");
                    pro = jsonObject.getString("pro");
                    JSONObject linksObject = jsonObject.getJSONObject("links");
                    web = linksObject.getString("web");
                    twitter = linksObject.getString("twitter");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_avatar_url",avatar_url);
                    editor.putString("username",username);
                    editor.putString("html_url",html_url);
                    editor.apply();

                    Message message = new Message();
                    myHandler.sendMessage(message);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            idView.setText(id);
            usernameView.setText(username);
            html_urlView.setText(html_url);
            nameView.setText(name);
            locationView.setText(location);
            if (bio.equals("")){
                bioView.setText("暂无");
            }else {
                bioView.setText(bio);
            }
            webView.setText(web);
            twitterView.setText(twitter);
            followings_countView.setText(followings_count);
            followers_countView.setText(followers_count);
            buckets_countView.setText(buckets_count);
            projects_countView.setText(projects_count);
            rebounds_received_countView.setText(rebounds_received_count);
            comments_received_countView.setText(comments_received_count);
            likes_countView.setText(likes_count);
            likes_received_countView.setText(likes_received_count);
            shots_countView.setText(shots_count);
            if (can_upload_shot.equals("false")){
                can_upload_shotView.setText("不允许");
            }else {
                can_upload_shotView.setText("允许");
            }
            teams_countView.setText(teams_count);
            if (type.equals("User")){
                typeView.setText("用户");
            }else {
                typeView.setText(type);
            }
            if (pro.equals("true")){
                proView.setText("会员");
            }else {
                proView.setText("非会员");
            }
        }
    }

    String getJSONFromAPI(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
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
