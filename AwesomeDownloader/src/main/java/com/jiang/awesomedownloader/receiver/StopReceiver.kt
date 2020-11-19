package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jiang.awesomedownloader.core.AwesomeDownloader
import com.jiang.awesomedownloader.tool.TAG

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: stop")
        AwesomeDownloader.stopAll()
    }
}
