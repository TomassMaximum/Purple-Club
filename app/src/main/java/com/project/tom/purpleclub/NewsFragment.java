package com.project.tom.purpleclub;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tom on 2016/1/25.
 */
public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public final String TAG = "NewsFragment";

    protected RecyclerView recyclerView;
    protected RecyclerViewAdapter myAdapter;
    protected RecyclerView.LayoutManager layoutManager;
    SharedPreferences sharedPreferences;
    MyDatabaseHelper myDatabaseHelper;
    MyHandler myHandler;
    SwipeRefreshLayout swipeRefreshLayout;

    TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news,container,false);



        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);

        myHandler = new MyHandler(this);

        myAdapter = new RecyclerViewAdapter(this);

        recyclerView.setAdapter(myAdapter);

        myDatabaseHelper = new MyDatabaseHelper(getActivity(),"shots.db",null,2);
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.query("shots", new String[]{"shot_id"}, null, null, null, null, null);
        if (!(cursor.moveToFirst())){
            swipeRefreshLayout.setRefreshing(true);
        }

        recyclerView.addOnScrollListener(new EndlessOnScrollListener(layoutManager) {
            @Override
            public void onScrolledToEnd() {
                Toast.makeText(getActivity(), "滑动至底部", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onRefresh() {
        //检测到用户下拉刷新动作后，开启一条线程使用HttpURLConnection抓取shots数据并将图片文件存入本地存储
        sharedPreferences = getActivity().getSharedPreferences("NerdPool", getActivity().MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token","");
        final String shotsUrl = GsonData.DRIBBBLE_GET_SHOTS + GsonData.ACCESS_TOKEN + access_token;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    URL url = new URL(shotsUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
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

                    JSONArray shotsArray = new JSONArray(response);
                    Log.e(TAG,"Array大小：" + shotsArray.length());

                    myDatabaseHelper = new MyDatabaseHelper(getActivity(),"shots.db",null,2);
                    SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
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

                        //通过id判断数据库中是否已经有此条记录，有则不再请求，没有则继续请求操作。
                        boolean flag = false;
                        Cursor cursor = db.query("shots",new String[]{"shot_id"},null,null,null,null,null);
                        if (cursor.moveToFirst()){
                            while (cursor.moveToNext()) {
                                String id = cursor.getString(cursor.getColumnIndex("shot_id"));
                                if (id.equals(shot_id)) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        cursor.close();
                        if (flag){
                            continue;
                        }

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

                        //将要添加的新数据添加到数据库
                        db.insert("shots", null, values);

                        //向网络请求用户头像和作品图片
                        InputStream avatarIn;
                        avatarIn = new URL(avatar_url).openStream();
                        Bitmap avatar = BitmapFactory.decodeStream(avatarIn);
                        avatarIn.close();

                        InputStream imageIn;
                        imageIn = new URL(avatar_url).openStream();
                        Bitmap image_small = BitmapFactory.decodeStream(imageIn);
                        imageIn.close();

                        //将请求到的作者头像和作品小图保存到本地
                        FileOutputStream avatarOut = getActivity().openFileOutput("avatar" + i + ".png", getActivity().MODE_PRIVATE);
                        avatar.compress(Bitmap.CompressFormat.PNG,100,avatarOut);
                        avatarOut.close();

                        FileOutputStream imageOut = getActivity().openFileOutput("image_small" + i + ".png",getActivity().MODE_PRIVATE);
                        image_small.compress(Bitmap.CompressFormat.PNG,100,imageOut);
                        imageOut.close();

                        Log.e(TAG, "数据添加完毕" + title);

                    }
                    db.close();

                    Message message = new Message();
                    myHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "请求数据出错，请重试", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private class MyHandler extends Handler{
        NewsFragment newsFragment;
        public MyHandler(NewsFragment newsFragment){
            this.newsFragment = newsFragment;
        }

        @Override
        public void handleMessage(Message msg) {

            myAdapter = new RecyclerViewAdapter(newsFragment);

            recyclerView.setAdapter(myAdapter);

            swipeRefreshLayout.setRefreshing(false);

        }
    }
}

