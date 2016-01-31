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

import java.io.IOException;

public class PersonalInfoActivity extends AppCompatActivity {

    ImageView userAvatar;
    SharedPreferences sharedPreferences;
    RoundImage roundImage;

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
