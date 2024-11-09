package com.example.wordapp

import android.media.RouteListingPreference
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Study_Word : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_study_word)

        // 初始化 recyclerView
        recyclerView = findViewById(R.id.recyclerView)

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 设置适配器
        recyclerView.adapter = Study_Adapter()
    }

    inner class Study_Adapter : RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemview: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            return MyViewHolder(itemview)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            // 绑定数据（根据你的需求，你可以在这里绑定数据）
        }

        override fun getItemCount(): Int {
            return 20 // 你可以根据实际数据调整返回的数量
        }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 在这里定义 ViewHolder 中的视图组件，例如 TextView, ImageView 等
    }
}