package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jiang.awesomedownloader.downloader.AwesomeDownloader

class CancelAllReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AwesomeDownloader.cancelAll()
    }
}
