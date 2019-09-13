package com.stemsc.vmkt

//import android.content.Context
import android.os.AsyncTask
import android.util.Log
//import androidx.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

const val TAG = "Send"

//internal class Send constructor(context: Context) : AsyncTask<String, Void, JSONObject?>() {
internal class Send : AsyncTask<String, Void, JSONObject?>() {

    //    private var resp: String? = null
//    private val activityReference: WeakReference<MainActivity> = WeakReference(context)

    private var method: String? = null
    private var body: String? = null
    private var respCode = 999
//    private val sPref = PreferenceManager.getDefaultSharedPreferences(context)


    override fun doInBackground(vararg urls: String): JSONObject {
        val ret = JSONObject()
        method = urls[0]
        val url = urls[1]
        body = urls[2]
        try {
            ret.put("data", loadFromNetwork(url))
            ret.put("code", respCode)
            Log.d(TAG, "$method=$url")
            Log.d(TAG, "(" + ret.getInt("code") + ") " + ret.getString("data"))
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

    private fun downloadUrl(urlString: String): InputStream? {
/*
        val MY_TOKEN = sPref.getString("token", "")
        val BASE_URL = if (sPref.contains("dev")) sPref.getString("dev", "Utils.WRK") else "Utils.WRK"
*/

        val url = URL("$BASE_URL/api/$urlString")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 20000
        conn.readTimeout = 20000
        conn.requestMethod = method
        conn.doInput = true

//        assert(MY_TOKEN != null)
//        if (MY_TOKEN != "") {
        //  String basicAuth = "Basic " + new String(Base64.encode("driver:1111".getBytes(),Base64.NO_WRAP ));
        val basicAuth = "Token $MY_TOKEN"
        conn.setRequestProperty("Authorization", basicAuth)
//        }
        var ret: InputStream? = null
        try {
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty(
                    "Content-Length",
                    "" + body!!.toByteArray().size.toString()
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
                }
                else -> {
//                    dbHelperLog.log("<font color=\"red\">=SEND ERROR == $method $urlString ($respCode)</font>")
                    Log.d(TAG, "===== SEND error = $method - $url - respCode=$respCode")
                }
            }//                ret = conn.getInputStream();
        } catch (e: UnknownHostException) {
            Log.d(TAG, "===== SEND error = UnknownHostException")
            return null
        } catch (e: java.net.SocketTimeoutException) {
            Log.d(TAG, "===== SEND error = SocketTimeoutException")
            return null
        } catch (e: java.io.EOFException) {
            Log.d(TAG, "===== SEND error = EOFException")
            return null
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            Log.d(TAG, "===== SEND error = SSLHandshakeException")
            return null
        }

        return ret
    }

    @Throws(IOException::class)
    private fun readIt(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val buffer = StringBuilder()
        var line = reader.readLine()
        while (line  != null) {
            buffer.append(line)
            line = reader.readLine()
        }
        return String(buffer)
    }
/*

    override fun onPreExecute() {
//        val activity = activityReference.get()
//        if (activity == null || activity.isFinishing) return
//        activity.progressBar.visibility = View.VISIBLE
    }



    override fun onPostExecute(result: String?) {

        // execution of result of Long time consuming operation
        val activity = activityReference.get()
        if (activity == null || activity.isFinishing) return
        // access Activity member variables or modify the activity's UI
        activity.progressBar.visibility = View.GONE
        activity.textView.text = result.let { it }
        activity.myVariable = 100
    }

    override fun onProgressUpdate(vararg text: String?) {

        val activity = activityReference.get()
        if (activity == null || activity.isFinishing) return

        Toast.makeText(activity, text.firstOrNull(), Toast.LENGTH_SHORT).show()

    }
*/
}
