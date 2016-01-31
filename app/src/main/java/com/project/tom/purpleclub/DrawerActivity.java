package com.project.tom.purpleclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DrawerActivity";
    SharedPreferences preferences;
    static String returnedResponse;
    RoundImage roundImage;

    ImageView userAvatarImageView;
    TextView userName;
    TextView userDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            //当用户点击特定项时，引导用户进行登录。
            @Override
            public void onDrawerOpened(View drawerView) {
                //如果用户未登录，使用Access Token向Dribbble获取用户个人信息
                preferences = getSharedPreferences("NerdPool", MODE_PRIVATE);
                String pref = preferences.getString(AuthorizationActivity.SHARED_PREFERENCE_KEY,"");
                getUserInfo(pref);

                //找到用户头像，用户ID，用户描述三个控件
                userAvatarImageView = (ImageView) findViewById(R.id.user_avatar);
                userName = (TextView) findViewById(R.id.text_not_signed);
                userDescription = (TextView) findViewById(R.id.text_press_to_sign_in);

                //如果用户未登录，显示Dribbble的默认头像，点击引导用户进入登录界面进行授权
                preferences = getSharedPreferences("NerdPool",MODE_PRIVATE);
                Boolean signedIn = preferences.getBoolean("SignedIn",false);
                if (!signedIn){
                    Bitmap userAvatar = Utils.getResizedBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dribbble_default_avatar),180,180);

                    roundImage = new RoundImage(userAvatar);
                    userAvatarImageView.setImageDrawable(roundImage);

                    //userAvatarImageView.setImageBitmap(roundedAvatar);

                    userDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });
                    userAvatarImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });
                    userName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getBaseContext(), AuthorizationActivity.class));
                        }
                    });

                    setUserInfo(returnedResponse);
                }else {
                    String name = preferences.getString("name","");
                    String html = preferences.getString("html","");
                    userName.setText(name);
                    userDescription.setText(html);
                    setLocalAvatar();

                    userAvatarImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DrawerActivity.this, PersonalInfoActivity.class));
                        }
                    });

                    userName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(DrawerActivity.this,PersonalInfoActivity.class));
                        }
                    });
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_new_data);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewsFragment fragment = new NewsFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new_data) {
            // 处理最新资讯选项，呈现fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewsFragment fragment = new NewsFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        } else if (id == R.id.nav_club_show) {

        } else if (id == R.id.nav_my_club) {

        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_message) {

        }else if (id == R.id.nav_settings){

        }else if (id == R.id.nav_version_info){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    //获取圆角矩形头像
//    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
//        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(output);
//
//        final int color = 0xff424242;
//        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        final RectF rectF = new RectF(rect);
//        final float roundPx = 12;
//
//        paint.setAntiAlias(true);
//        canvas.drawARGB(0, 0, 0, 0);
//        paint.setColor(color);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
//
//        return output;
//    }

    public void setUserInfo(String response){
        //获取用户个人信息Json数据成功，开始解析
        if (response != null){
            try {
                JSONObject jsonObject = new JSONObject(response);
                String name = jsonObject.getString("name");
                String html = jsonObject.getString("html_url");
                String avatar_url = jsonObject.getString("avatar_url");

                //获取用户头像，保存至本地。
                //如果url未变化，则无需再次请求，使用本地头像即可。否则，再次进行请求获取新头像。
                preferences = getSharedPreferences("NerdPool",MODE_PRIVATE);
                String localUrl = preferences.getString("user_avatar_url","");
                if (!avatar_url.equals(localUrl)){
                    //通过网络请求获取到头像
                    DownloadImageTask downloadImageTask = new DownloadImageTask();
                    Bitmap userAvatarBitmap = downloadImageTask.execute(avatar_url).get();
                    Bitmap roundedAvatarBitmap = Utils.getResizedBitmap(userAvatarBitmap,180,180);
                    userAvatarImageView.setImageBitmap(roundedAvatarBitmap);

                    //将新的url保存至SharedPreference
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("user_avatar_url",avatar_url);
                    editor.putString("name",name);
                    editor.putString("html",html);
                    editor.apply();

                    new SaveAvatarToLocalTask().execute(roundedAvatarBitmap);
                }else {
                    //如果URL未变，则直接使用本地存储的头像。
                    setLocalAvatar();
                }

                userName.setText(name);
                userDescription.setText(html);
            } catch (JSONException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLocalAvatar(){
        String avatarLocalUrl = preferences.getString("avatarLocalUrl", "");
        Uri avatarUri = Uri.parse(avatarLocalUrl);
        Bitmap localAvatar = null;
        try {
            localAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(),avatarUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        roundImage = new RoundImage(localAvatar);
        userAvatarImageView.setImageDrawable(roundImage);
    }

    public void getUserInfo(String accessToken){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = GsonData.DRIBBBLE_GET_JSON_WITH_ACCESS_TOKEN + "?" + GsonData.ACCESS_TOKEN + accessToken;
        Log.e(TAG,"URL:" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG,response.toString());
                        returnedResponse = response.toString();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,"请求出错");
                        Toast.makeText(DrawerActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private class SaveAvatarToLocalTask extends AsyncTask<Bitmap,Void,Void>{

        @Override
        protected Void doInBackground(Bitmap... params) {
            Bitmap avatarBitmap = params[0];
            SharedPreferences.Editor editor = preferences.edit();

            //将用户当前头像保存至本地
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream out = null;
            File file = new File(path,"user_avatar.jpg");
            try {
                out = new FileOutputStream(file);
                avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                String avatarLocalUrl = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                //存入当前头像URL地址,用户名,个人主页地址
                editor.putString("avatarLocalUrl", avatarLocalUrl);

                //将用户已登录信息存入SharedPreferences
                editor.putBoolean("SignedIn", true);
                editor.apply();
                Log.e(TAG, "Local URL" + avatarLocalUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
    }
}
