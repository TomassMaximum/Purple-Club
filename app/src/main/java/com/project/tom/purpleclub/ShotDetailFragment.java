package com.project.tom.purpleclub;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Tom on 2016/2/17.
 */
public class ShotDetailFragment extends Fragment {

    private static final String TAG = "ShotDetailFragment";

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

    String avatar_url;
    String image_url;
    String image_big_url;
    String image_normal_url;
    String image_small_url;
    String userName;
    String createdAt;
    String viewsCount;
    String likesCount;
    String description;
    String commentsCount;
    String stringDescription;
    String title;
    static String shot_id;

    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase db;
    Cursor cursor;
    SharedPreferences sharedPreferences;

    private DisplayImageOptions optionsAvatar;
    private DisplayImageOptions optionsImage;
    AnimateFirstDisplayListener animateFirstDisplayListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();

    public static ShotDetailFragment newInstance(String shotId){
        shot_id = shotId;

        return new ShotDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shot_detail,container,false);

        getActivity().getWindow().setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.shared_element_transition));

        avatarImageView = (ImageView) rootView.findViewById(R.id.publisher_avatar);
        pictureImageView = (ImageView) rootView.findViewById(R.id.shot_picture);
        likesIconImageView = (ImageView) rootView.findViewById(R.id.likes_icon);
        viewsIconImageView = (ImageView) rootView.findViewById(R.id.views_icon);

        userNameTextView = (TextView) rootView.findViewById(R.id.publisher_name);
        createdAtTextView = (TextView) rootView.findViewById(R.id.publish_time);
        viewsCountTextView = (TextView) rootView.findViewById(R.id.views_count);
        likesCountTextView = (TextView) rootView.findViewById(R.id.likes_count);
        titleTextView = (TextView) rootView.findViewById(R.id.shot_detail_title);
        descriptionTextView = (TextView) rootView.findViewById(R.id.shot_detail_description);

        myDatabaseHelper = new MyDatabaseHelper(getActivity(),"shots.db",null,5);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"title","description","username","views_count","comments_count","likes_count","created_at","avatar_url","image_small_url","image_normal_url","image_big_url"};

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
                title = cursor.getString(cursor.getColumnIndex("title"));
                description = cursor.getString(cursor.getColumnIndex("description"));
                userName = cursor.getString(cursor.getColumnIndex("username"));
                viewsCount = cursor.getString(cursor.getColumnIndex("views_count"));
                commentsCount = cursor.getString(cursor.getColumnIndex("comments_count"));
                likesCount = cursor.getString(cursor.getColumnIndex("likes_count"));
                createdAt = cursor.getString(cursor.getColumnIndex("created_at"));
                avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
                image_small_url = cursor.getString(cursor.getColumnIndex("image_small_url"));
                image_normal_url = cursor.getString(cursor.getColumnIndex("image_normal_url"));
                image_big_url = cursor.getString(cursor.getColumnIndex("image_big_url"));
            }while (cursor.moveToNext());
        }else {
            Log.e(TAG, "cursor为空");
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

        sharedPreferences = getActivity().getSharedPreferences("NerdPool", Context.MODE_PRIVATE);

        final String access_token = sharedPreferences.getString("access_token","");

        boolean liked = sharedPreferences.getBoolean(shot_id + "liked",false);
        if (!liked){
            likesCountTextView.setTextColor(getResources().getColor(R.color.black));
            likesIconImageView.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        }else {
            likesCountTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            likesIconImageView.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }

        viewsCountTextView.setTextColor(getResources().getColor(R.color.black));
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
                likeClicked(shot_id, access_token, likesCount, sharedPreferences);
            }
        });

        userNameTextView.setText(userName);
        createdAtTextView.setText(finalTime);
        viewsCountTextView.setText(viewsCount);
        likesCountTextView.setText(likesCount);
        titleTextView.setText(title);
        descriptionTextView.setText(stringDescription);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("shot_comments_count", commentsCount);
        editor.apply();

        optionsImage = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(3000))
                .build();

        optionsAvatar = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE,5))
                .build();

        if (!image_big_url.equals("null")){
            image_url = image_big_url;
        }else if (!image_normal_url.equals("null")){
            image_url = image_normal_url;
        }else{
            image_url = image_small_url;
        }

        imageLoader.displayImage(avatar_url, avatarImageView,optionsAvatar,animateFirstDisplayListener);
        imageLoader.displayImage(image_url, pictureImageView, optionsImage, animateFirstDisplayListener);

        return rootView;
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

    public void likeClicked(String shot_id,String access_token,String likesCount,SharedPreferences sharedPreferences){
        Log.e(TAG,"被点击了");
        boolean liked = sharedPreferences.getBoolean(shot_id + "liked",false);
        if (!liked){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(shot_id + "liked",true);
            editor.apply();
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
            Toast.makeText(getActivity(), "已喜欢", Toast.LENGTH_SHORT).show();
        }else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(shot_id + "liked",false);
            editor.apply();
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
            Toast.makeText(getActivity(), "已取消喜欢", Toast.LENGTH_SHORT).show();
        }
    }
}
