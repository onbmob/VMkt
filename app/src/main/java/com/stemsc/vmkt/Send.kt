package com.stemsc.VMkt

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.preference.PreferenceManager
import android.util.Log

//import com.stemsc.routelist.db.DBHelper;
//import com.stemsc.routelist.db.DBHelperDate
//import com.stemsc.routelist.db.DBHelperLog

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
//import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

/**
 * Created by ONB on 11.07.2017.
 * Send
 */

internal class Send(context: Context) : AsyncTask<String, Void, JSONObject>() {
    private var method: String? = null
    private var body: String? = null
    private var respCode = 999
    private val sPref: SharedPreferences
//    private val dbHelperLog: DBHelperLog
    //    private DBHelper dbHelper;
//    private val dbHelperDate: DBHelperDate

    init {
        sPref = PreferenceManager.getDefaultSharedPreferences(context)
//        dbHelperLog = DBHelperLog(context)
//        dbHelperDate = DBHelperDate(context)
        //        dbHelper = new DBHelper(context);
        //        Log.d(">>> ", "Send(Context context)");
    }

    override fun doInBackground(vararg urls: String): JSONObject {
        val ret = JSONObject()
        method = urls[0]
        val url = urls[1]
        body = urls[2]
        //        Log.d(">>> ", "doInBackground");
        try {
            ret.put("data", loadFromNetwork(url))
            ret.put("code", respCode)
            Log.d(">>> ", "$method=$url")
            Log.d("<<< ", "(" + ret.getInt("code") + ") " + ret.getString("data"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return ret
    }

    private fun loadFromNetwork(urlString: String): String {
        var stream: InputStream? = null
        var str = ""
        try {
            stream = downloadUrl(urlString)
            if (stream != null)
                str = readIt(stream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return str
    }

    /**
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     */
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream? {

        //        if (ServGPS.context != null) context=ServGPS.context;
        //        else if (ServTimer.context != null) context=ServTimer.context;
        //        else if (MainActivity.context != null) context=MainActivity.context;
        //        else if (RoutesActivity.context != null) context=RoutesActivity.context;
        //        else if (PointsActivity.context != null) context=PointsActivity.context;
        //
        //        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        //        DBHelper dbHelperLog = new DBHelper(context);

        val MY_TOKEN = sPref.getString("token", "")

        val BASE_URL =
            if (sPref.contains("dev")) sPref.getString("dev", "Utils.WRK") else "Utils.WRK"

        val url = URL("$BASE_URL/api/$urlString")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 20000
        conn.readTimeout = 20000
        conn.requestMethod = method
        conn.doInput = true

        assert(MY_TOKEN != null)
        if (MY_TOKEN != "") {
            //  String basicAuth = "Basic " + new String(Base64.encode("driver:1111".getBytes(),Base64.NO_WRAP ));
            val basicAuth = "Token " + MY_TOKEN!!
            conn.setRequestProperty("Authorization", basicAuth)
        }
        var ret: InputStream? = null
        try {
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty(
                    "Content-Length",
                    "" + Integer.toString(body!!.toByteArray().size)
                )
                val os = conn.outputStream
                os?.write(body!!.toByteArray(charset("UTF-8")))
                //            else  ??????????
            }

            conn.connect()
            respCode = conn.responseCode
            when (respCode) {
                //                case 401:
                //                    sPref.edit().remove("token").apply();
                //                exit(0);
                //                    break;
                200, 201 -> {
                    ret = conn.inputStream
//                    dbHelperLog.log("<font color=\"green\">=SEND = $method $urlString ($respCode)</font>")
                    if (method == "GET") {
//                        dbHelperDate.setTimeUrl(urlString)
                        Log.d(TAG, "dbHelperLog.setTimeUrl = $urlString")
                    }
                }
                else -> {
//                    dbHelperLog.log("<font color=\"red\">=SEND ERROR == $method $urlString ($respCode)</font>")
                    Log.d(TAG, "<font color=\"red\">=SEND = respCode=$respCode</font>")
                }
            }//                ret = conn.getInputStream();
        } catch (e: UnknownHostException) {
            Log.d(TAG, "===== SEND error = UnknownHostException")
//            dbHelperLog.log("<font color=\"red\">=SEND = UnknownHostException=$urlString</font>")
            return null
        } catch (e: java.net.SocketTimeoutException) {
            Log.d(TAG, "===== SEND error = SocketTimeoutException")
//            dbHelperLog.log("<font color=\"red\">=SEND = SocketTimeoutException=$urlString</font>")
            return null
        } catch (e: java.io.EOFException) {
            Log.d(TAG, "===== SEND error = EOFException")
//            dbHelperLog.log("<font color=\"red\">=SEND = EOFException=$urlString</font>")
            return null
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            Log.d(TAG, "===== SEND error = SSLHandshakeException")
//            dbHelperLog.log("<font color=\"red\">=SEND = SSLHandshakeException=$urlString</font>")
            return null
        }

        return ret
    }

    /**
     * @param stream InputStream containing HTML from targeted site.
     * @return String concatenated according to len parameter.
     */
    @Throws(IOException::class)
    private fun readIt(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val buffer = StringBuilder()
        var line: String
        while (true) {
            line = reader.readLine()
            if ( line == null) break
            buffer.append(line)
        }
        return String(buffer)
    }

    companion object {

        private val TAG = ">>Send>>"
    }
}
