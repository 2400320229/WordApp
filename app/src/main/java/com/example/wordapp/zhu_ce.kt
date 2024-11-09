package com.example.wordapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class zhu_ce : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zhu_ce)
        val sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE )
        val editor = sharedPreferences.edit()
        val button11: Button =findViewById(R.id.button11)
        val editText1:EditText=findViewById(R.id.ee1)//用户名
        val editText2:EditText=findViewById(R.id.ee2)//密码
        val editText3:EditText=findViewById(R.id.ee3)
        //获取输入的文本

        button11.setOnClickListener {
            val inputText1 = editText1.text.toString().trim()
            val inputText2 = editText2.text.toString().trim()
            val inputText3 = editText3.text.toString().trim()


            //比较，如果都不为空则进行下一步
            if(sharedPreferences.contains(inputText1)){
                Toast.makeText(this, "用户名已存在，请选择其他用户名！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //比较，如果都不为空则进行下一步

            if((inputText1.isNotEmpty()&&inputText2.isNotEmpty())&&(inputText3==inputText2)) {
                Toast.makeText(this,"注册成功",
                    Toast.LENGTH_SHORT).show()
                editor.putString(inputText1,inputText2)
                editor.apply()
                //上传

                finish()

            }
            else if(inputText3!=inputText2){
                Toast.makeText(this,"两次密码不一致！",
                    Toast.LENGTH_SHORT).show()
            }

            else{
                Toast.makeText(this,"不要再耍宝了！快点输入密码或者账号",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
}

