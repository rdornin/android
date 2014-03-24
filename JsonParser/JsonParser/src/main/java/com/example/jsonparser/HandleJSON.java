package com.example.jsonparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.annotation.SuppressLint;

public class HandleJSON {
    private String searchbox = "searchbox";
    private String courseTitle = "Course Title";
    private String urlString = null;

    public volatile boolean parsingComplete = true;
    public HandleJSON(String url){
        this.urlString = url;
    }

    public String getSearchBox(){
        return searchbox;
    }

    public String getCourseTitle(){
        return courseTitle;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);

/*
{"coord":{"lon":36.89,"lat":-1.19},
"sys":{"message":0.027,"country":"Kenya","sunrise":1395372969,"sunset":1395416573},
"weather":[{"id":801,"main":"Clouds","description":"few clouds","icon":"02d"}],"base":"cmc stations",
"main":{"temp":299.56,"pressure":1017,"humidity":39,"temp_min":299.15,"temp_max":300.15},
"wind":{"speed":6.2,"deg":70,"var_beg":40,"var_end":100},"clouds":{"all":20},"dt":1395408600,"id":192710,
"name":"Nairobi","cod":200}
             */

            JSONObject catalog  = reader.getJSONObject("catalog");
            courseTitle = catalog.getString("title");

            parsingComplete = false;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}