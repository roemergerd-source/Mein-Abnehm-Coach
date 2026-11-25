package de.leobeck.abnehmcoach;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b); WebView w=new WebView(this); w.setFitsSystemWindows(true); w.setWebViewClient(new android.webkit.WebViewClient(){ @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest req){if("coach".equals(req.getUrl().getScheme()) && "close".equals(req.getUrl().getHost())){finish();return true;}return true;} }); if(android.os.Build.VERSION.SDK_INT>=30){w.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return android.view.WindowInsets.CONSUMED;});} getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); w.getSettings().setJavaScriptEnabled(true); w.getSettings().setDomStorageEnabled(true); w.getSettings().setAllowFileAccess(false); setContentView(w); w.loadUrl("file:///android_asset/index.html");}
}