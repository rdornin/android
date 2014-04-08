package com.example.jsonparser;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ListView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.Thread;
import java.util.ArrayList;
import android.widget.AdapterView;
import android.widget.Toast;
import android.telephony.gsm.SmsManager;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.app.ListActivity;

public class MyCoursesActivity extends Activity {


    private String url1 = "http://www.people.fas.harvard.edu/~dornin/api/catalog.js";
    String jsondata = "Loading json...";
    ArrayList<CharSequence> ar = new ArrayList<CharSequence>();
    private int mNoteNumber = 1;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();


        final TextView output = (TextView) findViewById(R.id.output);

        Button courses = (Button) findViewById(R.id.courses);
        courses.setOnClickListener(
                new View.OnClickListener() { @Override public void onClick(View v)
                { startActivity(new Intent(MyCoursesActivity.this, MyCoursesActivity.class));
                    finish();
                    System.out.println("clicking courses");
                } });

        Button catalog = (Button) findViewById(R.id.catalog);
        catalog.setOnClickListener(
                new View.OnClickListener() { @Override public void onClick(View v)
                { startActivity(new Intent(MyCoursesActivity.this, Catalog.class));
                    finish();
                    System.out.println("clicking catalog");
                } });

        Button quit=(Button)findViewById(R.id.quit);
        quit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });
        Button sendtext=(Button)findViewById(R.id.sendtext);
        sendtext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("sms_body","Hey classmate, I am signing up for courses with the course planner app! Check it out!");
                        startActivity(smsIntent);
                    }
                });
        output.setText("My Courses");

        jsondata = LoginWebView.loadUserProfile(LoginWebView.token, LoginWebView._cookie,(LoginWebView.courseUrl+LoginWebView.token));

        String OutputData = "";
        JSONObject jsonResponse;

        Utils.sleep(1000); //buying some time while json loads

        try {
            System.out.println("processing json:" + jsondata);
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
                String course_num = jsonChildNode.optString("cat_num").toString();
                //Log.i("JSON COURSE TITLE", course_title);

                String tmpData = "<b>Delete</b> <br/>"
                        + "Course Title: \n\n     " + course_title + " <br/> Course Value: "
                        + course_value + " <br/> Group Code: "
                        + group_code
                        + "(" + course_num + ")";


                OutputData += Html.fromHtml(tmpData);
                //tmpData = tmpData;
                ar.add(Html.fromHtml(tmpData));

                //Log.i("JSON COURSE TITLE", course_title);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
        populateListView();
        registerClickCallback("delete");

    }

    private void populateListView() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                R.layout.textview,
                ar);
        // Configure the list view.
        ListView list = (ListView) findViewById(R.id.listViewMain);
        list.setAdapter(adapter);
    }

    private void registerClickCallback(String actionType) {
        ListView list = (ListView) findViewById(R.id.listViewMain);
        final String action = actionType;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                //mDbHelper.createNote(noteName, "");
                //fillData();
                String course_num = getCatNum(textView.getText().toString());
                if (action.equals("add")){
                    //createNote(textView.getText().toString());
                    String message = "You select to " + textView.getText().toString();
                    String addUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/course/"+course_num+"?method=post&terse=0&token="+LoginWebView.token;
                    LoginWebView.loadUserProfile(LoginWebView.token, LoginWebView._cookie,(addUrl));
                    Utils.sleep(500);
                    populateListView();

                } else {
                    String message = "You selected to " + textView.getText().toString();
                    Toast.makeText(MyCoursesActivity.this, message, Toast.LENGTH_LONG).show();
                    String deleteUrl = "https://apis.huit.harvard.edu/fascourseplanner/rest/v1/cart/course/"+course_num+"?method=delete&terse=0&token="+LoginWebView.token;
                    LoginWebView.loadUserProfile(LoginWebView.token, LoginWebView._cookie,(deleteUrl));
                    System.out.println(deleteUrl);
                    Utils.sleep(500);
                    startActivity(new Intent(MyCoursesActivity.this, MyCoursesActivity.class));
                    finish();
                }
            }
        });

    }
    public static String getCatNum(String course) {
         String cat_num = course.substring((course.indexOf("(") + 1),(course.indexOf(")")));
         return cat_num;
    }

}