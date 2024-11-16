package com.example.wordapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyService : Service() {


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        GlobalScope.launch(Dispatchers.Main) {
            delay(2000) // 延迟 2 秒
            // 延迟后执行的任务
            alarm()
        }
        // 返回START_STICKY表示服务结束后如果系统杀掉，系统会自动重启服务
        return START_STICKY
    }
    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()



        Log.d("MyService", "onCreate executed")

        GlobalScope.launch(Dispatchers.Main) {
            delay(2000) // 延迟 2 秒
            // 延迟后执行的任务
            alarm()
        }
    }
    @SuppressLint("ForegroundServiceType")
    private fun alarm(){
        val sharedPreferences2=getSharedPreferences("service",Context.MODE_PRIVATE)
        val sharedPreferences=getSharedPreferences("wordId", Context.MODE_PRIVATE)
        val goalId=sharedPreferences.getInt("goalId",0)
        val studiedId=sharedPreferences.getInt("studiedId",0)
        if( studiedId<goalId && sharedPreferences2.getBoolean("FA",false)) {

            Log.d("MyService", "${sharedPreferences2.getBoolean("FA",false)}")
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "my_service", "前台Service通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
            val intent = Intent(this, FirstActivity::class.java)
            val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "my_service")
                .setContentTitle("每日任务")
                .setContentText("今天的单词任务还没有完成哦")
                .setSmallIcon(R.drawable.book)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.task))
                .setContentIntent(pi)

                .build()
            startForeground(1, notification)
        }
    }
}