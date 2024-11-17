package com.example.wordapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SummaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private var wordlist:MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_summary)
        val dbHelper=WordDatabaseHelper(applicationContext)
        wordlist=dbHelper.getMostMistakenWords(2).toMutableList()
        recyclerView=findViewById(R.id.summeryRecycleView)
        recyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter=SummeryAdapter(wordlist)


    }
}