package com.example.wordapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Watch_Mistake_Word : AppCompatActivity(),Study_Adapter.OnWordClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wordDatabaseHelper: MistakeWordDatabaseHelper
    private var wordlist:MutableList<Word> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mistake_word)

        // 初始化 recyclerView
        recyclerView = findViewById(R.id.recyclerView)
        wordDatabaseHelper=MistakeWordDatabaseHelper(this)
        wordlist= wordDatabaseHelper.getAllWords().toMutableList()

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 设置适配器
        recyclerView.adapter = Study_Adapter(wordlist,this)
    }

    override fun onWordData(word: Word) {
        val intent=Intent(this,WordData::class.java)
        intent.putExtra("word",word.word)
        intent.putExtra("chinese",word.translation)
        startActivity(intent)

    }

    override fun onDelete(word: Word) {

        val dbHelper=MistakeWordDatabaseHelper(applicationContext)
        dbHelper.deleteWord(word.id)
        loadWords()
        recyclerView.adapter?.notifyDataSetChanged()
    }
    private fun loadWords(){

        val dbHelper=MistakeWordDatabaseHelper(applicationContext)
        wordlist.clear()
        wordlist.addAll(dbHelper.getAllWords())

    }


}


