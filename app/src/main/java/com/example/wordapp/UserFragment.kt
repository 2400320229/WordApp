package com.example.wordapp

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.system.Os.remove
import android.text.TextUtils.replace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         val view=inflater.inflate(R.layout.fragment_user, container, false)
        val sharedPreferences1 = requireActivity().getSharedPreferences("Check", Context.MODE_PRIVATE )
        val sharedPreferences2 = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor2=sharedPreferences1.edit()

        val Time:TextView=view.findViewById(R.id.Time)
        Time.setText("${sharedPreferences2.getLong("Time",3)/60000}分钟")
        val Exit:Button=view.findViewById(R.id.Exit)
        val claer:Button=view.findViewById(R.id.clear)
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

        claer.setOnClickListener {
            val sharedPreferences3 = requireContext().getSharedPreferences("wordId", Context.MODE_PRIVATE )


            val dbHelper=WordDatabaseHelper(requireContext())

            var num=0
            while (num<100){
                try {
                    val word=dbHelper.getWordsWithErrorCount()[0]
                    val id= dbHelper.getIdByWord(word)?.toInt()
                    dbHelper.deleteErrorCount(id!!)


                    Log.d("${id}",dbHelper.getErrorCount(id).toString())
                }catch (e:Exception){

                }
                num++
            }
            var num1=0
            val wordlist=dbHelper.getWordsLearn()
            while (num1<wordlist.size+2){
                try {
                    val word=wordlist[num1]
                    dbHelper.decreaseLearn(word.id.toInt())
                }catch (e:Exception){

                }
                num1++
            }

            val word=dbHelper.getWordsByIdAndLearn(1,20)
            Log.d("${id}",word.toString())
            Log.d("Delete","delete")


            val editor_id = sharedPreferences3.edit()
            editor_id.putInt("studiedId",1)
            editor_id.putInt("well_known",0)
            editor_id.putBoolean("summary",true)
            editor_id.putInt("goalId",3)
            editor_id.apply()

        }

        return view
    }
    // 打开系统图库选择图片
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

        
    }

    // 处理图片选择结果
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




}



