package com.mindfulness.dornin.mindfulness;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;


import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private TextView mTextView;
    private Integer counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        final Timer t = new Timer();
        final String[] msgs = new String[]{"Focus on the present","Relax, breathe deep","All is well"};

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);


                Thread thread = new Thread()
                {
                    @Override
                    public void run() {
                        try {
                            while(true) {
                                sleep(5000);
                                counter++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String text = counter.toString();
                                        if(counter > msgs.length - 1) {
                                            counter=0;
                                        }
                                        mTextView.setText(msgs[counter]);
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        v.vibrate(500);
                                    }
                                });

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();


            }



        });
    }
}
