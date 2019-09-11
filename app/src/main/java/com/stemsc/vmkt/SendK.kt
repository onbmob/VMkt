package com.stemsc.vmkt

import android.os.AsyncTask

class SendK internal constructor(context: MainActivity) : AsyncTask<Int, String, String?>() {

    private var resp: String? = null
    private val activityReference: WeakReference<MainActivity> = WeakReference(context)

    override fun onPreExecute() {
        val activity = activityReference.get()
        if (activity == null || activity.isFinishing) return
        activity.progressBar.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg params: Int?): String? {
        publishProgress("Sleeping Started") // Calls onProgressUpdate()
        try {
            val time = params[0]?.times(1000)
            time?.toLong()?.let { Thread.sleep(it / 2) }
            publishProgress("Half Time") // Calls onProgressUpdate()
            time?.toLong()?.let { Thread.sleep(it / 2) }
            publishProgress("Sleeping Over") // Calls onProgressUpdate()
            resp = "Android was sleeping for " + params[0] + " seconds"
        } catch (e: InterruptedException) {
            e.printStackTrace()
            resp = e.message
        } catch (e: Exception) {
            e.printStackTrace()
            resp = e.message
        }

        return resp
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
}
