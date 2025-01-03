package com.example.wordapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlin.text.*


class BlankFragment : DialogFragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.edit_goal_f, container, false)
        val NewGoal:EditText=view.findViewById(R.id.new_goal)
        val sharedPreferences=requireContext()
            .getSharedPreferences("wordId",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        val Button:Button=view.findViewById(R.id.accomplish)

        NewGoal.setText(sharedPreferences.getInt("goalNumber",0).toString())

        setCancelable(false)

        Button.setOnClickListener {

            val newGoal = NewGoal.text.toString().toInt()
            editor.putInt("goalNumber",newGoal)
            editor.putInt("goalId",newGoal+sharedPreferences.getInt("studiedId",0))
            editor.apply()
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.remove(this) // 移除当前的 Fragment
            transaction.commit()
            Log.d("f_goal",sharedPreferences.getInt("goalId",0).toString())
        }

        return view
    }
}



