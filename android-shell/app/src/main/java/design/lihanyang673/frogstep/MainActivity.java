package design.lihanyang673.frogstep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 蛙步 · WebView 外壳
 * 本质就是一个全屏浏览器窗口，固定打开 GitHub Pages 上的蛙步：
 *   https://lihanyang673-design.github.io/naiwa-step/
 * 网页更新后所有用户立即得到最新版，无需升级 APK。
 */
public class MainActivity extends Activity {

    private static final String START_URL = "https://lihanyang673-design.github.io/naiwa-step/";
    private static final String SITE_HOST = "lihanyang673-design.github.io";

    private WebView web;
    private LinearLayout errorPage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = findViewById(R.id.web);
        errorPage = findViewById(R.id.errorPage);
        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> loadSite());

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                 // localStorage 等
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); // 允许游戏自动播放音乐
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setBuiltInZoomControls(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                // 游戏站内链接留在外壳内；其他网站（原作链接、GitHub 等）交给系统浏览器
                if ("https".equalsIgnoreCase(u.getScheme()) && SITE_HOST.equals(u.getHost())) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showError();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            // 页面里 target="_blank" 的链接（公告中的外链）：转交系统浏览器打开
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.HitTestResult hit = view.getHitTestResult();
                String url = hit != null ? hit.getExtra() : null;
                if (url != null) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception ignored) { }
                }
                return true;
            }
        });

        loadSite();
    }

    private void loadSite() {
        errorPage.setVisibility(View.GONE);
        web.setVisibility(View.VISIBLE);
        web.loadUrl(START_URL);
    }

    private void showError() {
        web.setVisibility(View.GONE);
        errorPage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (web.getVisibility() == View.VISIBLE && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
