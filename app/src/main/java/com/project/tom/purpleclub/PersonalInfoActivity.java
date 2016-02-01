package com.project.tom.purpleclub;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class PersonalInfoActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    RoundImage roundImage;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        userAvatar = (ImageView) findViewById(R.id.user_avatar);
        sharedPreferences = getSharedPreferences("NerdPool",MODE_PRIVATE);

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

        String id = sharedPreferences.getString("id","未知");
        String username = sharedPreferences.getString("username","未知");
        String html_url = sharedPreferences.getString("html_url","未知");
        String name = sharedPreferences.getString("name","未知");
        String location = sharedPreferences.getString("location","未知");
        String bio = sharedPreferences.getString("bio","未知");
        String web = sharedPreferences.getString("web","未知");
        String twitter = sharedPreferences.getString("twitter","未知");
        String followings_count = sharedPreferences.getString("followings_count","未知");
        String followers_count = sharedPreferences.getString("followers_count","未知");
        String buckets_count = sharedPreferences.getString("buckets_count","未知");
        String projects_count = sharedPreferences.getString("projects_count","未知");
        String rebounds_received_count = sharedPreferences.getString("rebounds_received_count","未知");
        String comments_received_count = sharedPreferences.getString("comments_received_count","未知");
        String likes_count = sharedPreferences.getString("likes_count","未知");
        String likes_received_count = sharedPreferences.getString("likes_received_count","未知");
        String shots_count = sharedPreferences.getString("shots_count","未知");
        String can_upload_shot = sharedPreferences.getString("can_upload_shot","未知");
        String teams_count = sharedPreferences.getString("teams_count","未知");
        String type = sharedPreferences.getString("type","未知");
        String pro = sharedPreferences.getString("pro","未知");

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

        setLocalAvatar();
    }

    public void setLocalAvatar(){
        String avatarLocalUrl = sharedPreferences.getString("avatarLocalUrl", "");
        Uri avatarUri = Uri.parse(avatarLocalUrl);
        Bitmap localAvatar = null;
        try {
            localAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(),avatarUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        roundImage = new RoundImage(localAvatar);
        userAvatar.setImageDrawable(roundImage);
    }
}
