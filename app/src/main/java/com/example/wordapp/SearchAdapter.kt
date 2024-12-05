package com.example.wordapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson


class SearchAdapter(private val wordList: List<Word_s>,private val listener:OnSearchClickListener) :RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {


    interface OnSearchClickListener{
        fun onWordData(word_s:Word_s)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview: View =
            LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.word.text=wordList[position].word
        holder.transaction.text= obtainChinese(wordList[position].translation.toString()).toString()
        holder.word.setOnClickListener {
            listener.onWordData(wordList[position])
        }
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
       var word:TextView=view.findViewById(R.id.search_word)
        var transaction:TextView=view.findViewById(R.id.search_translation)
    }



}