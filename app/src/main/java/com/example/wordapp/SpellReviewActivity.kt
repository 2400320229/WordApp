package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.File
import java.io.FileOutputStream

class SpellReviewActivity : AppCompatActivity() {
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var bakeground: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spell_review)
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

                finish()
            }
            builder.create().show()
        }


        val WordText: TextView = findViewById(R.id.Word_text)
        val lastWordButton:MaterialButton = findViewById(R.id.last_word)
        val remain:TextView = findViewById(R.id.remainNum)
        val NoiceButton:Button=findViewById(R.id.ShowWordDate)
        val Spell:EditText=findViewById(R.id.spell)

        lastWordButton.setVisibility(View.GONE)

        try{

            remain.setText(wordList.size.toString())
            WordText.setText(obtainChinese(wordList[0].translation.toString()).toString())


        }catch (e:Exception){

            Log.d("e","e")
        }

        Spell.addTextChangedListener {
            if (wordList.isNotEmpty()) {
                val spell = Spell.text.toString()
                if (spell.length == wordList[0].word.length) {
                    //拼对了
                    if (spell == wordList[0].word) {

                        Spell.text.clear()

                        try {

                            val last_word=wordList[0].word
                            wordList.remove(wordList[0])
                            remain.setText(wordList.size.toString())
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
                            if (wordList.isNotEmpty()) {
                                WordText.setText(obtainChinese(wordList[0].translation.toString()).toString())
                            } else {
                                Toast.makeText(this,"复习完成",Toast.LENGTH_SHORT).show()
                                finish()
                            }


                        } catch (_: Exception) {

                        }

                    }
                    //拼错了
                    else {
                        Spell.text.clear()
                        try {
                            val wordId: Long? = dbHelper.getIdByWord(wordList[0].word)
                            val wordId1: Int = wordId!!.toInt()
                            val intent = Intent(applicationContext, WordData::class.java)
                            Log.d("DateId", wordId.toString())
                            intent.putExtra("key", wordId1)
                            startActivity(intent)
                            val dbHelper = WordDatabaseHelper(applicationContext)
                            val word = dbHelper.getWordById(wordId.toString())
                            Log.d("word", word.toString())
                            OKHttpRequestVoice(word)
                            try {

                                if(wordList[wordList.size-1].word!=word){
                                    wordList.add(wordList[0])
                                }
                            } catch (e: Exception) {

                            }
                            Log.d("wordlistSize","${wordList.size}")
                        } catch (e: Exception) {

                        }
                    }
                }

                try{
                    for (i in spell.indices) {
                        if (spell[i] == wordList[0].word[i]) {
                            Spell.setTextColor(Color.GREEN)
                        } else {
                            Spell.setTextColor(Color.RED)
                        }
                    }
                }catch (_:Exception){
                    Spell.text.clear()
                }


            }else{
                finish()
            }
        }

        NoiceButton.setOnClickListener {
            val word=wordList[0].word
            OKHttpRequestVoice(word)
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