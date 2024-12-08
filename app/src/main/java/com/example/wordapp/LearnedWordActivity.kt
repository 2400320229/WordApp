package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LearnedWordActivity : AppCompatActivity(),SearchAdapter.OnSearchClickListener {
    private lateinit var recyclerView: RecyclerView

    private var wordlist:MutableList<Word_s> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_learned_word)
        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false).apply()

        val dbHelper = WordDatabaseHelper(applicationContext)
        wordlist = dbHelper.getWordsLearn().toMutableList()
        recyclerView = findViewById(R.id.learnedRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = SummeryAdapter(wordlist, this)
        val learnednum:TextView=findViewById(R.id.learnedNum)
        val BackButton: ImageButton =findViewById(R.id.back)

        learnednum.setText("已学单词数：${dbHelper.getWordsLearn().size}")
        BackButton.setOnClickListener { finish() }
    }
    override fun onStop() {
        super.onStop()
        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true).apply()
    }


    override fun onWordData(word_s: Word_s) {
        val dbHelper = WordDatabaseHelper(applicationContext)
        val wordId=dbHelper.getIdByWord(word_s.word)
        val intent= Intent(this,WordData::class.java)
        if (wordId != null) {
            intent.putExtra("key",wordId.toInt())
        }
        startActivity(intent)
    }

}