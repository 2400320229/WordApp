package com.example.wordapp

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.wordapp.SearchAdapter.OnSearchClickListener
import com.google.gson.Gson


class SummeryAdapter(private val wordList: List<Word_s>,private val listener: com.example.wordapp.SearchAdapter.OnSearchClickListener) :RecyclerView.Adapter<SummeryAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sum, parent, false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.word.text=wordList[position].word
        holder.error.text= "错${ wordList[position].error_count }次"
        holder.transaction.text=obtainChinese(wordList[position].translation.toString()).toString()
        holder.word.setOnClickListener {
            listener.onWordData(wordList[position])
        }
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
       var word:TextView=view.findViewById(R.id.word_s)
        var error:TextView=view.findViewById(R.id.error_count)
        var transaction:TextView=view.findViewById(R.id.translation_s)
    }


}