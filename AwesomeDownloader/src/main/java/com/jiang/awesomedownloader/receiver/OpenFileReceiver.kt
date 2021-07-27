package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.jiang.awesomedownloader.tool.*
import java.io.File

const val INTENT_EXTRA_PATH = "PATH"

class OpenFileReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val stringExtra = intent.getStringExtra(INTENT_EXTRA_PATH)
            Toast.makeText(context, stringExtra, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onReceive: $stringExtra")
            val file = File(stringExtra)
            if (file.exists()) {
                val uriFromFile = Uri.fromFile(file)
                val type = getMimeType(file.name)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setDataAndType(uriFromFile, type)
                    context.startActivity(this)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onReceive: ${e.localizedMessage}", e)
            Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
