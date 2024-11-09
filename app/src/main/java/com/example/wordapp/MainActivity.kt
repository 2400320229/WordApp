package com.example.wordapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var NUM=0
        setContentView(R.layout.activity_main)
        val Studybutton:Button=findViewById(R.id.redenglu)
        Studybutton.setOnClickListener{

           /* val intent0 = Intent(this,Study_Word::class.java)
            startActivity(intent0)*/

        }
        val next_wordbutton:Button=findViewById(R.id.nextWord)
        next_wordbutton.setOnClickListener{
            sendRequestWithOkHttp(NUM)
            NUM += 1
            if(NUM==20){
                removeEditF()
            }
        }


        /*sendRequestWithOkHttp()*/
    }
    private fun sendRequestWithOkHttp(Num: Int) {

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
                    Log.d("WordInfo", "First word in list: ${wordResponse.list[Num]}")
                }
                /*val bundle=Bundle().apply {
                    putString("word_key",wordResponse.list[Num])
                }*/
                val word_text=wordResponse.list[Num]
                val fragment=StudyWordFragment(word_text,this)
                val fragmentTransaction=supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.study_word_fragment,fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()



            } catch (e: Exception) {
                // 错误处理
                runOnUiThread {
                    Log.e("http", "Request failed: ${e.message}")
                }
            }
        }.start() // 启动线程
    }
    private fun removeEditF(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = supportFragmentManager.findFragmentById(R.id.study_word_fragment)
        if (fragment != null) {
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.commit()
    }
}

/*
object OKhttp{
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10,TimeUnit.SECONDS)
        .readTimeout(10,TimeUnit.SECONDS)
        .writeTimeout(10,TimeUnit.SECONDS)
        .build()

    fun get() {
        // 创建一个线程来执行网络请求
        Thread(Runnable {
            try {
                // 创建请求对象
                val request = Request.Builder()
                    .url("https://cdn.jsdelivr.net/gh/lyc8503/baicizhan-word-meaning-API/data/list.js")
                    .build()

                // 执行请求并获得响应
                val call: Call = client.newCall(request)
                val response = call.execute()

                // 获取响应体内容
                val body = response.body?.string()

                // 检查响应体是否为空
                if (body.isNullOrEmpty()) {
                    Log.e("OkHttp", "Response body is empty or null")
                    return@Runnable
                }


            } catch (e: Exception) {
                // 处理异常
                Log.e("OkHttp", "Request failed", e)
            }
        }).start()
    }


}*/
