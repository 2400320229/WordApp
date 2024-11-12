package com.example.wordapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Study_Adapter (private val wordList: List<Word>, private val listener:OnWordClickListener): RecyclerView.Adapter<Study_Adapter.MyViewHolder>() {

    interface OnWordClickListener{

        fun onWordData(word: Word)
        fun onDelete(word: Word)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val word=wordList[position]

        holder.mistake_word_text.text=word.word
        holder.mistake_word_translation.text=word.translation?:"无翻译"
        holder.deleteButton.setOnClickListener{
            listener.onDelete(word)
        }
        holder.dataButton.setOnClickListener{
            listener.onWordData(word)
        }
    }

    override fun getItemCount(): Int {
        return wordList.size // 你可以根据实际数据调整返回的数量
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 在这里定义 ViewHolder 中的视图组件，例如 TextView, ImageView 等
        val mistake_word_text: TextView =view.findViewById(R.id.mistake_word_text)
        val mistake_word_translation: TextView =view.findViewById(R.id.mistake_word_translation)
        val deleteButton: Button =view.findViewById(R.id.delete)
        val dataButton:Button=view.findViewById(R.id.to_wordData)
    }
}