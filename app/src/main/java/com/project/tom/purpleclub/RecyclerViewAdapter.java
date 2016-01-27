package com.project.tom.purpleclub;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Tom on 2016/1/25.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    public final String TAG = "RecyclerViewAdapter";

    ImageView userImage;
    TextView userId;
    TextView userClub;
    TextView dataTitle;
    TextView dataContent;
    TextView dataDate;
    TextView thumbUpsNumber;
    TextView commentsNumber;

    private int viewNumber = 10;

    //暂时引用sample代码
    private String[] mDataSet;

    class RecyclerHolder extends RecyclerView.ViewHolder{

        private View view;
        private ImageView userImage;
        private TextView userId,userClub,dataTitle,dataContent,dataDate,thumbUpsNumber,commentsNumber;

        public RecyclerHolder(View itemView) {
            super(itemView);
            view = itemView;

            userImage = (ImageView) itemView.findViewById(R.id.user_image);
            userId = (TextView) itemView.findViewById(R.id.user_id);
            userClub = (TextView) itemView.findViewById(R.id.user_club);

            dataTitle = (TextView) itemView.findViewById(R.id.data_title);
            dataContent = (TextView) itemView.findViewById(R.id.data_content);
            dataDate = (TextView) itemView.findViewById(R.id.data_date);
            thumbUpsNumber = (TextView) itemView.findViewById(R.id.thumb_ups);
            commentsNumber = (TextView) itemView.findViewById(R.id.comments_number);
        }

        public ImageView getUserImage() {
            return userImage;
        }

        public TextView getUserId() {
            return userId;
        }

        public TextView getUserClub() {
            return userClub;
        }

        public TextView getDataTitle() {
            return dataTitle;
        }

        public TextView getDataContent() {
            return dataContent;
        }

        public TextView getDataDate() {
            return dataDate;
        }

        public TextView getCommentsNumber() {
            return commentsNumber;
        }

        public TextView getThumbUpsNumber() {
            return thumbUpsNumber;
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerHolder recyclerHolder = (RecyclerHolder) holder;


        //暂时没有bitmap，记得获取ImageView并set

        recyclerHolder.getUserId().setText("我是用户名");
        recyclerHolder.getUserClub().setText("轮滑社");
        recyclerHolder.getDataTitle().setText("轮滑社纳新啦！！啦啦啦啦啦啦");
        recyclerHolder.getDataContent().setText("纳新地点在中心广场，时间是下午三点半，感兴趣的同学来看看哦纳新地点在中心广场，时间是下午三点半，感兴趣的同学来看看哦纳新地点在中心广场，时间是下午三点半，感兴趣的同学来看看哦");
        recyclerHolder.getDataDate().setText("01-25");
        recyclerHolder.getCommentsNumber().setText("88");
        recyclerHolder.getThumbUpsNumber().setText("78555");
    }

    @Override
    public int getItemCount() {
;
        return viewNumber;    }
}
