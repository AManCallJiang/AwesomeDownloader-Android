package com.jiang.awesomedownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri


class OpenFileReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val stringExtra = intent.getStringExtra("PATH")
        //Toast.makeText(context, stringExtra, Toast.LENGTH_SHORT).show()
        val uri: Uri = Uri.parse(stringExtra)
        val openIntent = Intent("android.intent.action.VIEW")
        //val type = getType(stringExtra)
        //Log.d(TAG, "onReceive: $type")
        openIntent.let {
            it.addCategory("android.intent.category.DEFAULT")
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.setDataAndType(uri, "*/*")
            context.startActivity(it)
        }
    }

    private fun getType(path: String): String {
        val split = path.split(".")
        val s = split[split.size - 1]
        if (s.contains("mp4", ignoreCase = true) ||
            s.contains("mpg", ignoreCase = true) ||
            s.contains("mpeg", ignoreCase = true) ||
            s.contains("avi", ignoreCase = true) ||
            s.contains("rm", ignoreCase = true) ||
            s.contains("rmvb", ignoreCase = true) ||
            s.contains("mov", ignoreCase = true) ||
            s.contains("wmv", ignoreCase = true) ||
            s.contains("wmv", ignoreCase = true) ||
            s.contains("asf", ignoreCase = true) ||
            s.contains("dat", ignoreCase = true)
        ) {
            return "video/*"
        } else if (s.contains("apk", ignoreCase = true)) {
            return "application/vnd.android.package-archive"
        } else if (s.contains("mp3", ignoreCase = true) ||
            s.contains("wma", ignoreCase = true) ||
            s.contains("wav", ignoreCase = true) ||
            s.contains("mid", ignoreCase = true)
        ) {
            return "audio/*"
        } else if (s.contains("bmp", ignoreCase = true) ||
            s.contains("jpg", ignoreCase = true) ||
            s.contains("jpeg", ignoreCase = true) ||
            s.contains("png", ignoreCase = true) ||
            s.contains("gif", ignoreCase = true) ||
            s.contains("svg", ignoreCase = true)
        ) {
            return "image/*"
        } else if (s.contains("html", ignoreCase = true)) {
            return "text/html"
        } else {
            return "*/*"
        }
    }
}

fun isVideoFile(fileName: String): Boolean {
    val split = fileName.split(".")
    val s = split[split.size - 1]
    return s.equals("mp4", ignoreCase = true) ||
            s.equals("mpg", ignoreCase = true) ||
            s.equals("mpeg", ignoreCase = true) ||
            s.equals("avi", ignoreCase = true) ||
            s.equals("rm", ignoreCase = true) ||
            s.equals("rmvb", ignoreCase = true) ||
            s.equals("mov", ignoreCase = true) ||
            s.equals("wmv", ignoreCase = true) ||
            s.equals("asf", ignoreCase = true) ||
            s.equals("dat", ignoreCase = true)
}

fun isAudioFile(fileName: String): Boolean {
    val split = fileName.split(".")
    val s = split[split.size - 1]
    return s.equals("mp3", ignoreCase = true) ||
            s.equals("wma", ignoreCase = true) ||
            s.equals("wav", ignoreCase = true) ||
            s.equals("mid", ignoreCase = true)
}

fun isImageFile(fileName: String): Boolean {
    val split = fileName.split(".")
    val s = split[split.size - 1]
    return s.contains("bmp", ignoreCase = true) ||
            s.contains("jpg", ignoreCase = true) ||
            s.contains("jpeg", ignoreCase = true) ||
            s.contains("png", ignoreCase = true) ||
            s.contains("gif", ignoreCase = true)
}