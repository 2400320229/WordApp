package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class FragmentActivity : AppCompatActivity() {

    private lateinit var progressBar:ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment)
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()



        progressBar = findViewById(R.id.progressBar)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment, StudyFragment())

        transaction.show(StudyFragment())
        transaction.commit()

        var selectIndex = R.id.tab1 // 默认选中的按钮ID
        val toggleGroup: MaterialButtonToggleGroup = findViewById(R.id.ToggleGroup)

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val button = findViewById<MaterialButton>(checkedId)

            if (isChecked) {
                Log.d("checked","isChecked")
                // 如果当前按钮被选中
                if (selectIndex != checkedId) {

                    // 恢复之前选中的按钮的颜色
                    val previousButton = findViewById<MaterialButton>(selectIndex)
                    previousButton.setStrokeColorResource(R.color.white)
                    previousButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                    previousButton.setIconTintResource(R.color.black)
                }

                // 更新选中的按钮
                selectIndex = checkedId
                button.setStrokeColorResource(R.color.black)
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.setIconTintResource(R.color.white)

                switchFragment(selectIndex)
            } else {
                // 如果按钮被取消选中
                button.setStrokeColorResource(R.color.white)
                /*button.setTextColor(ContextCompat.getColor(this, R.color.black))*/
            }
        }



        toggleGroup.check(R.id.tab1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val dbHelper=WordDatabaseHelper(applicationContext)
        if ( dbHelper.getWordById(10927.toString())
                ?.isEmpty() ?: true){
            Toast.makeText(this,"数据不完整，正在恢复，请稍等",Toast.LENGTH_SHORT).show()
            sendRequestWithOkHttp()
        }else if(dbHelper.getNullId().isNotEmpty()){//存在空的id
            Toast.makeText(this,"正在获取翻译，无需等待，开始你的学习吧",Toast.LENGTH_SHORT).show()
            OkHttpRequestTranslate()
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true)
        editor.apply()
        val intent=Intent(this,MyService::class.java)

        startService(intent)//关闭时启用service，进行通知
    }
    private fun switchFragment(selectIndex: Int) {
        val fragment = when (selectIndex) {
            R.id.tab1 -> StudyFragment()
            R.id.tab2 -> StarWordFragment()
            R.id.tab3 -> UserFragment()
            else -> throw IllegalArgumentException("Invalid tab selection")
        }
        showOrHideFragment(fragment)
    }

    private fun showOrHideFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        transaction.add(R.id.fragment,fragment)

            if(currentFragment!=null) {
                transaction.remove(currentFragment)
                transaction.replace(R.id.fragment, fragment)
            }else{
                transaction.replace(R.id.fragment, fragment)
                transaction.addToBackStack(null)
            }
        // 提交事务
        transaction.commit()
    }
    private fun HideFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        transaction.add(R.id.fragment,fragment)

        transaction.hide(fragment)
        // 提交事务
        transaction.commit()
    }
    private fun sendRequestWithOkHttp() {

        progressBar.visibility = View.VISIBLE
        HideFragment(StudyFragment())
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://cdn.jsdelivr.net/gh/lyc8503/baicizhan-word-meaning-API/data/list.json")
                    .build()

                val response = client.newCall(request).execute()
                // 使用response.body?.string()获取返回的内容
                val responseData = response.body?.string()

                val gson= Gson()
                val wordResponse =gson.fromJson(responseData,WordResponse::class.java)

                runOnUiThread{
                    Log.d("WordInfo", "Total words: ${wordResponse.total}")
                    Log.d("WordInfo", "First word in list: ${wordResponse.list[1]}")
                }
                // 存储数据到数据库
                val dbHelper = WordDatabaseHelper(applicationContext)

                // 使用事务来批量插入数据，提高效率
                val db = dbHelper.writableDatabase
                /*db.beginTransaction()*/

                try {
                    // 一次性批量插入所有单词
                    dbHelper.insertWords(wordResponse.list)

                    /*db.setTransactionSuccessful()*/  // 提交事务
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    /*db.endTransaction()*/  // 结束事务
                    db.close()
                    runOnUiThread{
                        progressBar.visibility=View.GONE
                        showOrHideFragment(StudyFragment())
                        OkHttpRequestTranslate()

                    }

                }
            } catch (e: Exception) {
                // 错误处理
                runOnUiThread {
                    Toast.makeText(this, "获取数据失败，正在尝试重新获取", Toast.LENGTH_LONG).show()
                    recreate()
                }
            }
        }.start() // 启动线程


    }
    //给单词添加翻译
    private fun OkHttpRequestTranslate() {

        Thread {
            try {

                /*db.beginTransaction()*/
                val dbHelper = WordDatabaseHelper(applicationContext)

                // 使用事务来批量插入数据，提高效率
                val db = dbHelper.writableDatabase

                val IdList=dbHelper.getNullId()
                Log.d("IdList",IdList.toString())
                val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
                val editor=sharedPreferences3.edit()
                try {

                    for (id in IdList){
                        var word=dbHelper.getWordById(id.toString())
                        if(dbHelper.getTranslationById(id.toString())==null){
                            val client = OkHttpClient()
                            val request = Request.Builder()
                                .url("http://dict.youdao.com/suggest?num=1&doctype=json&q=${word}")
                                .build()

                            val response = client.newCall(request).execute()
                            // 使用response.body?.string()获取返回的内容
                            val responseData = response.body?.string()

                            val gson= Gson()
                            val wordResponse =gson.fromJson(responseData,WordResponse::class.java)

                            // 存储数据到数据库

                            dbHelper.updateTranslationById(id.toInt(),responseData.toString())

                            Log.d("id",id.toString())
                            if((id%100).toInt() ==0){

                                runOnUiThread {
                                    Toast.makeText(this, "${id}", Toast.LENGTH_LONG).show()
                                    if(id.toInt() >10900){
                                        Toast.makeText(this, "数据完整", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }


                    }

                    /*db.setTransactionSuccessful()*/  // 提交事务
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    /*db.endTransaction()*/  // 结束事务
                    db.close()
                }
            } catch (e: Exception) {
                // 错误处理
                runOnUiThread {
                    Toast.makeText(this, "Request failed: ${e.message}", Toast.LENGTH_LONG).show()

                }
            }
        }.start() // 启动线程
    }

}