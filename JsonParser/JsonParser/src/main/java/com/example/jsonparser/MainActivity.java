package com.example.jsonparser;

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


import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.app.ListActivity;

public class MainActivity extends Activity {

    private String url1 = "http://www.people.fas.harvard.edu/~dornin/api/catalog.js";
    String jsondata = "Loading json...";
    ArrayList<String> ar = new ArrayList<String>();
    private int mNoteNumber = 1;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fetchJSON();
        sleep(3000);//pausing for json to to load in :)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();

        final TextView output = (TextView) findViewById(R.id.output);
        final Button catalog = (Button) findViewById(R.id.catalog);
        final Button courses = (Button) findViewById(R.id.courses);


        /************  Static JSON data ***********/
        String dataToBeParsed = "Click on button to parse JSON.\n\n JSON DATA : \n\n" + jsondata;
        output.setText("My Courses");

        /******** Listener for button click ********/
        courses.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                output.setText("My Courses");
                fillData();
                registerClickCallback("delete");
            }
            });

                /******** Listener for button click ********/
       catalog.setOnClickListener(new View.OnClickListener() {

           public void onClick(View v) {
               output.setText("Course Catalog");
               String OutputData = "";
               JSONObject jsonResponse;

               try {

                   /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                   jsonResponse = new JSONObject(jsondata);

                   /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                   /*******  Returns null otherwise.  *******/
                   JSONArray jsonMainNode = jsonResponse.optJSONArray("catalog");

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

                   populateListView();
                   registerClickCallback("add");

                   /************ Show Output on screen/activity **********/
                   //((TextView) findViewById(R.id.output)).setMovementMethod(LinkMovementMethod.getInstance());
                   //output.setText(Html.fromHtml(OutputData));

                   //((TextView) findViewById(R.id.output)).setText(Html.fromHtml(OutputData));


               } catch (JSONException e) {

                   e.printStackTrace();
               }

           }
       });
    }

    private void populateListView() {
        // Create list of items
        //String[] myItems = {"Blue", "Green", "Purple", "Red"}; // Build Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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
                //sendSMS("99999999999", "message");
                //mDbHelper.createNote(noteName, "");
                //fillData();
                if (action.equals("add")){
                    createNote(textView.getText().toString());
                    String message = "You added " + position + ", which is string: " + textView.getText().toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    String message = "You deleted " + position + ", which is string: " + textView.getText().toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    mDbHelper.deleteNote(position);
                    populateListView();
                }
            }
        });
    }

    private void createNote(String course_info) {
        String noteName = "Note " + mNoteNumber++;
        mDbHelper.createNote(noteName, course_info);
        Log.i("Note Created", course_info);
        //fillData();
    }


    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchAllNotes();
        startManagingCursor(c);

        String[] from = new String[] { NotesDbAdapter.KEY_BODY };
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
               new SimpleCursorAdapter(this, R.layout.textview, c, from, to);
        //setListAdapter(notes);
        ListView list = (ListView) findViewById(R.id.listViewMain);
        list.setAdapter(notes);
        for( int i = 0; i <= from.length - 1; i++)
        {
            Log.i("JSON parse", from[i]);
            // and the next time get element      1 and 2 and put this in another variable.
        }

    }

    public void fetchJSON() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //String data = "";
                try {
                    URL url = new URL(url1);
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

    /**
     * ****************************************************************
     * This is a simple static method to cause the control flow to pause.
     *
     * @param time in milliseconds
     *             ****************************************************************
     */
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}