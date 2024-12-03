package com.example.wordapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class FirstActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frist_layout)

        val intent = Intent(this, MyService::class.java)
        stopService(intent)

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()
        /*editor_id.putInt("goalId",1)
        editor_id.putInt("studiedId",1)
        editor_id.apply()*/
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

    private fun setupDailyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 设置触发时间为今天的 0:00
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
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
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // 设置 AlarmManager 每天 0:00 触发广播
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, // 使用 RTC_WAKEUP，确保即使设备休眠时也能触发
            calendar.timeInMillis,   // 初始触发时间
            AlarmManager.INTERVAL_DAY, // 每天间隔
            pendingIntent            // 触发时要执行的 PendingIntent
        )

        Log.d("FirstActivity", "AlarmManager set for daily update at 1:00")
        Log.d("FirstActivity", "Alarm will trigger at: ${System.currentTimeMillis()}")
    }


   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        return true
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_item->Toast.makeText(this,"you clicked add",
                Toast.LENGTH_SHORT).show()
            R.id.remove_item->Toast.makeText(this,"you clicked remove",
                Toast.LENGTH_SHORT).show()
        }
        return true
    }*/
}