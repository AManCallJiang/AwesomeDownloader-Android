package com.jiang.awesomedownloader.tool

import com.jiang.awesomedownloader.downloader.WRITE_BUFFER_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

suspend fun writeFileInDisk(body: ResponseBody, target: File, isAppend: Boolean) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(WRITE_BUFFER_SIZE)
        val inputStream = body.byteStream()
        val outputStream = FileOutputStream(target, isAppend)
        inputStream.use { ips ->
            outputStream.use { ops ->
                while (true) {
                    val read = ips.read(buffer)
                    if (read == -1) break
                    ops.write(buffer, 0, read)
                }
                ops.flush()
            }
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