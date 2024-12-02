package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
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
    private lateinit var bakeground:ImageView
    private lateinit var WORD: Word_s


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.study_activity)


        bakeground=findViewById(R.id.backgroundImage)
        loadImageFromInternalStorage()//显示背景



        val intent = Intent(this, MyService::class.java)
        stopService(intent)

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false).apply()

        startTime = System.currentTimeMillis()// 记录应用启动的时间戳

        val sharedPreferences3 = getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor_id = sharedPreferences3.edit()
        val Goal=sharedPreferences3.getInt("goalId",10)
        val Studied=sharedPreferences3.getInt("studiedId",1)
        Log.d("goal",Goal.toString())

        val WordText:TextView=findViewById(R.id.Word_text)
        val GoalNUM:TextView=findViewById(R.id.GoalNUM)
        val StudyNUM:TextView=findViewById(R.id.StudyNUM)
        val s1:TextView=findViewById(R.id.s1)
        val lastWord:Button=findViewById(R.id.last_word)
        val Studybutton:Button=findViewById(R.id.nextWord)
        val WordDatabutton:Button=findViewById(R.id.ShowWordDate)


        val dbHelper=WordDatabaseHelper(applicationContext)
        GoalNUM.setText(Goal.toString())
        StudyNUM.setText(dbHelper.getWordsLearn().size.toString())
        lastWord.setVisibility(View.GONE)

        var wordList:MutableList<Word_s>
        var O_Num=0

        if(Goal<=Studied||dbHelper.getWordsByIdAndLearn(Studied,Goal).isEmpty()) {
             wordList = dbHelper.getWordsByIdAndLearn(
                Studied,Studied+3
            ).toMutableList()
        }else{
             wordList = dbHelper.getWordsByIdAndLearn(Studied, Goal).toMutableList()
        }
        Log.d("LIST","${wordList}")
        Log.d("studiedId",sharedPreferences3.getInt("studiedId",0).toString())
        Log.d("goalId",sharedPreferences3.getInt("goalId",0).toString())
        try{
            O_Num=Studied+wordList.size
            WORD=wordList[0]
        }catch (_:Exception){
            finish()
        }



        WordText.setText(WORD.word)
        OKHttpRequestVoice(WORD.word)
        WordDatabutton.setOnClickListener{
            try{

                val wordId=WORD.id.toInt()//因为点击下一个单词后，先显示单词再让studiedId+1，所以要对获取的wordId-1
                val intent= Intent(this,WordData::class.java)
                intent.putExtra("key",wordId)
                startActivity(intent)

                val word=dbHelper.getWordById((wordId).toString())
                OKHttpRequestVoice(word)
                dbHelper.incrementErrorCount(wordId)
                val WORD1=dbHelper.getWord_sById(wordId.toString())
                if (WORD1 != null) {
                    wordList.add(WORD1)
                }
                Log.d("DATA","${wordList}")
                Log.d("error","${word} is ${dbHelper.getErrorCount(wordId)}")
            }catch (e:Exception){

            }

        }
        var last_word=""
        Studybutton.setOnClickListener{
            editor_id.putBoolean("stu",true)
            Log.d("learn",dbHelper.getWordsLearn().size.toString())
            try {
                val chinese=obtainChinese(dbHelper.getTranslationById(WORD.id.toString()).toString())
                last_word=WORD.word
                wordList.remove(WORD)
                dbHelper.incrementLearn(WORD.id.toInt())
                WORD=wordList[0]
                val wordId=WORD.id.toInt()
                Log.d("id",wordId.toString())
                lastWord.setVisibility(View.VISIBLE)
                lastWord.setText("${last_word} ${chinese}")
                val word=dbHelper.getWordById(wordId.toString())
                WordText.setText(word)
                OKHttpRequestVoice(word)
                editor_id.putInt("studiedId", dbHelper.getWordsLearn().size)
                editor_id.apply()
                Log.d("studiedId", sharedPreferences3.getInt("studiedId", 1).toString())
                if(sharedPreferences3.getBoolean("summary",true)){
                    StudyNUM.setText((O_Num-wordList.size).toString())
                }
                else
                {
                    StudyNUM.setText(sharedPreferences3.getInt("studiedId", 1).toString())
                }

                /*dbHelper.incrementLearn(wordId)*/
                Log.d("STUDY","${wordList}")
                //如果达成了学习目标


            }catch (e:Exception){
                val wordId=WORD.id.toInt()
                if(wordList.isNotEmpty()) {
                    editor_id.putInt("studiedId", sharedPreferences3.getInt("studied",0) + 1)
                    editor_id.apply()
                    Log.d("studiedId", sharedPreferences3.getInt("studiedId", 1).toString())
                    StudyNUM.setText(sharedPreferences3.getInt("studied",0).toString())

                }else {
                    if(sharedPreferences3.getBoolean("summary",true)) {
                        editor_id.putBoolean("summary", false).apply()

                        endTime = System.currentTimeMillis()// 记录应用暂停或退出的时间戳
                        val duration1 = endTime - startTime
                        val intent = Intent(this, SummaryActivity::class.java)//学习新单词的时长
                        intent.putExtra("Time", duration1)
                        startActivity(intent)
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("恭喜你完成了今天的目标")
                        builder.setMessage("您确定要继续学习吗？")
                        builder.setPositiveButton("继续学") { dialog, which ->
                            editor_id.putInt(
                                "studiedId",
                                sharedPreferences3.getInt("goalId", 20) + sharedPreferences3.getInt(
                                    "studiedId", 0) + 1
                            )

                            Log.d("studiedId", sharedPreferences3.getInt("studiedId", 1).toString())
                            StudyNUM.setText(wordId.toString())
                            recreate()
                        }
                        builder.setNegativeButton("休息一下把") { dialog, which ->
                            val intent = Intent(this, FragmentActivity::class.java)
                            startActivity(intent)
                            dialog.dismiss()
                            finish()
                        }
                        builder.create().show()
                    }else{

                        wordList = dbHelper.getWordsByIdAndLearn(
                            sharedPreferences3.getInt("studiedId", 0),sharedPreferences3.getInt("studiedId", 0)+3
                        ).toMutableList()
                    }
                }
            }


        }
        /*sendRequestWithOkHttp()*/
        lastWord.setOnClickListener {

            val wordId=dbHelper.getIdByWord(last_word)
            val dbHelper=WordDatabaseHelper(applicationContext)
            val last_word=dbHelper.getWordById((wordId).toString())
            Log.d("last",(wordId).toString())
            val intent= Intent(this,WordData::class.java)
            if (wordId != null) {
                intent.putExtra("key",wordId.toInt())
            }
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
        val today_time=sharedPreferences3.getLong("TodayTime",0)
        editor_id.putLong("Time",duration+time)
        editor_id.putLong("TodayTime",duration+today_time)
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

    //解析翻译得到的JSON字符串，获取中文翻译
    private fun obtainChinese(jsonString: String): List<String> {
        val gson=Gson()
        val jsonResponse=gson.fromJson(jsonString,JsonResponse::class.java)
        return jsonResponse.data.entries.map { it.explain }

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

