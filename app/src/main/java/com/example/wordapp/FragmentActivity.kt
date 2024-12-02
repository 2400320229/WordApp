package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class FragmentActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment)
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()




        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment, StudyFragment())

        transaction.show(StudyFragment())
        transaction.commit()

        var selectIndex = R.id.tab1 // 默认选中的按钮ID
        val toggleGroup: MaterialButtonToggleGroup = findViewById(R.id.ToggleGroup)

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val button = findViewById<MaterialButton>(checkedId)

            if (isChecked) {
                Log.d("checked","isChecked")
                // 如果当前按钮被选中
                if (selectIndex != checkedId) {

                    // 恢复之前选中的按钮的颜色
                    val previousButton = findViewById<MaterialButton>(selectIndex)
                    previousButton.setStrokeColorResource(R.color.white)
                    previousButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                    previousButton.setIconTintResource(R.color.black)
                }

                // 更新选中的按钮
                selectIndex = checkedId
                button.setStrokeColorResource(R.color.black)
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                button.setIconTintResource(R.color.white)

                switchFragment(selectIndex)
            } else {
                // 如果按钮被取消选中
                button.setStrokeColorResource(R.color.white)
                /*button.setTextColor(ContextCompat.getColor(this, R.color.black))*/
            }
        }


        toggleGroup.check(R.id.tab1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true)
        editor.apply()
        val intent=Intent(this,MyService::class.java)

        startService(intent)//关闭时启用service，进行通知
    }
    private fun switchFragment(selectIndex: Int) {
        val fragment = when (selectIndex) {
            R.id.tab1 -> StudyFragment()
            R.id.tab2 -> StarWordFragment()
            R.id.tab3 -> UserFragment()
            else -> throw IllegalArgumentException("Invalid tab selection")
        }
        showOrHideFragment(fragment)
    }

    private fun showOrHideFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        transaction.add(R.id.fragment,fragment)

            if(currentFragment!=null) {
                transaction.remove(currentFragment)
                transaction.replace(R.id.fragment, fragment)
            }else{
                transaction.replace(R.id.fragment, fragment)
                transaction.addToBackStack(null)
            }
        // 提交事务
        transaction.commit()
    }

}