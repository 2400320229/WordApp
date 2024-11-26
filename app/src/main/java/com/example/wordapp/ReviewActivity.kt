package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review)
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        startTime = System.currentTimeMillis()// 记录应用启动的时间戳

        val dbHelper=WordDatabaseHelper(applicationContext)
        var wordList = mutableListOf<String>()
        if (this.intent.getStringExtra("WordList")=="Today"){
            wordList = dbHelper.getTodayErrorWord().toMutableList()
        }
        if (this.intent.getStringExtra("WordList")=="Before"){
            wordList = dbHelper.getBeforeErrorWord().toMutableList()
        }





        if (wordList.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("今天还没有单词哦")
            builder.setMessage("请先开始今天的学习")
            builder.setPositiveButton("去学习！") { dialog, which ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("去看看其他科目把") { dialog, which ->

            }
            builder.create().show()
        }


        val WordText: TextView = findViewById(R.id.Word_text)
        val Studybutton: Button = findViewById(R.id.nextWord)
        val WordDatabutton: Button = findViewById(R.id.ShowWordDate)
        val lastWordButton:MaterialButton = findViewById(R.id.last_word)
        val remain:TextView = findViewById(R.id.remainNum)

        lastWordButton.setVisibility(View.GONE)

        try{

            remain.setText(wordList.size.toString())
            WordText.setText(wordList[0])
            OKHttpRequestVoice(wordList[0])


        }catch (e:Exception){

            Log.d("e","e")
        }


        WordDatabutton.setOnClickListener {
            try {
                val wordId: Long? =dbHelper.getIdByWord(wordList[0])
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
                    if(wordList[0]!=word) {
                        wordList.add(word.toString())
                    }
                }catch (e:Exception){
                    wordList.add(word.toString())
                }
            }catch (e:Exception){

            }





        }
        Studybutton.setOnClickListener {
            try{

                val dbHelper = WordDatabaseHelper(applicationContext)
                var last_word = wordList[0]
                wordList.remove(last_word)
                val word=wordList[0]
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
                val builder = AlertDialog.Builder(this)
                builder.setTitle("太有实力了！")
                builder.setMessage("今天的单词语句复习完了")
                builder.setPositiveButton("再去学习！") { dialog, which ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                builder.setNegativeButton("再复习一遍") { dialog, which ->
                    recreate()
                }
                builder.create().show()
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
        editor_id.putLong("Time",duration+time)
        editor_id.apply()
    }
    //解析翻译得到的JSON字符串，获取中文翻译
    private fun obtainChinese(jsonString: String): List<String> {
        val gson= Gson()
        val jsonResponse=gson.fromJson(jsonString,JsonResponse::class.java)
        return jsonResponse.data.entries.map { it.explain }

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

}