package de.leobeck.abnehmcoach;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.webkit.WebView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  FrameLayout root=new FrameLayout(this);
  root.setBackgroundColor(0xffeaf1ed);
  WebView w=new WebView(this);
  root.addView(w,new FrameLayout.LayoutParams(-1,-1));
  if(android.os.Build.VERSION.SDK_INT>=30){
   getWindow().setDecorFitsSystemWindows(false);
   root.setOnApplyWindowInsetsListener((v,insets)->{
    android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
    v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
    return WindowInsets.CONSUMED;
   });
  } else {root.setFitsSystemWindows(true);}
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  w.setWebViewClient(new android.webkit.WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest req){
    if("coach".equals(req.getUrl().getScheme()) && "close".equals(req.getUrl().getHost())) finish();
    return true;
   }
  });
  w.getSettings().setJavaScriptEnabled(true);
  w.getSettings().setDomStorageEnabled(true);
  w.getSettings().setAllowFileAccess(false);
  setContentView(root);
  root.requestApplyInsets();
  w.loadUrl("file:///android_asset/index.html");
 }
}
