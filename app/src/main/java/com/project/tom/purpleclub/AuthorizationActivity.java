package com.project.tom.purpleclub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by Tom on 2016/1/27.
 */
public class AuthorizationActivity extends Activity {

    private static final String TAG = "AuthorizationActivity";
    WebView webView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        webView = (WebView) findViewById(R.id.webView_authorization);

        webView.getSettings().setJavaScriptEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("正在转到登录界面");
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        //写一个自己的WebViewClient，当加载完成时调用finish关闭当前Activity
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.loadUrl(Contract.DRIBBBLE_GET_CODE_PARAM);

        new Thread(){
            public void run()
            {
                try {
                    sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    progressDialog.dismiss();
                }
            }
        }.start();
    }

    public static class MyWebViewClient extends WebViewClient{

        public String codeResult;
        AuthorizationActivity authorizationActivity;
        String tokenJson;

        public MyWebViewClient(AuthorizationActivity authorizationActivity){
            this.authorizationActivity = authorizationActivity;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (authorizationActivity.progressDialog.isShowing()){
                authorizationActivity.progressDialog.dismiss();
            }

            if (url.contains("code")){
                //获取到返回url的"code"部分
                String[] urlParts = url.split("[?]");
                codeResult = urlParts[1];

                //使用Volley库请求Access Token
                RequestQueue requestQueue = Volley.newRequestQueue(authorizationActivity);
                String tokenUrl = Contract.DRIBBBLE_GET_ACCESS_TOKEN + codeResult;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, tokenUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                tokenJson = response.toString();

                                //使用Gson对Json数据进行解析，取出Access Token用于调用API
                                Gson gson = new Gson();
                                Contract contract = gson.fromJson(tokenJson, Contract.class);
                                String accessToken = contract.getAccessToken();

                                //开启一个服务用于在此Activity结束后继续向Dribbble获取用户个人信息
                                Intent intent = new Intent(authorizationActivity,LoginService.class);
                                intent.putExtra("access_token",accessToken);
                                Log.e(TAG,accessToken);
                                authorizationActivity.startService(intent);

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG,"授权失败");
                            }
                        }
                );
                requestQueue.add(jsonObjectRequest);
                //获取到code并传递给Drawer Activity后关闭当前Activity
                authorizationActivity.finish();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Toast.makeText(authorizationActivity, "网页加载失败，再试一次呗", Toast.LENGTH_SHORT).show();
        }
    }
}
