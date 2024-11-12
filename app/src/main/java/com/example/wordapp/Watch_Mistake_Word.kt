package com.example.wordapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
data class Word(val id: Long, val word: String, val translation: String?)
class Watch_Mistake_Word : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wordDatabaseHelper: MistakeWordDatabaseHelper
    private lateinit var wordlist:List<Word>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mistake_word)

        // 初始化 recyclerView
        recyclerView = findViewById(R.id.recyclerView)
        wordDatabaseHelper=MistakeWordDatabaseHelper(this)
        wordlist=wordDatabaseHelper.getAllWords()

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 设置适配器
        recyclerView.adapter = Study_Adapter(wordlist)
    }

    inner class Study_Adapter (private val wordList: List<Word>): RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemview: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            return MyViewHolder(itemview)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val word=wordList[position]

            holder.mistake_word_text.text=word.word
            holder.mistake_word_translation.text=word.translation?:"无翻译"
        }

        override fun getItemCount(): Int {
            return wordList.size // 你可以根据实际数据调整返回的数量
        }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 在这里定义 ViewHolder 中的视图组件，例如 TextView, ImageView 等
        val mistake_word_text:TextView=view.findViewById(R.id.mistake_word_text)
        val mistake_word_translation:TextView=view.findViewById(R.id.mistake_word_translation)
    }
}