package com.example.wordapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView


class RecordAdapter( val context: Context,val list: List<Clock_in_record>,private val listener: OnRecordClickListener) : RecyclerView.Adapter<RecordAdapter.MyViewHolder>() {

    interface OnRecordClickListener{
        fun getDateWord(date: String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyViewHolder {
        val itemview: View =
            LayoutInflater.from(parent.context).inflate(R.layout.date_item, parent, false)
        return MyViewHolder(itemview)
    }
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val date=view.findViewById<TextView>(R.id.date)
        val date_time=view.findViewById<TextView>(R.id.date_time)
        val card:CardView=view.findViewById(R.id.card)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.date.text=list[position].check_in_date
        holder.date_time.text= "${ (list[position].check_in_duration) / 60000 }分钟"
        if(list[position].is_checked_in==1){
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context,R.color.aquamarine))
        }else{
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context,R.color.hotpink))
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}
