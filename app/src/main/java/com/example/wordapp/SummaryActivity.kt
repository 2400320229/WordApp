package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SummaryActivity : AppCompatActivity(),SearchAdapter.OnSearchClickListener {

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
        recyclerView.adapter=SummeryAdapter(wordlist,this)
        val WordKnow:TextView=findViewById(R.id.word_know)
        val UnknownNumber:TextView=findViewById(R.id.word_unknown_number)
        val Button:Button=findViewById(R.id.ok)
        val time:TextView=findViewById(R.id.summeryTime)
        val sharedPreferences=getSharedPreferences("wordId",Context.MODE_PRIVATE)
        Log.d("known","${sharedPreferences.getInt("well_known",-1)}")
        WordKnow.text="第一次就认识${(sharedPreferences.getInt("well_known",0).toDouble()/sharedPreferences.getInt("goalId",1).toDouble()*100)}%"
        UnknownNumber.text="以下是错误单词"
        val editor=sharedPreferences.edit()
        editor.putInt("well_known",-1)
        editor.apply()
        time.setText("用时${ intent.getLongExtra("Time", 0)/1000 }秒钟")

        Button.setOnClickListener {
            finish()
        }


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