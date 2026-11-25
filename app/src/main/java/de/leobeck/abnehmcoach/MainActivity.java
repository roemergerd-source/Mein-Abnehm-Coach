package de.leobeck.abnehmcoach;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.webkit.WebView;
public class MainActivity extends Activity {
 private void openCoach(String report){
 if(report==null || report.length()>20000)return;
 android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
 android.content.ClipData clip=android.content.ClipData.newPlainText("Abnehm-Coach Wochenbericht",report);
 if(android.os.Build.VERSION.SDK_INT>=33){android.os.PersistableBundle extras=new android.os.PersistableBundle();extras.putBoolean("android.content.extra.IS_SENSITIVE",true);clip.getDescription().setExtras(extras);}
 clipboard.setPrimaryClip(clip);
 android.widget.Toast.makeText(this,"Bericht kopiert. In ChatGPT einfügen und prüfen.",android.widget.Toast.LENGTH_LONG).show();
 android.content.Intent app=new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse("https://chatgpt.com/"));app.setPackage("com.openai.chatgpt");
 try{startActivity(app);}catch(android.content.ActivityNotFoundException e){try{startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse("https://chatgpt.com/")));}catch(android.content.ActivityNotFoundException ignored){}}
 }
 private void reminderDialog(){
 android.content.SharedPreferences prefs=getSharedPreferences("reminders",0);
 android.widget.TimePicker picker=new android.widget.TimePicker(this);
 picker.setIs24HourView(true);picker.setHour(prefs.getInt("hour",8));picker.setMinute(prefs.getInt("minute",0));
 new android.app.AlertDialog.Builder(this).setTitle("Tägliche Erinnerung – "+(prefs.getBoolean("enabled",false)?"aktiv":"aus")).setView(picker).setPositiveButton("Speichern und aktivieren",(d,v)->{
 prefs.edit().putInt("hour",picker.getHour()).putInt("minute",picker.getMinute()).apply();
 if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},42);
 else ReminderReceiver.enable(this,true);
 }).setNegativeButton("Deaktivieren",(d,v)->ReminderReceiver.enable(this,false)).setNeutralButton("Abbrechen",null).show();
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==42 && g.length>0 && g[0]==0) ReminderReceiver.enable(this,true);}
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
    if("coach".equals(req.getUrl().getScheme())) { if("close".equals(req.getUrl().getHost())) finish(); if("reminders".equals(req.getUrl().getHost())) reminderDialog(); if("chatgpt".equals(req.getUrl().getHost()) && req.isForMainFrame()) openCoach(req.getUrl().getQueryParameter("report")); }
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
