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
 private void scanBarcode(WebView view){
 com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(this).startScan().addOnSuccessListener(code->{String raw=code.getRawValue();if(raw!=null&&raw.matches("[0-9]{8,14}"))view.evaluateJavascript("scannedBarcode("+org.json.JSONObject.quote(raw)+")",null);else android.widget.Toast.makeText(this,"Kein unterstützter Produktbarcode.",0).show();}).addOnFailureListener(e->android.widget.Toast.makeText(this,"Scanner nicht verfügbar. Barcode bitte manuell eingeben.",1).show());
 }
 private boolean searching=false;
 private void searchFood(WebView view,String q){
 if(searching||q==null||q.length()<3||q.length()>100)return;
 searching=true;
 new Thread(()->{
 org.json.JSONArray result=new org.json.JSONArray();
 java.net.HttpURLConnection c=null;
 try{
 String fields="product_name,brands,nutriments";
 boolean barcode=q.matches("[0-9]{8,14}");
 String url=barcode?"https://world.openfoodfacts.org/api/v2/product/"+q+".json?fields="+fields:"https://de.openfoodfacts.org/cgi/search.pl?search_terms="+java.net.URLEncoder.encode(q,"UTF-8")+"&search_simple=1&action=process&json=1&page_size=15&fields="+fields;
 c=(java.net.HttpURLConnection)new java.net.URL(url).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(15000);c.setRequestProperty("User-Agent","Mein-Abnehm-Coach/2.8 (Android; Leobeck)");
 java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=c.getInputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(bytes.size()+n>2000000)throw new java.io.IOException("Response too large");bytes.write(buf,0,n);}}
 org.json.JSONObject root=new org.json.JSONObject(bytes.toString("UTF-8"));org.json.JSONArray products=barcode?new org.json.JSONArray().put(root.optJSONObject("product")):root.optJSONArray("products");
 if(products!=null)for(int i=0;i<products.length();i++){org.json.JSONObject x=products.optJSONObject(i);if(x==null)continue;org.json.JSONObject v=x.optJSONObject("nutriments");if(v==null)continue;String name=x.optString("product_name","").trim();double k=v.optDouble("energy-kcal_100g",Double.NaN),p=v.optDouble("proteins_100g",Double.NaN);if(name.isEmpty()||!Double.isFinite(k)||!Double.isFinite(p)||k<0||k>900||p<0||p>100)continue;String brand=x.optString("brands","");if(!brand.isEmpty())name+=" ("+brand+")";result.put(new org.json.JSONArray().put(name+" [Open Food Facts]").put(k).put(p));}
 }catch(Exception ignored){}finally{if(c!=null)c.disconnect();}
 runOnUiThread(()->{searching=false;view.evaluateJavascript("onlineResults("+result.toString().replace("\u2028","\\u2028").replace("\u2029","\\u2029")+")",null);});
 }).start();
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
    if("coach".equals(req.getUrl().getScheme())) { if("close".equals(req.getUrl().getHost())) finish(); if("reminders".equals(req.getUrl().getHost())) reminderDialog(); if("scan".equals(req.getUrl().getHost()) && req.isForMainFrame()) scanBarcode(view); if("foodsearch".equals(req.getUrl().getHost()) && req.isForMainFrame()) searchFood(view,req.getUrl().getQueryParameter("q")); if("chatgpt".equals(req.getUrl().getHost()) && req.isForMainFrame()) openCoach(req.getUrl().getQueryParameter("report")); }
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
