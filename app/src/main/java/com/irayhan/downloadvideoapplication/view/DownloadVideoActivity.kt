package com.irayhan.downloadvideoapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.irayhan.downloadvideoapplication.R
import com.irayhan.downloadvideoapplication.base.BaseActivity
import com.irayhan.downloadvideoapplication.databinding.ActivityDownloadVideoBinding
import com.irayhan.downloadvideoapplication.services.DownloadVideoService
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DownloadVideoActivity : BaseActivity<ActivityDownloadVideoBinding>() {

    companion object {
        const val ACTION_PROGRESS_UPDATE = "pu"
        private const val RC_PENDING_INTENT = 100
        private var willNotificationShow = false
    }

    override val contentView: Int get() = R.layout.activity_download_video

    private var channelId = "cid"
    private var channelName = "cname"
    private var downloadProgress = -1
    private var notificationId = 10
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun init(savedInstanceState: Bundle?) {

        // set notification
        setNotification()

        // set receiver
        registerReceiver()

        // btn download
        binding.btnDownload.setOnClickListener {
            if (downloadProgress in 0..99) Toast.makeText(this, "One download is in progress", Toast.LENGTH_SHORT).show()
            else checkPermission()
        }
    }

    override fun onResume() {
        willNotificationShow = false
        NotificationManagerCompat.from(this).cancel(notificationId)
        super.onResume()
    }

    override fun onStop() {
        willNotificationShow = true
        super.onStop()
    }

    private fun registerReceiver() {
        val broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_PROGRESS_UPDATE)
        }
        broadcastManager.registerReceiver(mBroadcastReceiver, intentFilter)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_PROGRESS_UPDATE) {
                downloadProgress = intent.getIntExtra("progress", 0)
                binding.progress.progress = downloadProgress
                binding.txtProgress.text = "Downloaded: $downloadProgress%"

                // if notification is running it will update in background
                if (willNotificationShow && downloadProgress >= 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        updateNotification()
                    }
                }

            }
        }
    }

    private fun setNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_baseline_notifications)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(true)
            .setProgress(100, downloadProgress, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun updateNotification() {
        notificationBuilder
            .setContentTitle(if (downloadProgress < 100) "Download in progress" else "Download Complete")
            .setContentText("Downloading: $downloadProgress%")
            .setProgress(100, downloadProgress, false)
            .setContentIntent(PendingIntent.getActivity(this, RC_PENDING_INTENT, intent, PendingIntent.FLAG_ONE_SHOT))
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun checkPermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        downloadFile()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun downloadFile() {
        val intent = Intent(this, DownloadVideoService::class.java)
        startService(intent)
        Toast.makeText(this, "Download Started...", Toast.LENGTH_SHORT).show()
    }

}