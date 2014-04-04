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
    ArrayList<String> ar = new ArrayList<String>();
    private int mNoteNumber = 1;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fetchJSON();
        Utils.sleep(3000);//pausing for json to to load in :)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData(mDbHelper);

        final TextView output = (TextView) findViewById(R.id.output);

        Button daClicker = (Button) findViewById(R.id.catalog);
        daClicker.setOnClickListener(
                new View.OnClickListener() { @Override public void onClick(View v)
                { startActivity(new Intent(MyCoursesActivity.this, Catalog.class));
                finish();
                System.out.println("clicking catalog");
                } });
        /************  Static JSON data ***********/
        output.setText("Catalog");
    }

    private void populateListView() {
        // Create list of items
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
                fillData(mDbHelper);
                if (action.equals("add")){
                    createNote(textView.getText().toString());
                    String message = "You added " + position + ", which is string: " + textView.getText().toString();
                    Toast.makeText(MyCoursesActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    String message = "You deleted " + position + ", which is string: " + textView.getText().toString();
                    Toast.makeText(MyCoursesActivity.this, message, Toast.LENGTH_LONG).show();
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


    private void fillData(NotesDbAdapter db) {
        // Get all of the notes from the database and create the item list
        NotesDbAdapter database = db;
        Cursor c = database.fetchAllNotes();
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

                    String data = Utils.convertStreamToString(stream);
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





}