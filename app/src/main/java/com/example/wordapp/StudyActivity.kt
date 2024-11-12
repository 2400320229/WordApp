package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.util.Log
import android.util.Pair
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



data class WordResponse(
    val total: Int,
    val list: List<String>
)

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.study_activity)
        val WordText:TextView=findViewById(R.id.Word_text)
        val Trybutton:Button=findViewById(R.id.Try)
        val Studybutton:Button=findViewById(R.id.nextWord)
        val WordDatabutton:Button=findViewById(R.id.ShowWordDate)
        Trybutton.setOnClickListener{
            val intent=Intent(this,Watch_Mistake_Word::class.java)
            startActivity(intent)
        }
        WordDatabutton.setOnClickListener{
            val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
            val editor_id = sharedPreferences3.edit()
            val wordId=sharedPreferences3.getInt("id",1)

            val intent= Intent(this,WordData::class.java)
            intent.putExtra("key",wordId)
            startActivity(intent)
            val dbHelper=WordDatabaseHelper(applicationContext)
            val word=dbHelper.getWordById(wordId-1)
            OKHttpRequestVoice(word)

        }
        Studybutton.setOnClickListener{

            val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
            val editor_id = sharedPreferences3.edit()
            val wordId=sharedPreferences3.getInt("id",1)
            Log.d("id",wordId.toString())


            //sendRequestWithOkHttp()一劳永逸
            val dbHelper=WordDatabaseHelper(applicationContext)
            val word=dbHelper.getWordById(wordId)
            val translation=dbHelper.getTranslationById(wordId)
            WordText.setText(word)
            OKHttpRequestVoice(word)

            /*if (word != null) {
                Log.d("Word", "The word with ID $wordId is: $word")
            } else {
                Log.d("Word", "No word found with ID $wordId")
            }*/
            editor_id.putInt("id",wordId+1)
            editor_id.apply()


        }




        /*sendRequestWithOkHttp()*/
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
    //录入数据
    private fun sendRequestWithOkHttp() {

        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://cdn.jsdelivr.net/gh/lyc8503/baicizhan-word-meaning-API/data/list.json")
                    .build()

                val response = client.newCall(request).execute()
                // 使用response.body?.string()获取返回的内容
                val responseData = response.body?.string()

                val gson=Gson()
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
                }
            } catch (e: Exception) {
                // 错误处理
                runOnUiThread {
                    Log.e("http", "Request failed: ${e.message}")
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

                try {
                    var id=1
                    while (id<=10927){
                        var word=dbHelper.getWordById(id)
                        val client = OkHttpClient()
                        val request = Request.Builder()
                            .url("http://dict.youdao.com/suggest?num=1&doctype=json&q=${word}")
                            .build()

                        val response = client.newCall(request).execute()
                        // 使用response.body?.string()获取返回的内容
                        val responseData = response.body?.string()

                        val gson=Gson()
                        val wordResponse =gson.fromJson(responseData,WordResponse::class.java)

                        // 存储数据到数据库


                        dbHelper.updateTranslationById(id,responseData.toString())

                        Log.d("id",id.toString())
                        if(id==10927){
                            Toast.makeText(this,"翻译成功",Toast.LENGTH_SHORT).show()
                        }
                        id+=1
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
                    Log.e("http", "Request failed: ${e.message}")
                }
            }
        }.start() // 启动线程
    }
    //解析翻译得到的JSON字符串，获取中文翻译
    private fun obtainChinese(jsonString: String): List<String> {
        val gson=Gson()
        val jsonResponse=gson.fromJson(jsonString,JsonResponse::class.java)
        return jsonResponse.data.entries.map { it.explain }

    }


}


data class JsonResponse(
    val result: Result,
    val data: Data
)

data class Result(
    val msg: String,
    val code: Int
)

data class Data(
    val entries: List<Entry>,
    val query: String,
    val language: String,
    val type: String
)

data class Entry(
    val explain: String,
    val entry: String
)

