package com.project.tom.purpleclub;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Tom on 2016/1/25.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    public final String TAG = "RecyclerViewAdapter";

    private int viewNumber = 10;

    //暂时引用sample代码
    private String[] mDataSet;

    class RecyclerHolder extends RecyclerView.ViewHolder{

        private View view;

        public RecyclerHolder(View itemView) {
            super(itemView);
            view = itemView;

        }

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerHolder recyclerHolder = (RecyclerHolder) holder;

//        Bitmap view_icon = Utils.getResizedBitmap(BitmapFactory.decodeResource(newsFragment.getResources(),R.drawable.view_icon),80,80);
//        Bitmap comments_icon = Utils.getResizedBitmap(BitmapFactory.decodeResource(newsFragment.getResources(),R.drawable.comments_icon),72,72);
//        Bitmap likes_icon = Utils.getResizedBitmap(BitmapFactory.decodeResource(newsFragment.getResources(),R.drawable.likes_icon),72,72);
//        recyclerHolder.getViewIcon().setImageBitmap(view_icon);
//        recyclerHolder.getCommentsIcon().setImageBitmap(comments_icon);
//        recyclerHolder.getLikesIcon().setImageBitmap(likes_icon);
//        //暂时没有bitmap，记得获取ImageView并set
//        recyclerHolder.getTextView().setText("Test");
//
//        savePic(view_icon,"view_icon.jpg");
//        savePic(comments_icon,"comments_icon.jpg");
//        savePic(likes_icon,"likes_icon.jpg");
    }

    @Override
    public int getItemCount() {
        return viewNumber;
    }
}
