package com.example.wordapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class Study_Adapter (private val wordList: List<Word_s>, private val listener:OnWordClickListener): RecyclerView.Adapter<Study_Adapter.MyViewHolder>() {

    interface OnWordClickListener{

        fun onWordData(word: Word_s)
        fun onDelete(word: Word_s)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val word=wordList[position]

        holder.mistake_word_text.text=word.word
        holder.mistake_word_translation.text= obtainChinese(word.translation.toString()).toString()
        holder.deleteButton.setOnClickListener{
            listener.onDelete(word)
        }
        holder.dataButton.setOnClickListener{
            listener.onWordData(word)
        }
        holder.cardView.setOnClickListener{
            holder.cardView.visibility=View.GONE
        }
    }

    override fun getItemCount(): Int {
        return wordList.size // 你可以根据实际数据调整返回的数量
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView:CardView=view.findViewById(R.id.cardView)
        val mistake_word_text: TextView =view.findViewById(R.id.mistake_word_text)
        val mistake_word_translation: TextView =view.findViewById(R.id.mistake_word_translation)
        val deleteButton: Button =view.findViewById(R.id.delete)
        val dataButton:Button=view.findViewById(R.id.to_wordData)
    }
    //解析翻译得到的JSON字符串，获取中文翻译
    private fun obtainChinese(jsonString: String): List<String> {
        val gson= Gson()
        val jsonResponse=gson.fromJson(jsonString,JsonResponse::class.java)
        return jsonResponse.data.entries.map { it.explain }

    }
}