package com.jiang.awesomedownloader.tool

import  android.os.StrictMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection

const val WRITE_BUFFER_SIZE = 4096
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
    val s = getFileExtension(fileName)
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
    val s = getFileExtension(fileName)
    return s.equals("mp3", ignoreCase = true) ||
            s.equals("wma", ignoreCase = true) ||
            s.equals("wav", ignoreCase = true) ||
            s.equals("mid", ignoreCase = true)
}

fun isImageFile(fileName: String): Boolean {
    val s = getFileExtension(fileName)
    return s.contains("bmp", ignoreCase = true) ||
            s.contains("jpg", ignoreCase = true) ||
            s.contains("jpeg", ignoreCase = true) ||
            s.contains("png", ignoreCase = true) ||
            s.contains("gif", ignoreCase = true)
}

fun isApkFile(fileName: String): Boolean {
    val s = getFileExtension(fileName)
    return s.contains("apk", ignoreCase = true)

}

const val STRING_DOT = "."
fun getFileExtension(fileName: String): String {
    val splitStrings = fileName.split(STRING_DOT)
    if (splitStrings.size <= 1) return ""
    val extension = splitStrings[splitStrings.size - 1]
   // Log.d(TAG, "getFileExtension: $extension")
    return extension
}

//fun getMimeType(fileName: String): String {
//    return when {
//        isImageFile(fileName) -> "image/${getFileExtension(fileName)}"
//        isVideoFile(fileName) -> "video/${getFileExtension(fileName)}"
//        isAudioFile(fileName) -> "audio/${getFileExtension(fileName)}"
//        isApkFile(fileName) -> "application/vnd.android.package-archive"
//        else -> ""
//    }
//}
fun getMimeType(fileName: String): String? {
    return URLConnection.getFileNameMap().getContentTypeFor(fileName)
}

//报错：exposed beyond app through ClipData.Item.getUri，使用
fun exposedFileUri() {
    val builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())
    builder.detectFileUriExposure()
}