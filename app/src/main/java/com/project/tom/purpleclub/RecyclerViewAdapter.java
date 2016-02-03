package com.project.tom.purpleclub;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;

/**
 * Created by Tom on 2016/1/25.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    public final String TAG = "RecyclerViewAdapter";
    MyDatabaseHelper myDatabaseHelper;
    FragmentPage fragmentPage;
    Cursor cursor;
    SQLiteDatabase db;
    int lastPosition = -1;
    Bitmap image_small;
    RoundImage roundAvatarDrawable;

    RecyclerViewAdapter(FragmentPage fragmentPage){
        this.fragmentPage = fragmentPage;
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
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final RecyclerHolder recyclerHolder = (RecyclerHolder) holder;

        myDatabaseHelper = new MyDatabaseHelper(fragmentPage.getContext(),"shots.db",null,2);

        db = myDatabaseHelper.getWritableDatabase();
        String[] projection = {"title","avatar_url","image_small_url","views_count","comments_count","likes_count"};
        cursor = db.query("shots",projection,null,null,null,null,null);

        if (cursor.moveToFirst()){
            cursor.moveToPosition(position);

            final String avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
            final String title = cursor.getString(cursor.getColumnIndex("title"));
            final String image_small_url = cursor.getString(cursor.getColumnIndex("image_small_url"));
            final String views_count = cursor.getString(cursor.getColumnIndex("views_count"));
            final String comments_count = cursor.getString(cursor.getColumnIndex("comments_count"));
            final String likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));

            try {
                Bitmap avatar = BitmapFactory.decodeStream(fragmentPage.getActivity().openFileInput("avatar" + position + ".png"));
                roundAvatarDrawable = new RoundImage(avatar);
                recyclerHolder.avatarImageView.setImageDrawable(roundAvatarDrawable);

                Bitmap image_small = BitmapFactory.decodeStream(fragmentPage.getActivity().openFileInput("image_small" + position + ".png"));
                recyclerHolder.pictureImageView.setImageBitmap(image_small);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            recyclerHolder.titleTextView.setText(title);
            recyclerHolder.viewsCountTextView.setText(views_count);
            recyclerHolder.commentsCountTextView.setText(comments_count);
            recyclerHolder.likesCountTextView.setText(likes_count);

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
            Animation animation = AnimationUtils.loadAnimation(fragmentPage.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
