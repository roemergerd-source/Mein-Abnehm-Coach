package de.leobeck.abnehmcoach;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b); WebView w=new WebView(this); w.setFitsSystemWindows(true); w.getSettings().setJavaScriptEnabled(true); w.getSettings().setDomStorageEnabled(true); w.getSettings().setAllowFileAccess(false); setContentView(w); w.loadUrl("file:///android_asset/index.html");}
}