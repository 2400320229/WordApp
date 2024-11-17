package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager.OnActivityStopListener
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

    private var startTime: Long = 0
    private var endTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.study_activity)
        val intent = Intent(this, MyService::class.java)
        stopService(intent)

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
        startTime = System.currentTimeMillis()// 记录应用启动的时间戳

        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()
        val Goal=sharedPreferences3.getInt("goalId",10)
        Log.d("goal",Goal.toString())

        val WordText:TextView=findViewById(R.id.Word_text)
        val GoalNUM:TextView=findViewById(R.id.GoalNUM)
        val StudyNUM:TextView=findViewById(R.id.StudyNUM)
        val s1:TextView=findViewById(R.id.s1)
        val lastWord:Button=findViewById(R.id.last_word)
        val Studybutton:Button=findViewById(R.id.nextWord)
        val WordDatabutton:Button=findViewById(R.id.ShowWordDate)

        GoalNUM.setText(Goal.toString())

        WordDatabutton.setOnClickListener{
            val studyId=sharedPreferences3.getInt("studiedId",1)
            val dbHelper1=MistakeWordIDDatabaseHelper(applicationContext)
            val wordId=studyId-1//因为点击下一个单词后，先显示单词再让studiedId+1，所以要对获取的wordId-1
            dbHelper1.insertWordId(wordId)//将不认识的单词加入Mistake数据库中，可以复习
            val intent= Intent(this,WordData::class.java)
            intent.putExtra("key",wordId)
            startActivity(intent)
            Log.d("AllmistakeId",dbHelper1.getAllWords().toString())
            val dbHelper=WordDatabaseHelper(applicationContext)
            val word=dbHelper.getWordById((wordId).toString())
            OKHttpRequestVoice(word)
            dbHelper.incrementErrorCount(wordId)
            Log.d("error","${word} is ${dbHelper.getErrorCount(wordId)}")



        }
        Studybutton.setOnClickListener{


            val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
            val editor_id = sharedPreferences3.edit()
            val studyId=sharedPreferences3.getInt("studiedId",1)?:1
            val wordId=studyId
            Log.d("id",wordId.toString())

            //sendRequestWithOkHttp()一劳永逸
            val dbHelper=WordDatabaseHelper(applicationContext)

            //让lastWord的文本为last_word
            val last_word=dbHelper.getWordById((wordId-1).toString())
            lastWord.setText(last_word)

            val word=dbHelper.getWordById(wordId.toString())
            WordText.setText(word)
            OKHttpRequestVoice(word)

            //如果达成了学习目标
            if(wordId!=Goal+1) {
                editor_id.putInt("studiedId", wordId + 1)
                editor_id.apply()
                Log.d("studiedId", sharedPreferences3.getInt("studiedId", 1).toString())
                StudyNUM.setText(wordId.toString())
            }else{

                val builder= AlertDialog.Builder(this)
                builder.setTitle("恭喜你完成了今天的目标")
                builder.setMessage("您确定要继续学习吗？")
                builder.setPositiveButton("继续学") {dialog,which->
                    editor_id.putInt("studiedId", wordId + 1)
                    editor_id.apply()
                    Log.d("studiedId", sharedPreferences3.getInt("studiedId", 1).toString())
                    StudyNUM.setText(wordId.toString())
                    val intent=Intent(this,SummaryActivity::class.java)
                    startActivity(intent)
                }
                builder.setNegativeButton("去看看其他科目把"){dialog,which->
                    val intent=Intent(this,FragmentActivity::class.java)
                    startActivity(intent)
                    dialog.dismiss()
                }
                builder.create().show()
            }
        }
        /*sendRequestWithOkHttp()*/
        lastWord.setOnClickListener {
            val studyId=sharedPreferences3.getInt("studiedId",1)?:1
            val wordId=studyId
            val dbHelper=WordDatabaseHelper(applicationContext)
            val last_word=dbHelper.getWordById((wordId-2).toString())
            val intent= Intent(this,WordData::class.java)
            intent.putExtra("key",wordId-2)//不知道为什么就减二了
            startActivity(intent)
            OKHttpRequestVoice(last_word)

        }
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

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
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
                        var word=dbHelper.getWordById(id.toString())
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

