package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.File
import java.io.FileOutputStream

class ReviewActivity : AppCompatActivity() {
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var bakeground: ImageView
    private lateinit var DATE:String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review)
        bakeground=findViewById(R.id.backgroundImage_review)
        loadImageFromInternalStorage()
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        startTime = System.currentTimeMillis()// 记录应用启动的时间戳

        val dbHelper=WordDatabaseHelper(applicationContext)
        var wordList = mutableListOf<Word>()
        if (this.intent.getStringExtra("WordList")=="Today"){
            wordList = dbHelper.getTodayErrorWord().toMutableList()
            DATE="今天"
        }
        if (this.intent.getStringExtra("WordList")=="Before"){
            wordList = dbHelper.getYesterdayErrorWord().toMutableList()
            DATE="昨天"
        }
        if(sharedPreferences.getInt("ReViewBefore",0)==1){
            wordList = dbHelper.getBeforeErrorWord().toMutableList()
            DATE="以前"
        }





        if (wordList.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("还没有单词哦")
            builder.setPositiveButton("去学习！") { dialog, which ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("先等会") { dialog, which ->

                finish()
            }
            builder.create().show()
        }


        val WordText: TextView = findViewById(R.id.Word_text)
        val Studybutton: Button = findViewById(R.id.nextWord)
        val WordDatabutton: Button = findViewById(R.id.ShowWordDate)
        val lastWordButton:MaterialButton = findViewById(R.id.last_word)
        val remain:TextView = findViewById(R.id.remainNum)
        val BackButton: ImageButton =findViewById(R.id.back)

        BackButton.setOnClickListener {
            finish()
        }
        lastWordButton.setVisibility(View.GONE)

        try{

            remain.setText(wordList.size.toString())
            WordText.setText(wordList[0].word)
            OKHttpRequestVoice(wordList[0].word)


        }catch (e:Exception){

            Log.d("e","e")
        }


        WordDatabutton.setOnClickListener {
            try {
                val wordId: Long? =dbHelper.getIdByWord(wordList[0].word)
                val wordId1:Int= wordId!!.toInt()
                val intent = Intent(this, WordData::class.java)
                Log.d("DateId",wordId.toString())
                intent.putExtra("key", wordId1)
                startActivity(intent)
                val dbHelper = WordDatabaseHelper(applicationContext)
                val word = dbHelper.getWordById(wordId.toString())
                Log.d("word",word.toString())
                OKHttpRequestVoice(word)
                try{
                    if(wordList[wordList.size-1].word!=word) {
                        wordList.add(Word(word.toString(),wordList[0].translation))
                    }
                }catch (e:Exception){
                    wordList.add(Word(word.toString(),wordList[0].translation))
                }
            }catch (e:Exception){

            }





        }
        Studybutton.setOnClickListener {
            try{

                val dbHelper = WordDatabaseHelper(applicationContext)
                var last_word = wordList[0].word
                wordList.remove(wordList[0])
                val word=wordList[0].word
                WordText.setText(word)
                OKHttpRequestVoice(word)
                try{
                    remain.setText((wordList.size).toString())
                    val chinese=obtainChinese(dbHelper.getTranslationById(dbHelper.getIdByWord(last_word).toString()).toString())
                    lastWordButton.setVisibility(View.VISIBLE)
                    lastWordButton.setText("${last_word} ${chinese}")
                    lastWordButton.setOnClickListener {
                        val last_wordId=dbHelper.getIdByWord(last_word)
                        OKHttpRequestVoice(last_word)
                        Log.d("LastId",last_wordId.toString())
                        val intent = Intent(this, WordData::class.java)
                        if (last_wordId != null) {
                            intent.putExtra("key", last_wordId.toInt())
                        }
                        startActivity(intent)
                    }
                }catch (e: Exception) {
                    lastWordButton.setVisibility(View.GONE)
                }


                Log.d("word",wordList.toString())



            }catch (e:Exception){
                if(this.intent.getStringExtra("WordList")=="Today"){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("太有实力了！")
                    builder.setMessage("${DATE}的单词已经被你复习完了")
                    builder.setPositiveButton("复习之前的单词") { dialog, which ->
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    builder.setNegativeButton("再复习一遍") { dialog, which ->
                        recreate()
                    }

                    builder.create().show()
                }
                if(this.intent.getStringExtra("WordList")=="Before"
                    &&sharedPreferences.getInt("ReViewBefore",0)==1){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("太有实力了！")
                    builder.setMessage("${DATE}的单词已经被你复习完了")
                    builder.setPositiveButton("复习昨天的单词") { dialog, which ->
                        editor.putInt("ReViewBefore",0).apply()
                        recreate()
                    }
                    builder.setNegativeButton("再复习一遍") { dialog, which ->
                        recreate()
                    }

                    builder.create().show()
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("太有实力了！")
                    builder.setMessage("${DATE}的单词已经被你复习完了")
                    builder.setPositiveButton("复习之前的单词") { dialog, which ->
                        editor.putInt("ReViewBefore",1).apply()
                        recreate()
                    }
                    builder.setNegativeButton("再复习一遍") { dialog, which ->
                        recreate()
                    }

                    builder.create().show()
                }



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
    override fun onStop() {
        super.onStop()

        endTime = System.currentTimeMillis()// 记录应用暂停或退出的时间戳
        val duration = endTime - startTime// 计算应用的打开时长
        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()
        val time=sharedPreferences3.getLong("Time",0)
        val today_time=sharedPreferences3.getLong("TodayTime",0)
        editor_id.putLong("Time",duration+time)
        editor_id.putLong("TodayTime",duration+today_time)
        editor_id.apply()
    }
    private fun OKHttpRequestVoice(Word: String?) {
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://dict.youdao.com/dictvoice?audio=${Word}")
                    .build()
                val response = client.newCall(request).execute()

                val responseData = response.body?.byteStream()// 获取音频

                // 将音频流写入临时文件
                if (responseData != null) {
                    val tempFile = File.createTempFile("audio", ".mp3", this.cacheDir)
                    val outputStream = FileOutputStream(tempFile)
                    responseData.copyTo(outputStream)
                    outputStream.close()

                    runOnUiThread {
                        if (responseData != null) {
                            try {
                                val mediaPlayer = MediaPlayer()

                                // 直接使用 InputStream 作为数据源
                                mediaPlayer.setDataSource(tempFile.absolutePath)
                                mediaPlayer.prepare()
                                mediaPlayer.start()

                            } catch (e: Exception) {
                                Log.e("http", "Error playing audio: ${e.message}")
                            }
                        } else {
                            Log.e("http", "Failed to get audio stream")
                        }
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("http", "Request failed: ${e.message}")
                }
            }
        }.start()
    }
    // 从内部存储加载图片
    private fun loadImageFromInternalStorage() {
        try {
            val file = File(filesDir, "selected_image.jpg")
            if (file.exists()) {
                Log.d("MainActivity", "File exists: ${file.absolutePath}")
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bakeground.setImageBitmap(bitmap)
            } else {
                Log.d("MainActivity", "File does not exist.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}