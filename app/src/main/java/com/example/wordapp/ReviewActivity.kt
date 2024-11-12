package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review)
        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE)
        val editor_id = sharedPreferences3.edit()
        var mistakeId = 0

        val WordText: TextView = findViewById(R.id.Word_text)
        val Trybutton: Button = findViewById(R.id.Try)
        val Studybutton: Button = findViewById(R.id.nextWord)
        val WordDatabutton: Button = findViewById(R.id.ShowWordDate)
        Trybutton.setOnClickListener {
            val intent = Intent(this, Watch_Mistake_Word::class.java)
            startActivity(intent)
        }
        WordDatabutton.setOnClickListener {

            val wordId1 = sharedPreferences3.getInt("${mistakeId-1}", 1)-1

            val wordId2 = sharedPreferences3.getInt("${mistakeId-1}", 1)
            val intent = Intent(this, WordData::class.java)
            intent.putExtra("key", wordId2)
            startActivity(intent)
            val dbHelper = WordDatabaseHelper(applicationContext)
            val word = dbHelper.getWordById(wordId1)
            Log.d("word",word.toString())
            OKHttpRequestVoice(word)


        }
        Studybutton.setOnClickListener {



            var wordId=sharedPreferences3.getInt("${mistakeId}",1)-1
            Log.d("review",wordId.toString())
            Log.d("mistakeId",mistakeId.toString())
            val dbHelper = WordDatabaseHelper(applicationContext)
            val word = dbHelper.getWordById(wordId)
            val translation = dbHelper.getTranslationById(wordId)
            WordText.setText(word)
            OKHttpRequestVoice(word)
            mistakeId+=1
        }

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