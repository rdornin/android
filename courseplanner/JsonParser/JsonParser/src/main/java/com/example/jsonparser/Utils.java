package com.example.jsonparser;

/**
 * Created by dornin on 3/30/14.
 */
public class Utils {

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

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



}
