package com.stemsc.routelist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

//import com.stemsc.routelist.db.DBHelper;
import com.stemsc.routelist.db.DBHelperDate;
import com.stemsc.routelist.db.DBHelperLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by ONB on 11.07.2017.
 * Send
 */

class Send extends AsyncTask<String, Void, JSONObject> {

    private static final String TAG = ">>Send>>";
    private String method;
    private String body;
    private int respCode = 999;
    private SharedPreferences sPref;
    private DBHelperLog dbHelperLog;
//    private DBHelper dbHelper;
    private DBHelperDate dbHelperDate;

    Send(Context context){
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        dbHelperLog = new DBHelperLog(context);
        dbHelperDate = new DBHelperDate(context);
//        dbHelper = new DBHelper(context);
//        Log.d(">>> ", "Send(Context context)");
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        JSONObject ret = new JSONObject();
        method = urls[0];
        String url = urls[1];
        body = urls[2];
//        Log.d(">>> ", "doInBackground");
        try {
            ret.put("data", loadFromNetwork(url));
            ret.put("code", respCode);
            Log.d(">>> ", method + "=" + url);
            Log.d("<<< ", "(" + ret.getInt("code") + ") " + ret.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String loadFromNetwork(String urlString) {
        InputStream stream = null;
        String str = "";
        try {
            stream = downloadUrl(urlString);
            if (stream != null)
                str = readIt(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }

    /**
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     */
    private InputStream downloadUrl(String urlString) throws IOException {

//        if (ServGPS.context != null) context=ServGPS.context;
//        else if (ServTimer.context != null) context=ServTimer.context;
//        else if (MainActivity.context != null) context=MainActivity.context;
//        else if (RoutesActivity.context != null) context=RoutesActivity.context;
//        else if (PointsActivity.context != null) context=PointsActivity.context;
//
//        sPref = PreferenceManager.getDefaultSharedPreferences(context);
//        DBHelper dbHelperLog = new DBHelper(context);

        String MY_TOKEN = sPref.getString("token", "");

        String BASE_URL = (sPref.contains("dev") ? sPref.getString("dev", Utils.WRK) : Utils.WRK);

        URL url = new URL(BASE_URL + "/api/" + urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod(method);
        conn.setDoInput(true);

        assert MY_TOKEN != null;
        if (!MY_TOKEN.equals("")) {
            //  String basicAuth = "Basic " + new String(Base64.encode("driver:1111".getBytes(),Base64.NO_WRAP ));
            String basicAuth = "Token " + MY_TOKEN;
            conn.setRequestProperty("Authorization", basicAuth);
        }
        InputStream ret = null;
        try {
            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
                OutputStream os = conn.getOutputStream();
                if (os != null) os.write(body.getBytes("UTF-8"));
//            else  ??????????
            }

            conn.connect();
            respCode = conn.getResponseCode();
            switch (respCode) {
//                case 401:
//                    sPref.edit().remove("token").apply();
//                exit(0);
//                    break;
                case 200:
                case 201:
                    ret = conn.getInputStream();
                    dbHelperLog.log("<font color=\"green\">=SEND = "+method+" "+urlString+" ("+respCode+")</font>");
                    if(method.equals("GET")) {
                        dbHelperDate.setTimeUrl(urlString);
                        Log.d(TAG, "dbHelperLog.setTimeUrl = " + urlString);
                    }
                    break;
                default:
                    dbHelperLog.log("<font color=\"red\">=SEND ERROR == "+method+" "+urlString+" ("+respCode+")</font>");
                    Log.d(TAG,"<font color=\"red\">=SEND = respCode="+respCode+"</font>");
//                ret = conn.getInputStream();
            }
        } catch (UnknownHostException e) {
            Log.d(TAG,"===== SEND error = UnknownHostException");
            dbHelperLog.log("<font color=\"red\">=SEND = UnknownHostException="+urlString+"</font>");
            return null;
        } catch (java.net.SocketTimeoutException e) {
            Log.d(TAG,"===== SEND error = SocketTimeoutException");
            dbHelperLog.log("<font color=\"red\">=SEND = SocketTimeoutException="+urlString+"</font>");
            return null;
        } catch (java.io.EOFException e) {
            Log.d(TAG,"===== SEND error = EOFException");
            dbHelperLog.log("<font color=\"red\">=SEND = EOFException="+urlString+"</font>");
            return null;
        } catch (javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG,"===== SEND error = SSLHandshakeException");
            dbHelperLog.log("<font color=\"red\">=SEND = SSLHandshakeException="+urlString+"</font>");
            return null;
        }
        return ret;
    }

    /**
     * @param stream InputStream containing HTML from targeted site.
     * @return String concatenated according to len parameter.
     */
    private String readIt(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return new String(buffer);
    }
}
