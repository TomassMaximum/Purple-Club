package com.project.tom.purpleclub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
 * Created by Tom on 2016/1/25.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    public final String TAG = "RecyclerViewAdapter";
    MyDatabaseHelper myDatabaseHelper;
    FragmentPage fragmentPage;
    Cursor cursor;
    SQLiteDatabase db;
    Bitmap image_small;
    RoundImage roundAvatarDrawable;
    String finalImageUrl;
    int page;
    String drawerPosition;

    private DisplayImageOptions optionsPicture;
    private DisplayImageOptions optionsAvatar;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();

    RecyclerViewAdapter(FragmentPage fragmentPage,int page,String drawerPosition){
        this.fragmentPage = fragmentPage;
        this.page = page;
        this.drawerPosition = drawerPosition;

        optionsAvatar = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE,5))
                .build();

        optionsPicture = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(2000))
                .build();
    }

    class RecyclerHolder extends RecyclerView.ViewHolder{
        private View view;

        ImageView avatarImageView;
        TextView titleTextView;
        ImageView pictureImageView;
        TextView viewsCountTextView;
        TextView commentsCountTextView;
        ImageView likesIconImageView;
        TextView likesCountTextView;
        TextView createdTime;
        ImageView viewsIconImageView;
        ImageView commentsIconImageView;

        LinearLayout container;

        public RecyclerHolder(View itemView) {
            super(itemView);
            view = itemView;

            container = (LinearLayout) itemView.findViewById(R.id.fragment_container);

            avatarImageView = (ImageView) itemView.findViewById(R.id.publisher_avatar);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            pictureImageView = (ImageView) itemView.findViewById(R.id.picture);
            viewsCountTextView = (TextView) itemView.findViewById(R.id.views_count);
            commentsCountTextView = (TextView) itemView.findViewById(R.id.comments_count);
            likesIconImageView = (ImageView) itemView.findViewById(R.id.likes_icon);
            likesCountTextView = (TextView) itemView.findViewById(R.id.likes_count);
            createdTime = (TextView) itemView.findViewById(R.id.created_time);
            viewsIconImageView = (ImageView) itemView.findViewById(R.id.view_icon);
            commentsIconImageView = (ImageView) itemView.findViewById(R.id.comments_icon);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerHolder recyclerHolder = (RecyclerHolder) holder;

        myDatabaseHelper = new MyDatabaseHelper(fragmentPage.getContext(),"shots.db",null,6);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"id","drawer_position","page_position","shot_id","title","avatar_url","image_small_url","image_normal_url","views_count","comments_count","likes_count","created_at"};

        cursor = db.query("shots",projection,"drawer_position=? and page_position=?",new String[]{drawerPosition,Integer.toString(page)},null,null,"id DESC","12");

        Log.e(TAG,drawerPosition + "+" + page);
        if (cursor.moveToFirst()){
            cursor.moveToPosition(position);
            Log.e(TAG,position + "");
            //final String id = cursor.getString(cursor.getColumnIndex("id"));

            final String shot_id = cursor.getString(cursor.getColumnIndex("shot_id"));
            final String avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
            final String title = cursor.getString(cursor.getColumnIndex("title"));
            final String image_small_url = cursor.getString(cursor.getColumnIndex("image_small_url"));
            final String image_normal_url = cursor.getString(cursor.getColumnIndex("image_normal_url"));
            final String views_count = cursor.getString(cursor.getColumnIndex("views_count"));
            final String comments_count = cursor.getString(cursor.getColumnIndex("comments_count"));
            final String likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));
            final String created_at = cursor.getString(cursor.getColumnIndex("created_at"));

            final SharedPreferences sharedPreferences = fragmentPage.getActivity().getSharedPreferences("NerdPool", Context.MODE_PRIVATE);
            final String access_token = sharedPreferences.getString("access_token","");

            recyclerHolder.titleTextView.setText(title);
            recyclerHolder.viewsCountTextView.setText(views_count);
            recyclerHolder.commentsCountTextView.setText(comments_count);
            recyclerHolder.likesCountTextView.setText(likes_count);

            if (image_normal_url != null){
                finalImageUrl = image_normal_url;
            }else {
                finalImageUrl = image_small_url;
            }

            imageLoader.init(ImageLoaderConfiguration.createDefault(fragmentPage.getContext()));

            imageLoader.displayImage(finalImageUrl, recyclerHolder.pictureImageView, optionsPicture, animateFirstListener);
            imageLoader.displayImage(avatar_url, recyclerHolder.avatarImageView, optionsAvatar, animateFirstListener);

            boolean liked = sharedPreferences.getBoolean(shot_id + "liked",false);
            if (!liked){
                recyclerHolder.likesCountTextView.setTextColor(fragmentPage.getResources().getColor(R.color.black));
                recyclerHolder.likesIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            }else {
                recyclerHolder.likesCountTextView.setTextColor(fragmentPage.getResources().getColor(R.color.colorPrimary));
                recyclerHolder.likesIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            }

            recyclerHolder.viewsIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.commentsIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            //用户点击喜欢图标或按钮，向服务器post一个喜欢请求或取消一个喜欢。
            recyclerHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClicked(shot_id, access_token,recyclerHolder,likes_count,sharedPreferences);
                }
            });
            recyclerHolder.likesIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClicked(shot_id, access_token,recyclerHolder,likes_count,sharedPreferences);
                }
            });
            recyclerHolder.pictureImageView.setOnClickListener(new ClickListener(shot_id,title,comments_count,fragmentPage));

            String timeZ = created_at.replaceAll("T","  ");
            String time = timeZ.replaceAll("Z","");

            recyclerHolder.createdTime.setText(time);

        }else {
            Log.e(TAG,"cursor为空");
        }
        cursor.close();
        db.close();

    }

    private class ClickListener implements View.OnClickListener{

        String shot_id;
        String title;
        String comments_count;
        FragmentPage fragmentPage;

        ClickListener(String shot_id,String title,String comments_count,FragmentPage fragmentPage){
            this.shot_id = shot_id;
            this.title = title;
            this.comments_count = comments_count;
            this.fragmentPage = fragmentPage;
        }

        @Override
        public void onClick(View v) {
            v.setTransitionName("shot_picture");
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(fragmentPage.getActivity(),v,v.getTransitionName());

            Intent intent = new Intent(fragmentPage.getActivity(),ShotDetailActivity.class);
            intent.putExtra("shot_id",shot_id);
            intent.putExtra("title",title);
            intent.putExtra("comments_count",comments_count);
            fragmentPage.getActivity().startActivity(intent,activityOptionsCompat.toBundle());
        }
    }

    @Override
    public int getItemCount() {
        myDatabaseHelper = new MyDatabaseHelper(fragmentPage.getContext(),"shots.db",null,6);
        db = myDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.query("shots",new String[]{"id"},"drawer_position=? and page_position=?",new String[]{drawerPosition,page + ""},null,null,null);
        int i = 0;
        if (cursor.moveToFirst()){
            do {
                i++;
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (i >= 12){
            return 12;
        }else {
            return i-1;
        }
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

    public void likeClicked(String shot_id,String access_token,RecyclerHolder recyclerHolder,String likesCount,SharedPreferences sharedPreferences){
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
            recyclerHolder.likesIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(fragmentPage.getResources().getColor(R.color.colorPrimary));
            int likes = Integer.parseInt(likesCount) + 1;
            recyclerHolder.likesCountTextView.setText(Integer.toString(likes));
            Toast.makeText(fragmentPage.getActivity(), "已喜欢", Toast.LENGTH_SHORT).show();
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
            recyclerHolder.likesIconImageView.setColorFilter(fragmentPage.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(fragmentPage.getResources().getColor(R.color.black));
            recyclerHolder.likesCountTextView.setText(Integer.toString(Integer.parseInt(likesCount)));
            Toast.makeText(fragmentPage.getActivity(), "已取消喜欢", Toast.LENGTH_SHORT).show();
        }
    }
}
