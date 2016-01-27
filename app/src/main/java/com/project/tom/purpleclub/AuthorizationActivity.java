package com.project.tom.purpleclub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Tom on 2016/1/27.
 */
public class AuthorizationActivity extends Activity {

    private static final String TAG = "AuthorizationActivity";
    WebView webView;
    TextView textView;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        textView = (TextView) findViewById(R.id.text_view);

        webView = (WebView) findViewById(R.id.webView_authorization);
        webView.getSettings().setJavaScriptEnabled(true);

        //写一个自己的WebViewClient，当加载完成时调用finish关闭当前Activity
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.loadUrl("https://dribbble.com/oauth/authorize?client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&scope=public+write");
    }

    public static class MyWebViewClient extends WebViewClient{

        public String codeResult;
        AuthorizationActivity authorizationActivity;

        public MyWebViewClient(AuthorizationActivity authorizationActivity){
            this.authorizationActivity = authorizationActivity;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("code")){
                Log.e(TAG,"返回code成功" + url);

                //获取到返回url的"code"部分
                String[] urlParts = url.split("[?]");
                codeResult = urlParts[1];

                //调用方法获取到Access Token用于调用dribbble的API
                getAccessToken();

                //获取到Access Token后关闭当前Activity
                authorizationActivity.finish();
            }
        }

        public void getAccessToken(){
            //开启线程向dribbble获取Access Token.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection;
                    try {
                        Log.e(TAG,"子线程开启");
                        URL url = new URL("https://dribbble.com/oauth/token");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
//                        connection.setConnectTimeout(8000);
//                        connection.setReadTimeout(8000);
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.writeBytes("client_id=f6a62b7f35784ebc46ca965c7b7375de8a3172f4887c8ee86e10427e748c27ee&client_secret=7260ba76972c21b693c6960d976f991454930ef19c69eb9e1ed944dee82a1feb&" + codeResult);

                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null){
                            builder.append(line);
                        }

                        //LOG输出检验
                        Log.e(TAG,"成功获取到Access Token:" + builder.toString());

                        //将结果存入Message并传给Handler处理
                        Message message = new Message();
                        message.obj = builder.toString();
                        myHandler.sendMessage(message);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        private MyHandler myHandler = new MyHandler(this);

        static class MyHandler extends Handler{
            WeakReference<MyWebViewClient> mActivity;

            MyHandler(MyWebViewClient myWebViewClient){
                mActivity = new WeakReference<MyWebViewClient>(myWebViewClient);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String result = (String) msg.obj;
                Log.e(TAG, "成功获取到Access Token:" + result);
            }
        }

    }
}
