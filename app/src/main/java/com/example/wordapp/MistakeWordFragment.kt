package com.example.wordapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MistakeWordFragment : Fragment(),Study_Adapter.OnWordClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wordDatabaseHelper: MistakeWordDatabaseHelper
    private var wordlist:MutableList<Word> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_mistake_word, container, false)

        // 初始化 recyclerView
        recyclerView = view.findViewById(R.id.RecyclerView)
        wordDatabaseHelper=MistakeWordDatabaseHelper(requireContext())
        wordlist= wordDatabaseHelper.getAllWords().toMutableList()

        // 设置布局管理器
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // 设置适配器
        recyclerView.adapter = Study_Adapter(wordlist,this)
        return view
    }

    override fun onWordData(word: Word) {
        val intent= Intent(requireContext(),WordData::class.java)
        intent.putExtra("word",word.word)
        intent.putExtra("chinese",word.translation)
        startActivity(intent)

    }

    override fun onDelete(word: Word) {


        wordDatabaseHelper.deleteWord(word.id)
        loadWords()
        recyclerView.adapter?.notifyDataSetChanged()
    }
    private fun loadWords(){
        wordlist.clear()
        wordlist.addAll(wordDatabaseHelper.getAllWords())

    }


}