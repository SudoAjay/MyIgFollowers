package com.sudoajay.myigfollowers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sudoajay.myigfollowers.InternetCheck.DetectConnection;
import com.sudoajay.myigfollowers.Toast.CustomToast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<String> saveBackPage = new ArrayList<>();
    private WebView myWebView;
    private boolean doubleBackToExitPressedOnce;
    private SwipeRefreshLayout swipeToRefresh;
    private final String webPage = "https://myigfollowers.com/";

    @SuppressLint({"SetJavaScriptEnabled", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);

        changeStatusBarColor(1);

        saveBackPage.add(webPage);

        // Run Thread For InternetConnection.
        RunThread_Internet();

        myWebView = findViewById(R.id.myWebView);
        swipeToRefresh = findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeToRefresh.setProgressViewOffset(true, 0, 100);
        show();


        myWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                myWebView.loadUrl("file:///android_asset/noInternetConnection.html");
                changeStatusBarColor(2);
            }
        });

        swipeToRefresh.setOnRefreshListener(this);

    }


    @SuppressLint({"SetJavaScriptEnabled", "WrongConstant"})
    private void show() {
        changeStatusBarColor(1);

        myWebView.setPadding(0, 0, 0, 0);
        myWebView.setInitialScale(1);
        myWebView.setScrollBarStyle(33554432);
        myWebView.setScrollbarFadingEnabled(false);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 16) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        myWebView.setWebViewClient(new CustomWebViewClient());
        myWebView.loadUrl(webPage);
    }


    private void changeStatusBarColor(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (type == 2)
                window.setStatusBarColor(Color.parseColor("#1c457b"));
            else {
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (saveBackPage.size() > 1) {
            myWebView.loadUrl(saveBackPage.get(saveBackPage.size() - 2));
            saveBackPage.remove(saveBackPage.size() - 1);
        } else {
            if (doubleBackToExitPressedOnce) {
                Finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            CustomToast.ToastIt(getApplicationContext(), "Click Back Again To Exit");

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void Finish() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);

    }

    private void RunThread_Internet() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!DetectConnection.checkInternetConnection(getApplicationContext())) {
                    // do something...
                    RunThread_Internet();
                } else {
                    myWebView.loadUrl(webPage);
                    changeStatusBarColor(1);
                }
            }
        }, 5000); // 5 sec

    }

    @Override
    public void onRefresh() {

        swipeToRefresh.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!DetectConnection.checkInternetConnection(getApplicationContext())) {
                    changeStatusBarColor(2);
                    myWebView.reload();
                } else {
                    changeStatusBarColor(1);
                    myWebView.loadUrl(webPage);
                }
                swipeToRefresh.setRefreshing(false);
            }
        }, 2000);

    }

    class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return handleUri(uri);
        }

        private boolean handleUri(final Uri uri) {
            final String host = uri.getHost();
            // Based on some condition you need to determine if you are going to load the url
            // in your web view itself or in a browser.
            // You can use `host` or `scheme` or any part of the `uri` to decide.
            assert host != null;
            if (host.equals("myigfollowers.com")) {

                // Returning false means that you are going to load this url in the webView itself
                saveBackPage.add(uri.toString());
                return false;
            } else {
                // Returning true means that you need to handle what to do with the url
                // e.g. open web page in a Browser
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

        }


    }
}
