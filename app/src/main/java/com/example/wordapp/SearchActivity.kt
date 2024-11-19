package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity(),SearchAdapter.OnSearchClickListener {
    private lateinit var recyclerView: RecyclerView
    private var wordlist:MutableList<Word_s> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val dbHelper = WordDatabaseHelper(applicationContext)
        val sharedPreferences=getSharedPreferences("service", Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",false)
        editor.apply()

        val InputWord:TextView=findViewById(R.id.Input_word)
        InputWord.addTextChangedListener{
            if(InputWord.text.toString().isNotEmpty()) {

                wordlist=dbHelper.searchWords(InputWord.text.toString()).toMutableList()
                recyclerView=findViewById(R.id.search_recycleView)
                recyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                recyclerView.adapter=SearchAdapter(wordlist,this)
            }
        }
    }
    override fun onPause() {
        super.onPause()

        val sharedPreferences=getSharedPreferences("service",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        editor.putBoolean("FA",true)
        editor.apply()
    }

    override fun onWordData(word_s: Word_s) {
        val dbHelper = WordDatabaseHelper(applicationContext)
        val wordId=dbHelper.getIdByWord(word_s.word)
        val intent=Intent(this,WordData::class.java)
        if (wordId != null) {
            intent.putExtra("key",wordId.toInt())
        }
        startActivity(intent)
    }
}