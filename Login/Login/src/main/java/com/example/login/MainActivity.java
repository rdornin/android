package com.example.login;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
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

public class MainActivity extends ActionBarActivity {

    private String url1 = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/token";
    private String logOUTURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/logout";
    private String courseUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/courses?token=";
    String jsondata;
    String token;
    String _cookie;
    ArrayList<String> ar = new ArrayList<String>();
    private static String MY_DOMAIN = "apis.huit.harvard.edu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //need to do this to get the loadUserProfile to Work
        if (android.os.Build.VERSION.SDK_INT > 7) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        fetchJSON(url1);
        sleep(3000); // buying some time while json loads
        JSONObject jsonResponse;
        final TextView output = (TextView) findViewById(R.id.output);
        final Button catalog = (Button) findViewById(R.id.catalog);
        try {
            /****** GETTING TOKEN AS UNIQUE IDENTIFIER FOR API ********/
            jsonResponse = new JSONObject(jsondata);
            token = (String) jsonResponse.get("token");
            Log.i("JSON parse: ", token);
            output.setText(url1+"  |  " + token);
        } catch (JSONException e) {

            e.printStackTrace();
        }


        final WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        final String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?token="+token+"&redirect=https%3A%2F%2Fapis.huit.harvard.edu%2Ffascourseplanner%2Frest%2Fv1%2Fcart%2Fcourses%3FauthComplete%3D1%26token%3D"+token;
        System.out.println("My login URL: " + loginURL);
        myWebView.loadUrl(loginURL);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        final CookieSyncManager csm = CookieSyncManager.getInstance();
        final CookieManager cookieManager = CookieManager.getInstance();
        // Remove all cookies from the database.
        cookieManager.removeAllCookie();
        myWebView.setWebViewClient(new WebViewClient() {


            public void onPageFinished(WebView view, String url) {
                /**
                 When the process sends the user to the final "success page",
                 take the cookie and send it to my main auth class
                 */
                if(url.indexOf("authComplete") != -1){
                    String c = CookieManager.getInstance().getCookie(MY_DOMAIN);
                    output.setText(c);
                    _cookie = c;
                }
            }
        });

        //myWebView.loadUrl(courseUrl + token);


        catalog.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                loadUserProfile(token);
                String OutputData = "";
                JSONObject jsonResponse;
                //fetchJSON(courseUrl+token);

                sleep(3000); //buying some time while json loads


                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(jsondata);

                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    /*******  Returns null otherwise.  *******/
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("cart");

                    /*********** Process each JSON Node ************/

                    int lengthJsonArr = jsonMainNode.length();

                    for (int i = 0; i < lengthJsonArr; i++) {
                        /****** Get Object for each JSON node.***********/
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                        /******* Fetch node values **********/
                        String course_title = jsonChildNode.optString("title").toString();
                        String course_value = jsonChildNode.optString("value").toString();
                        String group_code = jsonChildNode.optString("group_code").toString();
                        String course_label = jsonChildNode.optString("label").toString();
                        //Log.i("JSON COURSE TITLE", course_title);

                        String tmpData = "<p><a href='http://www.google.com'>testing</a>: "
                               + "Course Title : \n\n     " + course_title + " | "
                                + course_value + " | "
                                + group_code + " </p> ";


                        OutputData += Html.fromHtml(tmpData);
                        tmpData = tmpData;
                        ar.add(tmpData);

                        Log.i("JSON COURSE TITLE", course_title);
                    }




                } catch (JSONException e) {

                    e.printStackTrace();
                }
                    output.setText(OutputData);
            }
        });

    }
    // this is only called once the setCookie has been called already
    public boolean loadUserProfile(String mytoken){

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setCookieStore(new BasicCookieStore());
        BasicHttpContext localContext = new BasicHttpContext();

        try{


            if(!_cookie.equals("")){
                String[] cookies = _cookie.split(";");
                String mylength = Integer.toString(cookies.length);
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
                    Log.i("Cookie in the loop Info", cookieString);
                }
            }
            HttpGet httpget = new HttpGet(courseUrl + mytoken);
            System.out.println("COOKIE executing request " + httpget.getURI());
            HttpResponse response = httpclient.execute(httpget);
            int code = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            String data = convertStreamToString(is);
            jsondata = data;
            Log.i("JSON from load profile", jsondata);

            if(code >= 400 && code < 500){

                return false;
            }
            else{
                return true;
            }

        }catch(Exception e){
            // Log the error
            e.printStackTrace();
            return false;
        }
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

    public void fetchJSON(String s) {
        final String mylink = s;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //String data = "";
                try {
                    URL url = new URL(mylink);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    String data = convertStreamToString(stream);
                    jsondata = data;
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        //return data;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


}
