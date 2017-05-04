package com.xa.okhttpdemo;

import android.util.Log;

/**
 * Created by XA on 3/21/2017.
 *
 */

public class L {

    private static String TAG="okhttp";
    private static boolean debug=true;

    public static void e(String msg) {
        if (debug) {
            Log.e(TAG, msg);
        }
    }

}
