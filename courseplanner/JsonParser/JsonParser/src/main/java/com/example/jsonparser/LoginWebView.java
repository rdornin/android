package com.example.jsonparser;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginWebView extends Activity {

    public static String tokenUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/token";
    //private String logOutUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/logout";
    public static String courseUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/courses?terse=0&token=";
    String jsondata;
    public static String token = "";
    public static String _cookie;

    private static String MY_DOMAIN = "apis.huit.harvard.edu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web_view);
        //need to do this to get the loadUserProfile to Work
        if (android.os.Build.VERSION.SDK_INT > 7) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        fetchToken();
        Utils.sleep(5000); // buying some time while json loads
        JSONObject jsonResponse;



        final TextView output = (TextView) findViewById(R.id.output);
        //final TextView output = (TextView) findViewById(R.id.output);
       /* Button daClicker = (Button) findViewById(R.id.courses);
        daClicker.setOnClickListener(
                new View.OnClickListener() { @Override public void onClick(View v)
                { startActivity(new Intent(LoginWebView.this, MyCoursesActivity.class));
                    finish();
                    System.out.println("zzz clicking courses");
                } });

        Button catalog = (Button) findViewById(R.id.catalog);
        catalog.setOnClickListener(
                new View.OnClickListener() { @Override public void onClick(View v)
                { startActivity(new Intent(LoginWebView.this, Catalog.class));
                    finish();
                    System.out.println("zzz clicking catalog");
                } });
        Log.i("zzz JSON parse: ", token);
        //output.setText("Course Planner Login");*/


        final WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        //final String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?token="+token+"&redirect=https%3A%2F%2Fapis.huit.harvard.edu%2Ffascourseplanner%2Frest%2Fv1%2Fcart%2Fcourses%3FauthComplete%3D1%26token%3D"+token;
        final String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?token="+token+"&redirect=http%3A%2F%2Fwww.people.fas.harvard.edu%2F%7edornin%2Fapi%2Findex.html%3FauthComplete%3D1%26token%3D"+token;
        System.out.println("zzz Login URL:" + loginURL);
        myWebView.loadUrl(loginURL);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        final CookieSyncManager csm = CookieSyncManager.getInstance();
        final CookieManager cookieManager = CookieManager.getInstance();
        // Remove all local cookies
        cookieManager.removeAllCookie();
        myWebView.setWebViewClient(new WebViewClient() {


            public void onPageFinished(WebView view, String url) {
                /**
                 When the process sends the user to the final "success page",
                 take the cookie and send it to my main auth class
                 */
                //System.out.println("zzz view url: " + url);
                //if(url.indexOf("api") != -1){
                    String c = CookieManager.getInstance().getCookie(MY_DOMAIN);
                    //output.setText(c);
                    _cookie = c; //getting cookie string to parse later
                    //System.out.println("zzz _cookie authcomplete: " + _cookie);
                    jsondata = loadUserProfile(token,_cookie,loginURL);
                    if(url.indexOf("authComplete=1") != -1){
                        Utils.sleep(3000);
                        startActivity(new Intent(LoginWebView.this,MyCoursesActivity.class));
                        finish();
                    }
               // }
            }
        });

    }
    // this is only called once the setCookie has been called already
    public static String loadUserProfile(String mytoken, String cookieLocal, String url){

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setCookieStore(new BasicCookieStore());
        BasicHttpContext localContext = new BasicHttpContext();
        String data = "";
        try{
            if(!cookieLocal.equals("")){
                String[] cookies = cookieLocal.split(";");
                //String mylength = Integer.toString(cookies.length);
                for(int i=0; i < cookies.length; i++){
                    String[] nvp = cookies[i].split("=");
                    BasicClientCookie c = new BasicClientCookie(nvp[0], nvp[1]);
                    c.setDomain(MY_DOMAIN);
                    httpclient.getCookieStore().addCookie(c);
                }
            }
            BasicClientCookie cpapi = new BasicClientCookie("cpapi_token", token);
            cpapi.setDomain(MY_DOMAIN);
            httpclient.getCookieStore().addCookie(cpapi);
            BasicClientCookie pin = new BasicClientCookie("faspincookietest", "1");
            pin.setDomain(MY_DOMAIN);
            httpclient.getCookieStore().addCookie(pin);

            List<Cookie> cookies = httpclient.getCookieStore().getCookies();

            if(cookies != null)
            {
                for(Cookie cookie : cookies)
                {
                    String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                    //Log.i("zzz Cookie in the loop Info", cookieString);
                }
            }
            HttpGet httpget = new HttpGet(url);
            System.out.println("zzz COOKIE executing request " + httpget.getURI());
            HttpResponse response = httpclient.execute(httpget);
            //int code = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            data = Utils.convertStreamToString(is);
            //jsondata = data;
            //Log.i("JSON from load profile", data);

        }catch(Exception e){
            // Log the error
            e.printStackTrace();
            //return false;
        }
        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public static String getToken(){
        String gettoken = token;
        return gettoken;
    }

    public static String getLocalCookie(){
        String getcookie = _cookie;
        return getcookie;
    }

    public void fetchToken() {
        //final String mylink = s;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String data = "";
                String returnToken = "";
                try {
                    URL url = new URL(tokenUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    //System.out.println("zzz before connect data: " + data);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    data = Utils.convertStreamToString(stream);
                    //System.out.println("zzz after connect data: " + data);
                    //jsondata = data;
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {

                    JSONObject jsonResponse = new JSONObject(data);
                    token = (String) jsonResponse.get("token");
                    System.out.println("zzz return token: " + token);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            });
            thread.start();
    }
}



