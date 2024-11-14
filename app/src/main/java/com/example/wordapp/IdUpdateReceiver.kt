package com.example.wordapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class IdUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val goal=20//学习目标
        val sharedPreferences = context?.getSharedPreferences("wordId", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        // 获取当前的 Id，默认值为 0
        val currentId = sharedPreferences?.getInt("studiedId", 0) ?: 0

        // 将 Id 增长 20
        val newId = currentId + goal

        // 更新保存新的 Id
        editor?.putInt("goalId", newId)
        editor?.apply()

        // 可选：打印日志或者其他操作
        Log.d("IdUpdateReceiver", "ID Updated to: $newId")
    }
}