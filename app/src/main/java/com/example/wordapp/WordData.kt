package com.example.wordapp

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class WordData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var Word="word"
        var chinese="chinese"
        val dbHelper = WordDatabaseHelper(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_word_data)
        val KnowButton:Button=findViewById(R.id.well_know)
        val VoiceButton:Button=findViewById(R.id.play_voice)
        val addButton:Button=findViewById(R.id.add_mistake_word)

        val WordText:TextView=findViewById(R.id.Word)
        val TranslateText:TextView=findViewById(R.id.Chinese)


        val wordId=intent.getIntExtra("key",0)-1
        if(wordId!=-1) {
            Word = dbHelper.getWordById(wordId).toString()
            val Translation = dbHelper.getTranslationById(wordId)
            chinese = obtainChinese(Translation.toString()).toString()
        }else{
            addButton.setVisibility(View.GONE)
            Word = intent.getStringExtra("word").toString()
            chinese=intent.getStringExtra("chinese").toString()
        }
        //显示数据
        WordText.setText(Word)
        TranslateText.setText(chinese.toString())
        //监听按钮
        addButton.setOnClickListener{
            try {
                val dbHelper=MistakeWordDatabaseHelper(applicationContext)
                if (Word != null) {
                    dbHelper.insertWordAndTranslation(Word, chinese.toString())
                }
                Toast.makeText(this,"添加成功",Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                Log.e("addWord", e.toString())
            }
        }
        VoiceButton.setOnClickListener{
            OKHttpRequestVoice(Word)
        }
        KnowButton.setOnClickListener{
            finish()

        }
    }
    //解析翻译得到的JSON字符串，获取中文翻译
    private fun obtainChinese(jsonString: String): List<String> {
        val gson= Gson()
        val jsonResponse=gson.fromJson(jsonString,JsonResponse::class.java)
        return jsonResponse.data.entries.map { it.explain }

    }
    //请求单词音频并播放
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