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
public class LoginService extends Service {

    public static final String TAG = "LoginService";
    String returnedResponse;
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
        final String access_token = intent.getStringExtra("access_token");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Contract.DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN + "?" + Contract.ACCESS_TOKEN + access_token;
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
                            String html_url = jsonObject.getString("html_url");
                            String avatar_url = jsonObject.getString("avatar_url");
                            String username = jsonObject.getString("username");

                            //存入当前头像URL地址,用户名,个人主页地址
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //将用户已登录信息存入SharedPreferences
                            editor.putBoolean("SignedIn", true);

                            //将新的url保存至SharedPreference
                            editor.putString("access_token",access_token);
                            editor.putString("user_avatar_url", avatar_url);
                            editor.putString("html_url", html_url);
                            editor.putString("username", username);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG,"解析数据出错");
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
}
