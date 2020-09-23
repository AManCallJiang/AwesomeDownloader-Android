package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jiang.awesomedownloader.downloader.AwesomeDownloader
import com.jiang.awesomedownloader.downloader.TAG

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: stop")
        AwesomeDownloader.stopAll()
    }
}
