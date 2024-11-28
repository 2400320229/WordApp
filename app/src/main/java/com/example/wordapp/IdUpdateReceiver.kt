package com.example.wordapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext

class IdUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val goal=20//学习目标
        val sharedPreferences = context?.getSharedPreferences("wordId", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        //每天重置复习单词的数据库

        val date=sharedPreferences?.getInt("date", 0) ?: 0

        // 获取当前的 Id，默认值为 0
        val currentId = sharedPreferences?.getInt("studiedId", 0) ?: 0

        // 将 Id 增长 20
        val newId = currentId + goal

        // 更新保存新的 Id
        editor?.putInt("date",date+1)
        editor?.putInt("goalId", newId)
        editor?.apply()

        // 可选：打印日志或者其他操作
        Log.d("IdUpdateReceiver", "ID Updated to: $newId")
    }
}