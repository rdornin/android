package com.example.login;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private String url1 = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/token";
    private String loginURL = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/login?";
    private String courseUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/courses?token=";
    String jsondata;
    String token;
    ArrayList<String> ar = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchJSON(url1);
        sleep(3000);
        JSONObject jsonResponse;


        final TextView output = (TextView) findViewById(R.id.output);
        final Button catalog = (Button) findViewById(R.id.catalog);
        //final Button courses = (Button) findViewById(R.id.courses);

        try {

            /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
            jsonResponse = new JSONObject(jsondata);

            token = (String) jsonResponse.get("token");

            Log.i("JSON parse: ", token);

            output.setText(url1+token);


        } catch (JSONException e) {

            e.printStackTrace();
        }


//        if (savedInstanceState == null) {
  //          getSupportFragmentManager().beginTransaction()
    //                .add(R.id.container, new PlaceholderFragment())
      //              .commit();
        //}
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl(loginURL + token);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        //myWebView.getSettings().setLoadWithOverviewMode(true);
        //myWebView.getSettings().setUseWideViewPort(true);


        catalog.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String OutputData = "";
                JSONObject jsonResponse;
                fetchJSON(courseUrl+token);
                sleep(3000);

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

                        //Button button = new Button(this);
                        //button.setId(i);
                        //yourView.add(button);
                        String tmpData = "<p><a href='http://www.google.com'>testing</a>: "
                                + "Course Title : \n\n     " + course_title + " | "
                                + course_value + " | "
                                + group_code + " </p> ";


                        OutputData += Html.fromHtml(tmpData);
                        //tmpData = tmpData;
                        ar.add(tmpData);

                        Log.i("JSON parse", course_title);
                    }

                    //populateListView();
                    //r/egisterClickCallback("add");

                    /************ Show Output on screen/activity **********/
                    //((TextView) findViewById(R.id.output)).setMovementMethod(LinkMovementMethod.getInstance());
                    output.setText(Html.fromHtml(OutputData));

                    //((TextView) findViewById(R.id.output)).setText(Html.fromHtml(OutputData));


                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        });

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
                    //readAndParseJSON(data);
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
