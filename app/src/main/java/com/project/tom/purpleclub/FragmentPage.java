package com.project.tom.purpleclub;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Tom on 2016/1/25.
 */
public class FragmentPage extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public final String TAG = "FragmentPage";

    public final String POPULARITY_SHOTS = "shots_popularity";
    public final String RECENT_SHOTS = "shots_recent";
    public final String VIEWS_SHOTS = "shots_views";
    public final String COMMENTS_SHOTS = "shots_comments";
    public String tableName;

    public static int page;
    public static int fragmentPosition;

    protected RecyclerView recyclerView;
    protected RecyclerViewAdapter myAdapter;
    protected RecyclerView.LayoutManager layoutManager;
    SharedPreferences sharedPreferences;
    MyDatabaseHelper myDatabaseHelper;
    MyHandler myHandler;
    SwipeRefreshLayout swipeRefreshLayout;
    String shotsUrl;
    SQLiteDatabase db;

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build();

    public FragmentPage(){}

    public static FragmentPage newInstance(int position,String drawerPosition){
        fragmentPosition = position;

        FragmentPage fragmentPage = new FragmentPage();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("drawerPosition",drawerPosition);
        fragmentPage.setArguments(args);
        return fragmentPage;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_page,container,false);

        getActivity().getWindow().setSharedElementExitTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.shared_element_transition));

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);

        String drawerPosition = getArguments().getString("drawerPosition");
        int position = getArguments().getInt("position");

        myDatabaseHelper = new MyDatabaseHelper(getActivity(),"shots.db",null,6);
        db = myDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.query("shots", new String[]{"id"}, "drawer_position=? and page_position=?", new String[]{drawerPosition,Integer.toString(position)},null,null,null);
        if (cursor.moveToFirst()){
            Log.e(TAG, "位置为：" + position);
            myAdapter = new RecyclerViewAdapter(this,position,drawerPosition);
            SlideInRightAnimationAdapter slideInRightAnimationAdapter = new SlideInRightAnimationAdapter(myAdapter);
            slideInRightAnimationAdapter.setInterpolator(new LinearOutSlowInInterpolator());
            recyclerView.setAdapter(slideInRightAnimationAdapter);
        }else {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                }
            });
        }
        db.close();
        myHandler = new MyHandler(this);

        return rootView;
    }

    @Override
    public void onRefresh() {
        //检测到用户下拉刷新动作后，开启一条线程使用HttpURLConnection抓取shots数据并将图片文件存入本地存储
        sharedPreferences = getActivity().getSharedPreferences("NerdPool", getActivity().MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token","");

        page = getArguments().getInt("position");
        final String drawerPosition = getArguments().getString("drawerPosition");

        if (drawerPosition != null){
            switch (drawerPosition){
                case "top":
                    //判断页面的角标，生成相应的URL进行请求。
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                case "new_show":
                    //判断页面的角标，生成相应的URL进行请求。
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_NEW_SHOW;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_NEW_SHOW + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_NEW_SHOW + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_NEW_SHOW + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                case "gif_animation":
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_GIF_ANIMATED;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_GIF_ANIMATED + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_GIF_ANIMATED + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_GIF_ANIMATED + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                case "season_winner":
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SEASON;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SEASON + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SEASON + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SEASON + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                case "team_work":
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_TEAMS;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_TEAMS + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_TEAMS + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_TEAMS + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                case "second_production":
                    switch(page){
                        case 0:
                            //当前碎片为Popularity最受关注时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SECOND_PRODUCTION;
                            break;
                        case 1:
                            //当前碎片为Recent最新发布十：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SECOND_PRODUCTION + Contract.SORT_RECENT;
                            break;
                        case 2:
                            //当前碎片为Views最受欣赏时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SECOND_PRODUCTION + Contract.SORT_VIEWS;
                            break;
                        case 3:
                            //当前碎片为comments最受议论时：
                            shotsUrl = Contract.DRIBBBLE_GET_SHOTS + Contract.ACCESS_TOKEN + access_token + Contract.LIST_SECOND_PRODUCTION + Contract.SORT_COMMENTS;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        Log.e(TAG, shotsUrl);
        myDatabaseHelper = new MyDatabaseHelper(getActivity(),"shots.db",null,6);
        db = myDatabaseHelper.getWritableDatabase();
        db.delete("shots","drawer_position=? and page_position=?",new String[]{drawerPosition,page + ""});

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = getJSONFromAPI(shotsUrl);
                    Log.e(TAG, response);
                    JSONArray shotsArray = new JSONArray(response);

                    for (int i = 0;i < shotsArray.length();i++){

                        String description = "";
                        String image_big_url = "";
                        String image_normal_url = "";
                        String image_small_url = "";
                        String bio = "";
                        String location = "";
                        String web = "";
                        String twitter = "";
                        String teams_count = "";


                        JSONObject shotObject = shotsArray.getJSONObject(i);
                        String shot_id = shotObject.getString("id");
                        String title = shotObject.getString("title");
                        if (shotObject.has("description")){
                            description = shotObject.getString("description");
                        }
                        String width = shotObject.getString("width");
                        String height = shotObject.getString("height");

                        JSONObject imageObject = shotObject.getJSONObject("images");
                        if(imageObject.has("hidpi")){
                            image_big_url = imageObject.getString("hidpi");
                        }
                        if (imageObject.has("normal")){
                            image_normal_url = imageObject.getString("normal");
                        }
                        if (imageObject.has("teaser")){
                            image_small_url = imageObject.getString("teaser");
                        }
                        String views_count = shotObject.getString("views_count");
                        String likes_count = shotObject.getString("likes_count");
                        String comments_count = shotObject.getString("comments_count");
                        String created_at = shotObject.getString("created_at");
                        String animated = shotObject.getString("animated");

                        JSONObject userObject = shotObject.getJSONObject("user");
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

                        //创建ContentValues组装解析出来的数据
                        ContentValues values = new ContentValues();
                        values.put("drawer_position",drawerPosition);
                        values.put("page_position", page);
                        values.put("shot_id",shot_id);
                        values.put("title",title);
                        values.put("description",description);
                        values.put("width",width);
                        values.put("height",height);
                        values.put("image_big_url",image_big_url);
                        values.put("image_normal_url",image_normal_url);
                        values.put("image_small_url",image_small_url);
                        values.put("views_count",views_count);
                        values.put("likes_count",likes_count);
                        values.put("comments_count",comments_count);
                        values.put("created_at",created_at);
                        values.put("animated",animated);

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
                        Log.e(TAG, "第" + i + "组数据添加完毕" + drawerPosition + page);

                        db.insert("shots",null,values);

                    }
                    db.close();

                    Message message = new Message();
                    message.what = 1;
                    myHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "获取数据出错");
                    Message message = new Message();
                    message.what = -1;
                    myHandler.sendMessage(message);
                } catch (JSONException e) {
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

    private class MyHandler extends Handler{
        FragmentPage fragmentPage;
        public MyHandler(FragmentPage fragmentPage){
            this.fragmentPage = fragmentPage;
        }
        int success;
        String drawerPosition;

        @Override
        public void handleMessage(Message msg) {
            page = getArguments().getInt("position");
            drawerPosition = getArguments().getString("drawerPosition");

            success = msg.what;
            if (success != -1){
                myAdapter = new RecyclerViewAdapter(fragmentPage,page,drawerPosition);
                SlideInRightAnimationAdapter slideInRightAnimationAdapter = new SlideInRightAnimationAdapter(myAdapter);
                slideInRightAnimationAdapter.setInterpolator(new LinearOutSlowInInterpolator());
                recyclerView.setAdapter(slideInRightAnimationAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }else {
                Toast.makeText(fragmentPage.getActivity(), "网络请求出现问题，请重试。", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }


        }
    }
}

