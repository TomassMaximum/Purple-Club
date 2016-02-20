package com.project.tom.purpleclub;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Tom on 2016/2/17.
 */
public class ShotCommentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "ShotCommentFragment";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final int SEND_COMMENT_SUCCESS = -2;
    public static final int SEND_COMMENT_FAILED = -3;

    MyRecyclerView recyclerView;
    CommentRecyclerViewAdapter commentRecyclerViewAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    MyHandler myHandler;
    SendCommentHandler sendCommentHandler;

    ImageView comment_icon_image_view;
    ImageView send_comment_icon_image_view;
    EditText my_comment_edit_text;

    String access_token;
    String myComment;
    String commentsCount;
    static String shot_id;
    SharedPreferences sharedPreferences;
    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase db;
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    OkHttpClient postClient = new OkHttpClient();

    public static ShotCommentFragment newInstance(String shotID){
        shot_id = shotID;
        return new ShotCommentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shot_comments,container,false);
        sharedPreferences = getActivity().getSharedPreferences("NerdPool", Context.MODE_PRIVATE);
        access_token = sharedPreferences.getString("access_token", "");
        commentsCount = sharedPreferences.getString("shot_comments_count", "0");

        comment_icon_image_view = (ImageView) rootView.findViewById(R.id.comment_icon);
        send_comment_icon_image_view = (ImageView) rootView.findViewById(R.id.send_comment_icon);
        my_comment_edit_text = (EditText) rootView.findViewById(R.id.my_comment_body);

        sendCommentHandler = new SendCommentHandler(this);

        send_comment_icon_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myComment = my_comment_edit_text.getText().toString();
                String escapedHTML = Html.escapeHtml(myComment);

                View view = getActivity().getCurrentFocus();
                if (view != null){
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
                my_comment_edit_text.setText("");

                final String comment_body = "{\"body\":\"" + escapedHTML + "\"}";
                final String postCommentURL = Contract.BASE_URL + shot_id + Contract.COMMENTS + Contract.ACCESS_TOKEN + access_token;

                myDatabaseHelper = new MyDatabaseHelper(getActivity(),"comments",null,5);
                db = myDatabaseHelper.getWritableDatabase();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String JSONCommentResponse = postJSONToAPI(postCommentURL, comment_body);
                            JSONObject commentObject = new JSONObject(JSONCommentResponse);

                            String comment_id = commentObject.getString("id");
                            String body = commentObject.getString("body");
                            String created_at = commentObject.getString("created_at");
                            String likes_count = commentObject.getString("likes_count");

                            JSONObject userObject = commentObject.getJSONObject("user");
                            String username = userObject.getString("username");
                            String avatar_url = userObject.getString("avatar_url");
                            String user_id = userObject.getString("id");

                            ContentValues contentValues = new ContentValues();
                            contentValues.put("comment_id", comment_id);
                            contentValues.put("body", body);
                            contentValues.put("created_at", created_at);
                            contentValues.put("username", username);
                            contentValues.put("avatar_url", avatar_url);
                            contentValues.put("likes_count", likes_count);
                            contentValues.put("user_id", user_id);

                            db.insert("comments", null, contentValues);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            int i = Integer.parseInt(commentsCount) + 1;
                            editor.putString("comment_id" + i, comment_id);
                            editor.apply();
                            db.close();
                            Log.e(TAG, "评论添加完毕");

                            Message message = new Message();
                            message.what = SEND_COMMENT_SUCCESS;
                            sendCommentHandler.sendMessage(message);

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Message message = new Message();
                            message.what = SEND_COMMENT_FAILED;
                            sendCommentHandler.sendMessage(message);
                        }
                    }
                }).start();

                onRefresh();
            }
        });

        comment_icon_image_view.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        send_comment_icon_image_view.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        refreshAutomatically(rootView, swipeRefreshLayout);

        Log.e(TAG, commentsCount);
        recyclerView = (MyRecyclerView) rootView.findViewById(R.id.comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    private void refreshAutomatically(final View rootView,final SwipeRefreshLayout swipeRefreshLayout){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onRefresh() {
        Log.e(TAG, "onRefresh执行");
        myHandler = new MyHandler(this);
        String access_token = sharedPreferences.getString("access_token","");
        commentsCount = sharedPreferences.getString("shot_comments_count", "");
        myDatabaseHelper = new MyDatabaseHelper(getActivity(),"comments",null,5);
        db = myDatabaseHelper.getWritableDatabase();
        final String stringUrl = Contract.BASE_URL + shot_id + Contract.COMMENTS + Contract.ACCESS_TOKEN + access_token;
        Log.e(TAG,stringUrl);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String responseComments = getJSONFromAPI(stringUrl);
                    JSONArray commentsArray = new JSONArray(responseComments);

                    for (int i = 0;i < commentsArray.length();i++){
                        JSONObject commentObject = commentsArray.getJSONObject(i);

                        String comment_id = commentObject.getString("id");
                        String body = commentObject.getString("body");
                        String created_at = commentObject.getString("created_at");
                        String likes_count = commentObject.getString("likes_count");

                        JSONObject userObject = commentObject.getJSONObject("user");
                        String username = userObject.getString("username");
                        String avatar_url = userObject.getString("avatar_url");
                        String user_id = userObject.getString("id");

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("comment_id",comment_id);
                        contentValues.put("body",body);
                        contentValues.put("created_at",created_at);
                        contentValues.put("username",username);
                        contentValues.put("avatar_url",avatar_url);
                        contentValues.put("likes_count", likes_count);
                        contentValues.put("user_id", user_id);

                        db.insert("comments", null, contentValues);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("comment_id" + i,comment_id);
                        editor.apply();

                        Log.e(TAG,i + "处理完毕");
                    }
                    db.close();

                    Message message = new Message();
                    message.what = commentsArray.length();
                    myHandler.sendMessage(message);

                } catch (IOException | JSONException e) {
                    Message message = new Message();
                    message.what = -1;
                    myHandler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    String getJSONFromAPI(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String postJSONToAPI(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = postClient.newCall(request).execute();
        return response.body().string();
    }

    private class SendCommentHandler extends Handler{

        ShotCommentFragment shotCommentFragment;

        public SendCommentHandler(ShotCommentFragment shotCommentFragment){
            this.shotCommentFragment = shotCommentFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            if (result == SEND_COMMENT_SUCCESS){
                Toast.makeText(shotCommentFragment.getActivity(), "评论发送成功", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(shotCommentFragment.getActivity(), "抱歉，您没有评论权限", Toast.LENGTH_SHORT).show();
                my_comment_edit_text.setText(myComment);
            }
        }
    }

    private class MyHandler extends Handler {

        ShotCommentFragment shotCommentFragment;

        public MyHandler(ShotCommentFragment shotCommentFragment){
            this.shotCommentFragment = shotCommentFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            int comments_count = msg.what;
            if (comments_count != -1) {
                commentRecyclerViewAdapter = new CommentRecyclerViewAdapter(shotCommentFragment, shot_id, comments_count);
                recyclerView.setLayoutManager(new LinearLayoutManager(shotCommentFragment.getActivity()));
                SlideInRightAnimationAdapter slideInRightAnimationAdapter = new SlideInRightAnimationAdapter(commentRecyclerViewAdapter);
                slideInRightAnimationAdapter.setInterpolator(new LinearOutSlowInInterpolator());
                recyclerView.setAdapter(slideInRightAnimationAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }else {
                if (getActivity() != null){
                    Toast.makeText(getActivity(), "网络请求出现问题，请重试。", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

        }
    }
}

class CommentRecyclerViewAdapter extends RecyclerView.Adapter {

    private final String TAG = "CommentRecyclerAdapter";
    String shot_id;
    int comments_count;
    String comment_id;
    String username;
    String body;
    String likes_count;
    String avatar_url;
    String created_at;
    String user_id;
    String access_token;

    ShotCommentFragment shotCommentFragment;
    SharedPreferences sharedPreferences;
    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase db;
    Cursor cursor;
    Context context;

    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();

    public CommentRecyclerViewAdapter(ShotCommentFragment shotCommentFragment, String shot_id, int comments_count) {
        this.shotCommentFragment = shotCommentFragment;
        this.shot_id = shot_id;
        this.comments_count = comments_count;

        context = shotCommentFragment.getContext();
        if (context != null){
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .displayer(new CircleBitmapDisplayer(Color.WHITE,5))
                .build();
    }

    class CommentRecyclerViewHolder extends RecyclerView.ViewHolder {

        private View view;

        ImageView commenterAvatarImageView;
        ImageView likesIconImageView;

        TextView commentTimeTextView;
        TextView commenterUserNameTextView;
        TextView likesCountTextView;
        TextView commentBodyTextView;
        CardView cardView;

        public CommentRecyclerViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            commenterAvatarImageView = (ImageView) view.findViewById(R.id.commenter_avatar);
            likesIconImageView = (ImageView) view.findViewById(R.id.likes_icon);
            commentTimeTextView = (TextView) view.findViewById(R.id.comment_time);
            commenterUserNameTextView = (TextView) view.findViewById(R.id.commenter_user_name);
            likesCountTextView = (TextView) view.findViewById(R.id.comment_likes_count);
            commentBodyTextView = (TextView) view.findViewById(R.id.comment_body);
            cardView = (CardView) view.findViewById(R.id.shot_comments_card_view);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shots_comments_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final CommentRecyclerViewHolder recyclerHolder = (CommentRecyclerViewHolder) holder;

        sharedPreferences = shotCommentFragment.getActivity().getSharedPreferences("NerdPool",Context.MODE_PRIVATE);
        comment_id = sharedPreferences.getString("comment_id" + position, "");
        access_token = sharedPreferences.getString("access_token","");

        myDatabaseHelper = new MyDatabaseHelper(shotCommentFragment.getActivity(),"comments",null,5);
        db = myDatabaseHelper.getWritableDatabase();

        String[] projections = {"created_at","username","body","likes_count","user_id","avatar_url"};

        cursor = db.query("comments",projections,"comment_id=?",new String[]{comment_id},null,null,null);
        if (cursor.moveToFirst()){
            username = cursor.getString(cursor.getColumnIndex("username"));
            created_at = cursor.getString(cursor.getColumnIndex("created_at"));
            body = cursor.getString(cursor.getColumnIndex("body"));
            likes_count = cursor.getString(cursor.getColumnIndex("likes_count"));
            user_id = cursor.getString(cursor.getColumnIndex("user_id"));
            avatar_url = cursor.getString(cursor.getColumnIndex("avatar_url"));
        }
        cursor.close();
        db.close();

        String[] parts = created_at.split("T");
        String dayPart = parts[0];
        String timePart = parts[1];
        String[] dayParts = dayPart.split("-");
        String day = dayParts[1] + "-" + dayParts[2];
        String timeClean = timePart.replaceAll("Z", "");
        String[] timeParts = timeClean.split(":");
        String time = timeParts[0] + ":" + timeParts[1];
        String finalTime = day + "  " + time;

        String formattedBody = Html.fromHtml(body).toString();

        recyclerHolder.commentTimeTextView.setText(finalTime);
        recyclerHolder.commenterUserNameTextView.setText(username);
        recyclerHolder.commentBodyTextView.setText(formattedBody);
        recyclerHolder.likesCountTextView.setText(likes_count);

        imageLoader.displayImage(avatar_url,recyclerHolder.commenterAvatarImageView,options,animateFirstListener);
        boolean liked = sharedPreferences.getBoolean(comment_id + "liked", false);
        if (!liked) {
            recyclerHolder.likesCountTextView.setTextColor(shotCommentFragment.getResources().getColor(R.color.black));
            recyclerHolder.likesIconImageView.setColorFilter(shotCommentFragment.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        } else {
            recyclerHolder.likesCountTextView.setTextColor(shotCommentFragment.getResources().getColor(R.color.colorPrimary));
            recyclerHolder.likesIconImageView.setColorFilter(shotCommentFragment.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }

        //用户点击喜欢图标或按钮，向服务器post一个喜欢请求或取消一个喜欢。
        recyclerHolder.likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeClicked(comment_id, shot_id, access_token, likes_count, sharedPreferences, recyclerHolder);
            }
        });
        recyclerHolder.likesIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeClicked(comment_id, shot_id, access_token, likes_count, sharedPreferences, recyclerHolder);
            }
        });
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

    @Override
    public int getItemCount() {
        return comments_count;
    }

    public void likeClicked(String comment_id,String shot_id,String access_token,String likesCount,SharedPreferences sharedPreferences,CommentRecyclerViewHolder recyclerHolder){
        Log.e(TAG, "被点击了");
        boolean liked = sharedPreferences.getBoolean(comment_id + "liked", false);
        if (!liked){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(comment_id + "liked",true);
            editor.apply();
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
            recyclerHolder.likesIconImageView.setColorFilter(shotCommentFragment.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(shotCommentFragment.getResources().getColor(R.color.colorPrimary));
            int likes = Integer.parseInt(likesCount) + 1;
            recyclerHolder.likesCountTextView.setText(Integer.toString(likes));
            Toast.makeText(shotCommentFragment.getActivity(), "已喜欢", Toast.LENGTH_SHORT).show();
        }else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(comment_id + "liked",false);
            editor.apply();
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
            recyclerHolder.likesIconImageView.setColorFilter(shotCommentFragment.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            recyclerHolder.likesCountTextView.setTextColor(shotCommentFragment.getResources().getColor(R.color.black));
            recyclerHolder.likesCountTextView.setText(Integer.toString(Integer.parseInt(likesCount)));
            Toast.makeText(shotCommentFragment.getActivity(), "已取消喜欢", Toast.LENGTH_SHORT).show();
        }
    }
}
