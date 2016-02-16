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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    int page;
    String drawerPosition;

    RecyclerViewAdapter(FragmentPage fragmentPage,int page,String drawerPosition){
        this.fragmentPage = fragmentPage;
        this.page = page;
        this.drawerPosition = drawerPosition;
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

        myDatabaseHelper = new MyDatabaseHelper(fragmentPage.getContext(),"shots.db",null,5);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"id","drawer_position","shot_id","title","avatar_url","image_small_url","views_count","comments_count","likes_count","created_at"};

        switch (drawerPosition){
            case "top":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"top"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"top"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"top"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"top"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"top"},null,null,"id DESC","12");
                        break;
                }
                break;
            case "new_show":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"new_show"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"new_show"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"new_show"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"new_show"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"new_show"},null,null,"id DESC","12");
                        break;
                }
                break;
            case "gif_animation":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"gif_animation"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"gif_animation"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"gif_animation"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"gif_animation"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"gif_animation"},null,null,"id DESC","12");
                        break;
                }
                break;
            case "season_winner":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"season_winner"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"season_winner"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"season_winner"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"season_winner"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"season_winner"},null,null,"id DESC","12");
                        break;
                }
                break;
            case "team_work":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"team_work"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"team_work"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"team_work"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"team_work"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"team_work"},null,null,"id DESC","12");
                        break;
                }
                break;
            case "second_production":
                switch (page){
                    case 0:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"second_production"},null,null,"id DESC","12");
                        break;
                    case 1:
                        cursor = db.query("shots_recent",projection,"drawer_position=?",new String[]{"second_production"},null,null,"id DESC","12");
                        break;
                    case 2:
                        cursor = db.query("shots_views",projection,"drawer_position=?",new String[]{"second_production"},null,null,"id DESC","12");
                        break;
                    case 3:
                        cursor = db.query("shots_comments",projection,"drawer_position=?",new String[]{"second_production"},null,null,"id DESC","12");
                        break;
                    default:
                        cursor = db.query("shots_popularity",projection,"drawer_position=?",new String[]{"second_production"},null,null,"id DESC","12");
                        break;
                }
                break;

        }

        if (cursor.moveToFirst()){
            cursor.moveToPosition(position);

            final String id = cursor.getString(cursor.getColumnIndex("id"));
            Log.e(TAG, id);

            final String shot_id = cursor.getString(cursor.getColumnIndex("shot_id"));
            final String avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
            final String title = cursor.getString(cursor.getColumnIndex("title"));
            final String image_small_url = cursor.getString(cursor.getColumnIndex("image_small_url"));
            final String views_count = cursor.getString(cursor.getColumnIndex("views_count"));
            final String comments_count = cursor.getString(cursor.getColumnIndex("comments_count"));
            final String likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));
            final String created_at = cursor.getString(cursor.getColumnIndex("created_at"));

            try {
                Bitmap avatar = BitmapFactory.decodeStream(fragmentPage.getActivity().openFileInput("avatar" + shot_id + ".png"));
                Drawable roundAvatarDrawable = new RoundImage(avatar);
                recyclerHolder.avatarImageView.setImageDrawable(roundAvatarDrawable);

                Bitmap image_small = BitmapFactory.decodeStream(fragmentPage.getActivity().openFileInput("image_small" + shot_id + ".png"));
                recyclerHolder.pictureImageView.setImageBitmap(image_small);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            final SharedPreferences sharedPreferences = fragmentPage.getActivity().getSharedPreferences("NerdPool", Context.MODE_PRIVATE);
            final String access_token = sharedPreferences.getString("access_token","");

            recyclerHolder.titleTextView.setText(title);
            recyclerHolder.viewsCountTextView.setText(views_count);
            recyclerHolder.commentsCountTextView.setText(comments_count);
            recyclerHolder.likesCountTextView.setText(likes_count);

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
            recyclerHolder.pictureImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(fragmentPage.getActivity(),ShotDetailActivity.class);
                    intent.putExtra("shot_id",shot_id);
                    intent.putExtra("title",title);
                    fragmentPage.startActivity(intent);

                }
            });

            String timeZ = created_at.replaceAll("T","  ");
            String time = timeZ.replaceAll("Z","");

            recyclerHolder.createdTime.setText(time);

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
