package com.jiang.awesomedownloader

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