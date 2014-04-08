package com.example.jsonparser;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class LoginWebView extends Activity {

    public static String tokenUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/token";
    public static String courseUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/courses?terse=0&token=";
    String jsondata;
    public static String token = "";
    public static String _cookie;

    private static String MY_DOMAIN = "apis.huit.harvard.edu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web_view);
        fetchToken();

        Utils.sleep(1000); // buying some time while json loads


        final TextView output = (TextView) findViewById(R.id.output);
        final WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        //final String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?token="+token+"&redirect=https%3A%2F%2Fapis.huit.harvard.edu%2Ffascourseplanner%2Frest%2Fv1%2Fcart%2Fcourses%3FauthComplete%3D1%26token%3D"+token;
        final String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?token=" + token + "&redirect=http%3A%2F%2Fwww.people.fas.harvard.edu%2F%7edornin%2Fapi%2Findex.html%3FauthComplete%3D1%26token%3D" + token;
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
                String c = CookieManager.getInstance().getCookie(MY_DOMAIN);
                _cookie = c; //getting cookie string to parse later
                jsondata = loadUserProfile(token, _cookie, loginURL);
                if (url.indexOf("authComplete=1") != -1) {
                    Utils.sleep(2000);
                    startActivity(new Intent(LoginWebView.this, MyCoursesActivity.class));
                    finish();
                }
            }
        });

    }

    // this is only called once the setCookie has been called already
    public static String loadUserProfile(String mytoken, String cookieLocal, String url) {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setCookieStore(new BasicCookieStore());
        BasicHttpContext localContext = new BasicHttpContext();
        String data = "";
        try {
            if (!cookieLocal.equals("")) {
                String[] cookies = cookieLocal.split(";");
                for (int i = 0; i < cookies.length; i++) {
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

            HttpGet httpget = new HttpGet(url);
            System.out.println("zzz COOKIE executing request " + httpget.getURI());
            HttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            data = Utils.convertStreamToString(is);
            Log.i("JSON from load profile", data);

        } catch (Exception e) {
            e.printStackTrace();
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

    public static String getToken() {
        String gettoken = token;
        return gettoken;
    }

    public static String getLocalCookie() {
        String getcookie = _cookie;
        return getcookie;
    }

    public void fetchToken() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String data = "";
                try {
                    URL url = new URL(tokenUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    data = Utils.convertStreamToString(stream);
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



