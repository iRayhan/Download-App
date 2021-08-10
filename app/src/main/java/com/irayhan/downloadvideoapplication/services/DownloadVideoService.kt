package com.irayhan.downloadvideoapplication.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.irayhan.downloadvideoapplication.networking.APIService
import com.irayhan.downloadvideoapplication.networking.RemoteDataSource
import com.irayhan.downloadvideoapplication.repository.DownloadVideoRepository
import com.irayhan.downloadvideoapplication.utils.DataState
import android.os.Environment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.irayhan.downloadvideoapplication.core.AppConstants
import com.irayhan.downloadvideoapplication.view.DownloadVideoActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.*


class DownloadVideoService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CoroutineScope(Dispatchers.IO).launch {
            downloadFile()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        stopSelf()
        return null
    }

    private suspend fun downloadFile() {

        val repository = DownloadVideoRepository(RemoteDataSource().buildApi(APIService::class.java))
        repository.getVideo().let {
            when (it) {
                is DataState.Success -> {
                    it.value.body()?.let { responseBody -> downloadFile(responseBody) }
                }

                is DataState.Loading -> {

                }

                is DataState.Error -> {
                    stopSelf()
                }
            }
        }
    }

    private fun downloadFile(body: ResponseBody) {
        try {
            var count: Int
            val data = ByteArray(1024 * 4)
            val fileSize = body.contentLength()
            val inputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
            val fileName = AppConstants.ENDPOINT_LINK.substring(AppConstants.ENDPOINT_LINK.lastIndexOf("/") + 1)
            val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            val outputStream = FileOutputStream(outputFile)
            var total: Long = 0
//            val totalFileSize = (fileSize / (1024.0.pow(2.0)));
            while (inputStream.read(data).also { count = it } != -1) {
                total += count.toLong()
                val progress = ((total * 100).toDouble() / fileSize.toDouble()).toInt()
                broadcastUpdate(progress)
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun broadcastUpdate(currentProgress: Int) {
        val intent = Intent(DownloadVideoActivity.PROGRESS_UPDATE).apply {
            putExtra("progress", currentProgress)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}