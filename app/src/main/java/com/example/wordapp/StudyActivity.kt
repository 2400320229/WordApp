package com.example.wordapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

data class WordResponse(
    val total: Int,
    val list: List<String>
)

class MainActivity : AppCompatActivity() {
    private var wordId=3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.study_activity)
        val WordText:TextView=findViewById(R.id.Word_text)
        val Studybutton:Button=findViewById(R.id.nextWord)
        Studybutton.setOnClickListener{


           /* sendRequestWithOkHttp()*///一劳永逸
            val dbHelper=WordDatabaseHelper(applicationContext)
            val word=dbHelper.getWordById(wordId)
            WordText.setText(word)

            /*if (word != null) {
                Log.d("Word", "The word with ID $wordId is: $word")
            } else {
                Log.d("Word", "No word found with ID $wordId")
            }*/
            wordId+=1
        }


        /*sendRequestWithOkHttp()*/
    }
    private fun OKHttpRequestVoice(Word:String?){
        Thread{
            try {
                val client=OkHttpClient()
                val request= Request.Builder()
                    .url("http://dict.youdao.com/dictvoice?audio=${Word}")
                    .build()
                val response=client.newCall(request).execute()
                val responseData=response.body?.string()
                val gson=Gson()
                val voiceResponse=gson.fromJson(responseData,WordResponse::class.java)


            }catch (e:Exception){
                runOnUiThread {
                    Log.e("http", "Request failed: ${e.message}")
                }
            }
        }.start()
    }
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

}

