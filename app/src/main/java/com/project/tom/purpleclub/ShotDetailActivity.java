package com.project.tom.purpleclub;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ShotDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShotDetailActivity";

    SQLiteDatabase db;
    Cursor cursor;

    SharedPreferences sharedPreferences;

    RecyclerView recyclerView;
    CommentRecyclerViewAdapter commentRecyclerViewAdapter;
    RecyclerView.LayoutManager layoutManager;

    MyHandler myHandler;
    MyDatabaseHelper myDatabaseHelper;

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
    TextView commentsTextView;

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

        myDatabaseHelper = new MyDatabaseHelper(this,"shots.db",null,5);

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
        commentsTextView = (TextView) findViewById(R.id.comments_text_view);

        myHandler = new MyHandler(this);

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

        recyclerView = (RecyclerView) findViewById(R.id.comments_recycler_view);
        commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(this,shot_id,commentsCount);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(commentRecyclerViewAdapter);

        userNameTextView.setText(userName);
        createdAtTextView.setText(finalTime);
        viewsCountTextView.setText(viewsCount);
        likesCountTextView.setText(likesCount);
        titleTextView.setText(title);
        descriptionTextView.setText(stringDescription);
        commentsTextView.setText("评论(" + commentsCount + ")");

        try {
            Bitmap avatar = BitmapFactory.decodeStream(openFileInput("avatar" + shot_id + ".png"));
            Drawable roundAvatarDrawable = new RoundImage(avatar);
            avatarImageView.setImageDrawable(roundAvatarDrawable);

            Bitmap image_small = BitmapFactory.decodeStream(openFileInput("image_small" + shot_id + ".png"));
            pictureImageView.setImageBitmap(image_small);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final String stringUrl = Contract.BASE_URL + shot_id + Contract.COMMENTS + Contract.ACCESS_TOKEN + access_token;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    URL url = new URL(stringUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line);
                    }
                    String responseComments = stringBuilder.toString();

                    JSONArray commentsObject = new JSONArray(responseComments);
                    for (int i = 0;i < commentsObject.length();i++){
                        String bio = "";
                        String location = "";
                        String web = "";
                        String twitter = "";
                        String teams_count = "";

                        JSONObject commentObject = commentsObject.getJSONObject(i);
                        String comment_id = commentObject.getString("id");
                        String body = commentObject.getString("body");
                        String likes_count = commentObject.getString("likes_count");
                        String created_at = commentObject.getString("created_at");

                        JSONObject userObject = commentObject.getJSONObject("user");
                        String user_id = userObject.getString("id");
                        String name = userObject.getString("name");
                        String username = userObject.getString("username");
                        String html_url = userObject.getString("html_url");
                        String avatar_url = userObject.getString("avatar_url");
                        if (userObject.has("bio")){
                            bio = userObject.getString("bio");
                        }
                        if (userObject.has("location")){
                            location = userObject.getString("location");
                        }

                        JSONObject linksObject = userObject.getJSONObject("links");
                        if (linksObject.has("web")){
                            web = linksObject.getString("web");
                        }
                        if (linksObject.has("twitter")){
                            twitter = linksObject.getString("twitter");
                        }

                        String buckets_count = userObject.getString("buckets_count");
                        String comments_received_count = userObject.getString("comments_received_count");
                        String followers_count = userObject.getString("followers_count");
                        String followings_count = userObject.getString("followings_count");
                        String user_likes_count = userObject.getString("likes_count");
                        String likes_received_count = userObject.getString("likes_received_count");
                        String projects_count = userObject.getString("projects_count");
                        String rebounds_received_count = userObject.getString("rebounds_received_count");
                        String shots_count = userObject.getString("shots_count");
                        if (userObject.has("teams_count")){
                            teams_count = userObject.getString("teams_count");
                        }
                        String can_upload_shot = userObject.getString("can_upload_shot");
                        String type = userObject.getString("type");
                        String pro = userObject.getString("pro");

                        ContentValues values = new ContentValues();
                        values.put("comment_id",comment_id);
                        values.put("body",body);
                        values.put("likes_count",likes_count);
                        values.put("created_at",created_at);

                        values.put("user_id",user_id);
                        values.put("name",name);
                        values.put("username",username);
                        values.put("html_url",html_url);
                        values.put("avatar_url",avatar_url);
                        values.put("bio",bio);
                        values.put("location",location);
                        values.put("web",web);
                        values.put("twitter",twitter);
                        values.put("buckets_count",buckets_count);
                        values.put("comments_received_count",comments_received_count);
                        values.put("followers_count",followers_count);
                        values.put("followings_count",followings_count);
                        values.put("user_likes_count",user_likes_count);
                        values.put("likes_received_count",likes_received_count);
                        values.put("projects_count",projects_count);
                        values.put("rebounds_received_count",rebounds_received_count);
                        values.put("shots_count",shots_count);
                        values.put("teams_count",teams_count);
                        values.put("can_upload_shot",can_upload_shot);
                        values.put("type", type);
                        values.put("pro", pro);

                        db = myDatabaseHelper.getWritableDatabase();
                        boolean flag = checkData(db,comment_id);

                        db.insert("comments",null,values);
                        Log.e(TAG,flag + body);

                        //如果标记为true，证明本地文件中存在该作品的图片和作者头像，不用再次请求，continue下一条信息。
                        if (flag){
                            //更新本地数据库中的数据
                            db.update("comments", values, "comment_id=?", new String[]{comment_id});
                            continue;
                        }

                        //如果数据不存在数据库中，向网络请求用户头像和作品图片
                        InputStream avatarIn;
                        avatarIn = new URL(avatar_url).openStream();
                        Bitmap avatar = BitmapFactory.decodeStream(avatarIn);
                        avatarIn.close();

                        //将请求到的作者头像保存到本地
                        FileOutputStream avatarOut = openFileOutput("avatar" + user_id + ".png", MODE_PRIVATE);
                        avatar.compress(Bitmap.CompressFormat.PNG,100,avatarOut);
                        avatarOut.close();
                    }
                    db.close();

                    Message message = new Message();
                    message.obj = this;
                    Bundle bundle = new Bundle();
                    bundle.putString("shot_id",shot_id);
                    bundle.putString("comments_count",commentsCount);
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            public boolean checkData(SQLiteDatabase db,String comment_id){
                //通过id判断数据库中是否已经有此条记录，有则置标记为true
                boolean flag = false;
                Cursor cursor = db.query("comments",new String[]{"comment_id"},null,null,null,null,null);

                if (cursor.moveToFirst()){
                    do {
                        String id = cursor.getString(cursor.getColumnIndex("comment_id"));

                        if (id.equals(comment_id)) {
                            flag = true;
                            break;
                        }
                    }while (cursor.moveToNext());
                }
                cursor.close();

                return flag;
            }
        }).start();
    }

    private class MyHandler extends Handler{

        ShotDetailActivity shotDetailActivity;

        public MyHandler(ShotDetailActivity shotDetailActivity){
            this.shotDetailActivity = shotDetailActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            String shot_id = msg.getData().getString("shot_id");
            String comments_count = msg.getData().getString("comments_count");
            Log.e(TAG,"开始handle消息" + commentsCount);
            commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(shotDetailActivity,shot_id,comments_count);
            recyclerView.setLayoutManager(new LinearLayoutManager(shotDetailActivity));
            recyclerView.setAdapter(commentRecyclerViewAdapter);
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

class CommentRecyclerViewAdapter extends RecyclerView.Adapter{

    ShotDetailActivity shotDetailActivity;
    private final String TAG = "CommentRecyclerAdapter";
    String shot_id;
    String comments_count;

    public CommentRecyclerViewAdapter(ShotDetailActivity shotDetailActivity,String shot_id,String comments_count){
        this.shotDetailActivity = shotDetailActivity;
        this.shot_id = shot_id;
        this.comments_count = comments_count;
    }

    class CommentRecyclerViewHolder extends RecyclerView.ViewHolder{

        private View view;

        ImageView commenterAvatarImageView;
        ImageView likesIconImageView;

        TextView commentTimeTextView;
        TextView commenterUserNameTextView;
        TextView likesCountTextView;
        TextView commentBodyTextView;

        public CommentRecyclerViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            commenterAvatarImageView = (ImageView) view.findViewById(R.id.commenter_avatar);
            likesIconImageView = (ImageView) view.findViewById(R.id.likes_icon);
            commentTimeTextView = (TextView) view.findViewById(R.id.comment_time);
            commenterUserNameTextView = (TextView) view.findViewById(R.id.commenter_user_name);
            likesCountTextView = (TextView) view.findViewById(R.id.comment_likes_count);
            commentBodyTextView = (TextView) view.findViewById(R.id.comment_body);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shots_comments_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.e(TAG,"开始绑定视图");
        final CommentRecyclerViewHolder recyclerHolder = (CommentRecyclerViewHolder) holder;

        shotDetailActivity.myDatabaseHelper = new MyDatabaseHelper(shotDetailActivity,"shots.db",null,5);

        shotDetailActivity.db = shotDetailActivity.myDatabaseHelper.getWritableDatabase();
        String[] projection = {"created_at","username","likes_count","body","user_id","comment_id"};
        Cursor cursor = shotDetailActivity.db.query("comments",projection,null,null,null,null,null);

        if (cursor.moveToFirst()){
            cursor.moveToPosition(position);

            final String user_id = cursor.getString(cursor.getColumnIndex("user_id"));
            final String body = cursor.getString(cursor.getColumnIndex("body"));
            final String username = cursor.getString(cursor.getColumnIndex("username"));
            final String likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));
            final String created_at = cursor.getString(cursor.getColumnIndex("created_at"));
            final String comment_id = cursor.getString(cursor.getColumnIndex("comment_id"));

            Log.e(TAG,"评论为：" + body);

            try {
                Bitmap avatar = BitmapFactory.decodeStream(shotDetailActivity.openFileInput("avatar" + user_id + ".png"));
                Drawable roundAvatarDrawable = new RoundImage(avatar);
                recyclerHolder.commenterAvatarImageView.setImageDrawable(roundAvatarDrawable);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            final SharedPreferences sharedPreferences = shotDetailActivity.getSharedPreferences("NerdPool", Context.MODE_PRIVATE);
            final String access_token = sharedPreferences.getString("access_token", "");

            String formattedBody = Html.fromHtml(body).toString();

            recyclerHolder.commenterUserNameTextView.setText(username);
            recyclerHolder.commentBodyTextView.setText(formattedBody);
            recyclerHolder.likesCountTextView.setText(likes_count);

            boolean liked = sharedPreferences.getBoolean( comment_id + "liked",false);
            if (!liked){
                recyclerHolder.likesCountTextView.setTextColor(shotDetailActivity.getResources().getColor(R.color.black));
                recyclerHolder.likesIconImageView.setColorFilter(shotDetailActivity.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            }else {
                recyclerHolder.likesCountTextView.setTextColor(shotDetailActivity.getResources().getColor(R.color.colorPrimary));
                recyclerHolder.likesIconImageView.setColorFilter(shotDetailActivity.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            }

            //用户点击喜欢图标或按钮，向服务器post一个喜欢请求或取消一个喜欢。
            recyclerHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClicked(comment_id,shot_id,access_token,likes_count,sharedPreferences,recyclerHolder);
                }
            });
            recyclerHolder.likesIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClicked(comment_id,shot_id,access_token,likes_count, sharedPreferences,recyclerHolder);
                }
            });

            String[] parts = created_at.split("T");
            String dayPart = parts[0];
            String timePart = parts[1];
            String[] dayParts = dayPart.split("-");
            String day = dayParts[1] + "-" + dayParts[2];
            String timeClean = timePart.replaceAll("Z","");
            String[] timeParts = timeClean.split(":");
            String time = timeParts[0] + ":" + timeParts[1];
            String finalTime = day + "  " + time;

            recyclerHolder.commentTimeTextView.setText(finalTime);

        }else {
            Log.e(TAG,"cursor为空");
        }
        cursor.close();
        shotDetailActivity.db.close();
    }

    @Override
    public int getItemCount() {
        return Integer.parseInt(comments_count);
    }

    public void likeClicked(String comment_id,String shot_id,String access_token,String likesCount,SharedPreferences sharedPreferences,CommentRecyclerViewHolder recyclerHolder){
        Log.e(TAG,"被点击了");
        boolean liked = sharedPreferences.getBoolean(comment_id + "liked",false);
        if (!liked){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(comment_id + "liked",true);
            editor.commit();
            final String likeURL = Contract.BASE_URL + shot_id + Contract.COMMENTS_TWO + comment_id + "/like?access_token=" + access_token;
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
            recyclerHolder.likesIconImageView.setColorFilter(shotDetailActivity.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(shotDetailActivity.getResources().getColor(R.color.colorPrimary));
            int likes = Integer.parseInt(likesCount) + 1;
            recyclerHolder.likesCountTextView.setText(Integer.toString(likes));
            Toast.makeText(shotDetailActivity, "已喜欢", Toast.LENGTH_SHORT).show();
        }else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(comment_id + "liked",false);
            editor.commit();
            final String likeURL = Contract.BASE_URL + shot_id + Contract.COMMENTS_TWO + comment_id + "/like?access_token=" + access_token;
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
            recyclerHolder.likesIconImageView.setColorFilter(shotDetailActivity.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(shotDetailActivity.getResources().getColor(R.color.black));
            recyclerHolder.likesCountTextView.setText(Integer.toString(Integer.parseInt(likesCount)));
            Toast.makeText(shotDetailActivity, "已取消喜欢", Toast.LENGTH_SHORT).show();
        }
    }
}
