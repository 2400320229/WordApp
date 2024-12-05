package com.example.wordapp

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream


@Suppress("NAME_SHADOWING")
class WordData : AppCompatActivity() {
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var bakeground: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startTime = System.currentTimeMillis()
        enableEdgeToEdge()
        setContentView(R.layout.activity_word_data)

        bakeground=findViewById(R.id.background)
        loadImageFromInternalStorage()
        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        Log.d("DataWord",sharedPreferences.getBoolean("FA",true).toString())
        var Word="word"
        var chinese="chinese"
        val dbHelper = WordDatabaseHelper(applicationContext)

        val KnowButton:Button=findViewById(R.id.well_know)
        val VoiceButton: ImageButton =findViewById(R.id.play_voice)
        val addButton:Button=findViewById(R.id.add_mistake_word)

        val WordText:TextView=findViewById(R.id.Word)
        val TranslateText:TextView=findViewById(R.id.Chinese)

        val wordId=intent.getIntExtra("key",-1)
        Log.d("DataWord",wordId.toString())
        //如果是从单词本进入的WordData，wordId为-1，就不会显示添加至单词本的按钮
        if(wordId!=-1) {
            Word = dbHelper.getWordById(wordId.toString()).toString()
            val Translation = dbHelper.getTranslationById(wordId.toString())
            chinese = obtainChinese(Translation.toString()).toString()
        }else{
            addButton.setVisibility(View.GONE)
            Word = intent.getStringExtra("word").toString()
            chinese=intent.getStringExtra("chinese").toString()
        }
        //显示数据
        WordText.setText(Word)
        TranslateText.setText(chinese)
        //监听按钮
        addButton.setOnClickListener{
            try {
                val dbHelper=WordDatabaseHelper(applicationContext)
                dbHelper.incrementStar(wordId)
                Toast.makeText(this,"添加成功",Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                Log.e("addWord", e.toString())
                Toast.makeText(this,"添加失败",Toast.LENGTH_SHORT).show()
            }
        }
        VoiceButton.setOnClickListener{
            OKHttpRequestVoice(Word)
        }
        KnowButton.setOnClickListener{
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
    }
    override fun onPause() {
        super.onPause()

        endTime = System.currentTimeMillis()// 记录应用暂停或退出的时间戳
        val duration = endTime - startTime// 计算应用的打开时长
        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()
        val time=sharedPreferences3.getLong("Time",0)
        editor_id.putLong("Time",duration+time)
        editor_id.apply()

        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true)
        editor.apply()
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
                        try {
                            val mediaPlayer = MediaPlayer()

                            // 直接使用 InputStream 作为数据源
                            mediaPlayer.setDataSource(tempFile.absolutePath)
                            mediaPlayer.prepare()
                            mediaPlayer.start()

                        } catch (e: Exception) {
                            Log.e("http", "Error playing audio: ${e.message}")
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