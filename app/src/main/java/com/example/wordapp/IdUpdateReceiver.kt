package com.example.wordapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IdUpdateReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context!=null){
            val dbHelper=WordDatabaseHelper(context)

            dbHelper.updateDayForLearnWords()


            val sharedPreferences = context.getSharedPreferences("wordId", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            val goal= sharedPreferences?.getInt("goalId",20)?:20
            val date=sharedPreferences?.getInt("date", 0) ?: 0
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // 自定义格式
            val formattedDate = currentDate.format(formatter)
            val today_study_time=sharedPreferences?.getLong("TodayTime",0)?:0
            if (sharedPreferences != null) {
                if(sharedPreferences.getBoolean("stu",false)){
                    dbHelper.insertCheckInRecord(1,today_study_time.toInt(),formattedDate.toString())
                }else{
                    dbHelper.insertCheckInRecord(0,today_study_time.toInt(),formattedDate.toString())
                }

            }
            // 获取当前的 Id，默认值为 0
            val currentId = sharedPreferences?.getInt("studiedId", 0) ?: 0

            // 将 Id 增长 20
            val newId = currentId + goal

            // 更新保存新的 Id
            editor?.putLong("TodayTime",0)//每日的学习时间归零
            editor?.putInt("date",date+1)//日期加一
            editor?.putInt("goalId", newId)
            editor?.putBoolean("summary",true)
            editor?.putBoolean("stu",false)
            editor?.apply()

            Log.d("before",dbHelper.getBeforeErrorWord().toString())
        }






    }
}