package com.acatapps.videomaker.utils

import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileDownloader {

    private val okHttpClient = OkHttpClient()

    interface OnDownloadListener {
        fun onDownloadComplete()
        fun onProgress(progress: Int)
        fun onError(e: Exception)
    }

    fun download(url: String, dirPath: String, fileName: String, listener: OnDownloadListener) {
        val file = File(dirPath, fileName)
        if (file.exists()) {
            file.delete()
        }
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    listener.onError(IOException("Unexpected code $response"))
                    return
                }

                val body = response.body
                if (body == null) {
                    listener.onError(IOException("Response body is null"))
                    return
                }

                val contentLength = body.contentLength()
                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(file)
                    val buffer = ByteArray(2048)
                    var read: Int
                    var totalRead: Long = 0

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        totalRead += read
                        if (contentLength > 0) {
                            val progress = (totalRead * 100 / contentLength).toInt()
                            listener.onProgress(progress)
                        }
                    }
                    outputStream.flush()
                    listener.onDownloadComplete()
                } catch (e: Exception) {
                    listener.onError(e)
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        })
    }
}
