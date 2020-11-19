package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jiang.awesomedownloader.core.AwesomeDownloader

class CancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        AwesomeDownloader.cancel()
    }
}
