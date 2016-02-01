package com.project.tom.purpleclub;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Tom on 2016/2/1.
 */
public class RequestService extends Service {

    public static final String TAG = "RequestService";
    String returnedResponse;
    String localAvatarUrl;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String access_token = intent.getStringExtra("access_token");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = GsonData.DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN + "?" + GsonData.ACCESS_TOKEN + access_token;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,response.toString());
                        returnedResponse = response.toString();

                        //解析获取到的Json数据，存入sharedPreference
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(returnedResponse);
                            String name = jsonObject.getString("name");
                            String html_url = jsonObject.getString("html_url");
                            String avatar_url = jsonObject.getString("avatar_url");
                            String id = jsonObject.getString("id");
                            String username = jsonObject.getString("username");
                            String bio = jsonObject.getString("bio");
                            String location = jsonObject.getString("location");
                            String buckets_count = jsonObject.getString("buckets_count");
                            String comments_received_count = jsonObject.getString("comments_received_count");
                            String followers_count = jsonObject.getString("followers_count");
                            String followings_count = jsonObject.getString("followings_count");
                            String likes_count = jsonObject.getString("likes_count");
                            String likes_received_count = jsonObject.getString("likes_received_count");
                            String projects_count = jsonObject.getString("projects_count");
                            String rebounds_received_count = jsonObject.getString("rebounds_received_count");
                            String shots_count = jsonObject.getString("shots_count");
                            String teams_count = jsonObject.getString("teams_count");
                            String can_upload_shot = jsonObject.getString("can_upload_shot");
                            String type = jsonObject.getString("type");
                            String pro = jsonObject.getString("pro");
                            JSONObject linksObject = jsonObject.getJSONObject("links");
                            String web = linksObject.getString("web");
                            String twitter = linksObject.getString("twitter");

                            //获取用户头像，保存至本地。
                            //如果url未变化，则无需再次请求，使用本地头像即可。否则，再次进行请求获取新头像。
                            sharedPreferences = getSharedPreferences("NerdPool",MODE_PRIVATE);
                            String localUrl = sharedPreferences.getString("user_avatar_url","");
                            if (!avatar_url.equals(localUrl)) {
                                //通过网络请求获取到头像
                                new DownloadImageTask().execute(avatar_url);

                                //存入当前头像URL地址,用户名,个人主页地址
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("avatarLocalUrl", localUrl);

                                //将用户已登录信息存入SharedPreferences
                                editor.putBoolean("SignedIn", true);


                                //将新的url保存至SharedPreference
                                editor.putString("user_avatar_url", avatar_url);
                                editor.putString("name", name);
                                editor.putString("html_url", html_url);
                                editor.putString("id", id);
                                editor.putString("username", username);
                                editor.putString("bio", bio);
                                editor.putString("location", location);
                                editor.putString("buckets_count", buckets_count);
                                editor.putString("comments_received_count", comments_received_count);
                                editor.putString("followers_count", followers_count);
                                editor.putString("followings_count", followings_count);
                                editor.putString("likes_count", likes_count);
                                editor.putString("likes_received_count", likes_received_count);
                                editor.putString("projects_count", projects_count);
                                editor.putString("rebounds_received_count", rebounds_received_count);
                                editor.putString("shots_count", shots_count);
                                editor.putString("teams_count", teams_count);
                                editor.putString("can_upload_shot", can_upload_shot);
                                editor.putString("type", type);
                                editor.putString("pro", pro);
                                editor.putString("web", web);
                                editor.putString("twitter", twitter);
                                editor.apply();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "请求出错");
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream out;
                File file = new File(path,"user_avatar.jpg");
                out = new FileOutputStream(file);
                mIcon11.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                localAvatarUrl = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("avatarLocalUrl", localAvatarUrl);
                editor.apply();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
    }
}
