package com.example.wordapp
//2024绘蓝杯参数作品
//2400320229
//张宇
/*基础功能：
1. 实现登录注册（都有相对应的页面）和记住密码功能，已经登录过再次进入
应用时跳过登录。
2. 登录后主页要求为进入学习单词的入口。
3. 点击学习单词后会进入学习单词功能模块，学习当日的单词任务。
4. 学习单词时能够播放单词的读音
5. 对学习过的单词的各种数据进行记录，并在合适位置展示数据统计信息。
6. 没完成当日任务时，应用通知用户去完成。*/
//
/*拓展功能：
1. 每日签到打卡功能，并记录每日学习时长。
2. 可以自定义学习单词页面的背景
3. 能够搜索查询陌生单词。
4. 能够复习前一天学习过的单词。
5. 自由发挥。*/
//

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import android.Manifest

class FirstActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frist_layout)

        requestExactAlarmPermissionIfNeeded()

        val intent = Intent(this, MyService::class.java)
        stopService(intent)

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()

        setupDailyAlarm()
        var id=sharedPreferences3.getInt("goalId",0)
        Log.d("goalId","${id}")


        val checkBox=findViewById<CheckBox?>(R.id.checkbox)
        val sharedPreferences2 = getSharedPreferences("Check", Context.MODE_PRIVATE )
        val editor_check = sharedPreferences2.edit()
        val check=sharedPreferences2.getBoolean("auto_login",false)
        Log.d("Check", check.toString())


        if(check){
            val intent0 = Intent(this,FragmentActivity::class.java)
            startActivity(intent0)
            finish()
        }

        val button2: Button =findViewById(R.id.button2)
        button2.setOnClickListener {

            val intent1 = Intent(this, zhu_ce::class.java)
            startActivity(intent1)

        }

        val button1:Button=findViewById(R.id.button1)
        val editText1: EditText =findViewById(R.id.e1)
        val editText2: EditText =findViewById(R.id.e2)

        button1.setOnClickListener {
            val sharedPreferences1 = getSharedPreferences("User", Context.MODE_PRIVATE)
            val inputTextUserName = editText1.text.toString().trim()
            val inputTextPassWord = editText2.text.toString().trim()

            if(inputTextUserName.isEmpty()&&inputTextPassWord.isEmpty() ){
                    Toast.makeText(this,"请输入用户名和密码",
                        Toast.LENGTH_SHORT).show()
            }else if(inputTextPassWord==sharedPreferences1.getString(inputTextUserName,"")) {

                val intent0 = Intent(this,FragmentActivity::class.java)
                startActivity(intent0)

                if(checkBox.isChecked) {
                    editor_check.putBoolean("auto_login", true)
                    editor_check.apply()
                }

                Toast.makeText(
                    this, "登录成功",
                    Toast.LENGTH_SHORT
                ).show()//toast 在点击这个button时可以提示引号中的内容
                finish()
            }
            else {
                Toast.makeText(this,"密码或用户名输入错误",Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true)
        editor.apply()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setupDailyAlarm() {

        // 检查是否已经获得了精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isExactAlarmPermissionGranted()) {
            // 提示用户启用精确闹钟权限
            requestExactAlarmPermission()
            return
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 设置触发时间为今天的 23:59
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        Log.d("FirstActivity", "Alarm will trigger at: ${calendar.time}")

// 如果设置的时间已经过去，则设置为第二天
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            // 设置为第二天的 0:00
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        Log.d("FirstActivity", "Alarm will trigger at: ${calendar.time}")

        // 创建一个 Intent 指向广播接收器
        val intent = Intent(this, IdUpdateReceiver::class.java)

        // 创建一个 PendingIntent，用于触发广播
        val pendingIntent = PendingIntent.getBroadcast(this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)//FLAG_IMMUTABLE保证PendingIntent不可修改

        // 使用 setExactAndAllowWhileIdle 设置精确的定时任务
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // 使用 RTC_WAKEUP，确保即使设备休眠时也能触发
            calendar.timeInMillis,   // 初始触发时间
            pendingIntent            // 触发时要执行的 PendingIntent
        )

        Log.d("FirstActivity", "AlarmManager set for daily update at 1:00")
        Log.d("FirstActivity", "Alarm will trigger at: ${System.currentTimeMillis()}")
    }
    private fun requestExactAlarmPermissionIfNeeded() {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val permissionRequested = sharedPreferences.getBoolean("exact_alarm_permission_requested", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isExactAlarmPermissionGranted() && !permissionRequested) {
            // 提示用户启用精确闹钟权限
            requestExactAlarmPermission()
        }
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permission = Manifest.permission.SCHEDULE_EXACT_ALARM
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 在 Android 12 以下不需要此权限
        }
    }

    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity(intent)

        // 标记请求过权限，以避免重复请求
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("exact_alarm_permission_requested", true).apply()
    }

}