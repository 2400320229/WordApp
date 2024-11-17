package com.example.wordapp

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SummaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private var wordlist:MutableList<Word_s> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_summary)
        val dbHelper=WordDatabaseHelper(applicationContext)
        wordlist=dbHelper.getWordsWithErrorCountGreaterThanZero().toMutableList()
        recyclerView=findViewById(R.id.summeryRecycleView)
        recyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter=SummeryAdapter(wordlist)
        val WordKnow:TextView=findViewById(R.id.word_know)
        val UnknownNumber:TextView=findViewById(R.id.word_unknown_number)
        val sharedPreferences=getSharedPreferences("wordId",Context.MODE_PRIVATE)
        WordKnow.text="100%"



    }
}