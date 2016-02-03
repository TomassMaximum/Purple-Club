package com.project.tom.purpleclub;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.conn.scheme.HostNameResolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tom on 2016/1/25.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    public final String TAG = "RecyclerViewAdapter";
    MyDatabaseHelper myDatabaseHelper;
    NewsFragment newsFragment;
    Cursor cursor;
    SQLiteDatabase db;
    int lastPosition = -1;
    Bitmap image_small;
    RoundImage roundAvatarDrawable;

    RecyclerViewAdapter(NewsFragment newsFragment){
        this.newsFragment = newsFragment;
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

        FrameLayout container;

        public RecyclerHolder(View itemView) {
            super(itemView);
            view = itemView;

            container = (FrameLayout) itemView.findViewById(R.id.fragment_news_data);

            avatarImageView = (ImageView) itemView.findViewById(R.id.publisher_avatar);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            pictureImageView = (ImageView) itemView.findViewById(R.id.picture);
            viewsCountTextView = (TextView) itemView.findViewById(R.id.views_count);
            commentsCountTextView = (TextView) itemView.findViewById(R.id.comments_count);
            likesIconImageView = (ImageView) itemView.findViewById(R.id.likes_icon);
            likesCountTextView = (TextView) itemView.findViewById(R.id.likes_count);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerHolder recyclerHolder = (RecyclerHolder) holder;

        myDatabaseHelper = new MyDatabaseHelper(newsFragment.getContext(),"shots.db",null,2);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"title","avatar_url","image_small_url","views_count","comments_count","likes_count"};
        cursor = db.query("shots",projection,null,null,null,null,null);

        if (cursor.moveToFirst()){
            Log.e(TAG, "第一次：" + position);
            cursor.moveToPosition(position);

            final String avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
            final String title = cursor.getString(cursor.getColumnIndex("title"));
            final String image_small_url = cursor.getString(cursor.getColumnIndex("image_small_url"));
            final String views_count = cursor.getString(cursor.getColumnIndex("views_count"));
            final String comments_count = cursor.getString(cursor.getColumnIndex("comments_count"));
            final String likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));

            try {
                Bitmap avatar = BitmapFactory.decodeStream(newsFragment.getActivity().openFileInput("avatar" + position + ".png"));
                roundAvatarDrawable = new RoundImage(avatar);
                recyclerHolder.avatarImageView.setImageDrawable(roundAvatarDrawable);

                Bitmap image_small = BitmapFactory.decodeStream(newsFragment.getActivity().openFileInput("image_small" + position + ".png"));
                recyclerHolder.pictureImageView.setImageBitmap(image_small);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            recyclerHolder.titleTextView.setText(title);
            recyclerHolder.viewsCountTextView.setText(views_count);
            recyclerHolder.commentsCountTextView.setText(comments_count);
            recyclerHolder.likesCountTextView.setText(likes_count);

            Log.e(TAG, "第二次：" + cursor.getPosition() + title);
        }else {
            Log.e(TAG,"cursor为空");
        }
        cursor.close();
        db.close();
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(newsFragment.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
