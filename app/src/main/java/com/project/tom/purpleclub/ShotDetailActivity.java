package com.project.tom.purpleclub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShotDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShotDetailActivity";

    SQLiteDatabase db;
    MyDatabaseHelper myDatabaseHelper;
    Cursor cursor;

    SharedPreferences sharedPreferences;

    ImageView pictureImageView;
    ImageView avatarImageView;
    ImageView likesIconImageView;
    ImageView viewsIconImageView;

    TextView userNameTextView;
    TextView createdAtTextView;
    TextView viewsCountTextView;
    TextView likesCountTextView;
    TextView titleTextView;
    TextView descriptionTextView;

    String userName;
    String createdAt;
    String viewsCount;
    String likesCount;
    String description;
    String commentsCount;
    String stringDescription;
    String shot_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_detail);

        //将应用栏的文字设为当前shot的标题
        String title = getIntent().getStringExtra("title");
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }

        //获取当前的shot id，向数据库请求相应的数据
        shot_id = getIntent().getStringExtra("shot_id");
        Log.e(TAG,"Shot_ID为：" + shot_id);

        myDatabaseHelper = new MyDatabaseHelper(this,"shots.db",null,4);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"description","username","views_count","comments_count","likes_count","created_at"};

        avatarImageView = (ImageView) findViewById(R.id.publisher_avatar);
        pictureImageView = (ImageView) findViewById(R.id.shot_picture);
        likesIconImageView = (ImageView) findViewById(R.id.likes_icon);
        viewsIconImageView = (ImageView) findViewById(R.id.views_icon);

        userNameTextView = (TextView) findViewById(R.id.publisher_name);
        createdAtTextView = (TextView) findViewById(R.id.publish_time);
        viewsCountTextView = (TextView) findViewById(R.id.views_count);
        likesCountTextView = (TextView) findViewById(R.id.likes_count);
        titleTextView = (TextView) findViewById(R.id.shot_detail_title);
        descriptionTextView = (TextView) findViewById(R.id.shot_detail_description);

        String[] args = new String[]{shot_id};
        Cursor cursor_one = db.query("shots_popularity",projection,"shot_id=?",args,null,null,null);
        Cursor cursor_two = db.query("shots_recent",projection,"shot_id=?",args,null,null,null);
        Cursor cursor_three = db.query("shots_views",projection,"shot_id=?",args,null,null,null);
        Cursor cursor_four = db.query("shots_comments",projection,"shot_id=?",args,null,null,null);

        if (cursor_one.moveToFirst()) {
            cursor = cursor_one;
        }else if (cursor_two.moveToFirst()){
            cursor = cursor_two;
        }else if (cursor_three.moveToFirst()){
            cursor = cursor_three;
        }else if (cursor_four.moveToFirst()){
            cursor = cursor_four;
        }
        if (cursor.moveToFirst()){
            do {
                description = cursor.getString(cursor.getColumnIndex("description"));
                userName = cursor.getString(cursor.getColumnIndex("username"));
                viewsCount = cursor.getString(cursor.getColumnIndex("views_count"));
                commentsCount = cursor.getString(cursor.getColumnIndex("comments_count"));
                likesCount = cursor.getString(cursor.getColumnIndex("likes_count"));
                createdAt = cursor.getString(cursor.getColumnIndex("created_at"));
            }while (cursor.moveToNext());
        }else {
            Log.e(TAG,"cursor为空");
        }
        cursor_one.close();
        cursor_two.close();
        cursor_three.close();
        cursor_four.close();
        cursor.close();

        if (!description.equals("null")){
            stringDescription = Html.fromHtml(description).toString();
        }else {
            stringDescription = "暂无描述";
        }

        String[] parts = createdAt.split("T");
        String dayPart = parts[0];
        String timePart = parts[1];
        String[] dayParts = dayPart.split("-");
        String day = dayParts[1] + "-" + dayParts[2];
        String timeClean = timePart.replaceAll("Z","");
        String[] timeParts = timeClean.split(":");
        String time = timeParts[0] + ":" + timeParts[1];
        String finalTime = "创建于:   " + time + "  " + day;

        sharedPreferences = getSharedPreferences("NerdPool", Context.MODE_PRIVATE);

        final String access_token = sharedPreferences.getString("access_token","");

        boolean liked = sharedPreferences.getBoolean(shot_id + "liked",false);
        if (!liked){
            likesCountTextView.setTextColor(getResources().getColor(R.color.black));
            likesIconImageView.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        }else {
            likesCountTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            likesIconImageView.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }

        viewsIconImageView.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        //用户点击喜欢图标或按钮，向服务器post一个喜欢请求或取消一个喜欢。
        likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeClicked(shot_id, access_token,likesCount,sharedPreferences);
            }
        });
        likesIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeClicked(shot_id, access_token,likesCount,sharedPreferences);
            }
        });

        userNameTextView.setText(userName);
        createdAtTextView.setText(finalTime);
        viewsCountTextView.setText(viewsCount);
        likesCountTextView.setText(likesCount);
        titleTextView.setText(title);
        descriptionTextView.setText(stringDescription);

        try {
            Bitmap avatar = BitmapFactory.decodeStream(openFileInput("avatar" + shot_id + ".png"));
            Drawable roundAvatarDrawable = new RoundImage(avatar);
            avatarImageView.setImageDrawable(roundAvatarDrawable);

            Bitmap image_small = BitmapFactory.decodeStream(openFileInput("image_small" + shot_id + ".png"));
            pictureImageView.setImageBitmap(image_small);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void likeClicked(String shot_id,String access_token,String likesCount,SharedPreferences sharedPreferences){
        Log.e(TAG,"被点击了");
        boolean liked = sharedPreferences.getBoolean(shot_id + "liked",false);
        if (!liked){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(shot_id + "liked",true);
            editor.commit();
            final String likeURL = Contract.BASE_URL + shot_id + "/like?access_token=" + access_token;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection;
                    try {
                        URL url = new URL(likeURL);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        InputStream in = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null){
                            stringBuilder.append(line);
                        }
                        String response = stringBuilder.toString();
                        Log.e(TAG,response);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            likesIconImageView.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            likesCountTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            int likes = Integer.parseInt(likesCount) + 1;
            likesCountTextView.setText(Integer.toString(likes));
            Toast.makeText(this, "已喜欢", Toast.LENGTH_SHORT).show();
        }else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(shot_id + "liked",false);
            editor.commit();
            final String likeURL = Contract.BASE_URL + shot_id + "/like?access_token=" + access_token;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection;
                    try {
                        URL url = new URL(likeURL);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("DELETE");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        InputStream in = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null){
                            stringBuilder.append(line);
                        }
                        String response = stringBuilder.toString();
                        Log.e(TAG,response);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            likesIconImageView.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            likesCountTextView.setTextColor(getResources().getColor(R.color.black));
            likesCountTextView.setText(Integer.toString(Integer.parseInt(likesCount)));
            Toast.makeText(this, "已取消喜欢", Toast.LENGTH_SHORT).show();
        }
    }
}
