package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecordActivity : AppCompatActivity(),SearchAdapter.OnSearchClickListener,RecordAdapter.OnRecordClickListener {
    private lateinit var recyclerView1: RecyclerView

    private var datelist:MutableList<Clock_in_record> = mutableListOf()
    private var wordlist:MutableList<Word_s> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)

        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false).apply()
        val dbHelper=WordDatabaseHelper(applicationContext)
        val dayNum:TextView=findViewById(R.id.day_number)
        val BackButton:ImageButton=findViewById(R.id.back)

        recyclerView1=findViewById(R.id.Clock_in2)
        datelist=dbHelper.getAllCheckInRecords().toMutableList()


        Log.d("list","${datelist}")
        dayNum.setText("共打卡${(dbHelper.getSucceedCheckedInRecords()).size}天")
        recyclerView1.layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recyclerView1.adapter=RecordAdapter(this,datelist,this)
        BackButton.setOnClickListener {
            finish()
        }
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

    override fun getDateWord(date: String) {
        val dbHelper=WordDatabaseHelper(applicationContext)
        wordlist= dbHelper.getWordsByDayAndLearnGreaterThanZero(date.toInt()).toMutableList()
        Log.d("wordlist","${wordlist}")
    }
}