package com.example.wordapp

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.system.Os.remove
import android.text.TextUtils.replace
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.E

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {

    private lateinit var bak:Button
    private lateinit var ImageView:ImageView
    // 请求码常量
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val sharedPreferences=requireActivity().getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false).apply()
         val view=inflater.inflate(R.layout.fragment_user, container, false)
        val sharedPreferences1 = requireActivity().getSharedPreferences("Check", Context.MODE_PRIVATE )
        val sharedPreferences2 = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor2=sharedPreferences1.edit()

        val Time:TextView=view.findViewById(R.id.Time)
        Time.setText("${sharedPreferences2.getLong("Time",3)/60000}分钟")
        val Exit:Button=view.findViewById(R.id.Exit)
        val claer:Button=view.findViewById(R.id.clear)
        val record:Button=view.findViewById(R.id.record)
        bak=view.findViewById(R.id.bak)
        ImageView=view.findViewById(R.id.Image)
        Exit.setOnClickListener{

            editor2.putBoolean("auto_login", false)
            editor2.apply()
            val intent=Intent(requireContext(),FirstActivity::class.java)
            startActivity(intent)

        }
        bak.setOnClickListener {

            openGallery()

        }
        record.setOnClickListener {

            val intent=Intent(requireContext(),RecordActivity::class.java)
            startActivity(intent)
        }

        claer.setOnClickListener {


            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("触发时光机")
            builder.setMessage("请选择")
            builder.setPositiveButton("前进24小时！") { dialog, which ->
                val dbHelper=WordDatabaseHelper(requireContext())
                dbHelper.updateDayForLearnWords()


                val sharedPreferences = context?.getSharedPreferences("wordId", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                val goal= sharedPreferences?.getInt("goalNumber",20)?:20
                val date=sharedPreferences?.getInt("date", 0) ?: 0
                val currentDate = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")  // 自定义格式
                val formattedDate = currentDate.format(formatter)
                val today_study_time=sharedPreferences?.getLong("TodayTime",0)?:0
                if (sharedPreferences != null) {
                    if(sharedPreferences.getBoolean("stu",false)){
                        dbHelper.insertCheckInRecord(1,today_study_time.toInt(),formattedDate.toString())
                    }else{
                        dbHelper.insertCheckInRecord(0,today_study_time.toInt(),formattedDate.toString())
                    }

                }



                // 获取当前的 Id，默认值为 0
                val currentId = sharedPreferences?.getInt("studiedId", 0) ?: 0

                Log.d("studiedId","${currentId}")
                Log.d("goal","${goal}")
                // 将 Id 增长 20
                val newId = currentId + goal

                // 更新保存新的 Id
                editor?.putLong("TodayTime",0)//每日的学习时间归零
                editor?.putInt("date",date+1)//日期加一
                editor?.putInt("goalId", newId)
                editor?.putBoolean("summary",true)
                editor?.putBoolean("stu",false)
                editor?.apply()

                Log.d("before",dbHelper.getBeforeErrorWord().toString())
            }
            builder.setNegativeButton("回到起点") { dialog, which ->
                val sharedPreferences3 = requireContext().getSharedPreferences("wordId", Context.MODE_PRIVATE )


                val dbHelper=WordDatabaseHelper(requireContext())

                var num=0
                while (num<100){
                    try {
                        val word=dbHelper.getWordsWithErrorCount()[0]
                        val id= dbHelper.getIdByWord(word)?.toInt()
                        dbHelper.deleteErrorCount(id!!)


                        Log.d("${id}",dbHelper.getErrorCount(id).toString())
                    }catch (_:Exception){

                    }
                    num++
                }
                var num1=0
                val wordlist=dbHelper.getWordsLearn()
                while (num1<wordlist.size+2){
                    try {
                        val word=wordlist[num1]
                        dbHelper.decreaseLearn(word.id.toInt())
                        dbHelper.decreaseDay(word.id.toInt())
                    }catch (_:Exception){
                    }
                    num1++
                }

                dbHelper.clearCheckInRecords()
                val word=dbHelper.getWordsByIdAndLearn(1,20)
                Log.d("${id}",word.toString())
                Log.d("Delete","delete")


                val editor_id = sharedPreferences3.edit()
                editor_id.putInt("studiedId",0)
                editor_id.putInt("well_known",0)
                editor?.putLong("TodayTime",0)
                editor_id.putInt("date",0)
                editor_id.putBoolean("summary",true)
                editor_id.putInt("goalNumber",3)
                editor_id.putInt("goalId",3)
                editor_id.putBoolean("stu",false)
                editor_id.apply()
                if (dbHelper.checkDatabaseCompleteness()){
                    Toast.makeText(requireContext(),"数据完整",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),"数据不完整，正在恢复，请稍等",Toast.LENGTH_SHORT).show()
                    sendRequestWithOkHttp()
                }
            }
            builder.create().show()


        }

        return view
    }
    // 打开系统图库选择图片
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)


    }

    // 处理图片选择结果
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)


                // 保存图片
                saveImageToInternalStorage(bitmap)
            }
        }
    }

    // 保存图片到内部存储
    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        try {
            val file = File(requireActivity().filesDir, "selected_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            Toast.makeText(requireActivity(), "图片已保存", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), "保存图片失败", Toast.LENGTH_SHORT).show()
        }
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

                val gson= Gson()
                val wordResponse =gson.fromJson(responseData,WordResponse::class.java)

                requireActivity().runOnUiThread{
                    Log.d("WordInfo", "Total words: ${wordResponse.total}")
                    Log.d("WordInfo", "First word in list: ${wordResponse.list[1]}")
                }
                // 存储数据到数据库
                val dbHelper = WordDatabaseHelper(requireContext())

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
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Request failed: ${e.message}", Toast.LENGTH_LONG).show()

                }
            }
        }.start() // 启动线程
        OkHttpRequestTranslate()
    }
    //给单词添加翻译
    private fun OkHttpRequestTranslate() {

        Thread {
            try {

                /*db.beginTransaction()*/
                val dbHelper = WordDatabaseHelper(requireContext())

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

                        val gson= Gson()
                        val wordResponse =gson.fromJson(responseData,WordResponse::class.java)

                        // 存储数据到数据库


                        dbHelper.updateTranslationById(id,responseData.toString())

                        if(id%100==0){


                        requireActivity().runOnUiThread {
                            Toast.makeText(requireActivity(), "${id}", Toast.LENGTH_LONG).show()
                        }
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
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Request failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start() // 启动线程
    }




}



