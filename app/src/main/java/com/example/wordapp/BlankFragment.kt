package com.example.wordapp

import android.content.Context
import android.os.Bundle
import android.system.Os.remove
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlin.text.*


class BlankFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.study_f, container, false)
        val NewGoal:EditText=view.findViewById(R.id.new_goal)
        val sharedPreferences=requireContext()
            .getSharedPreferences("wordId",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        val Button:Button=view.findViewById(R.id.accomplish)

        NewGoal.setText(sharedPreferences.getInt("goalId",0).toString())
        val newGoal = NewGoal.text.toString()
        editor.putInt("goalId",toInteger(newGoal))
        Button.setOnClickListener {

            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.remove(this) // 移除当前的 Fragment
            transaction.commit()
        }
        return view
    }
    fun toInteger(s: String): Int {
        try {
            val value = s.toInt()
            return value
        } catch (ex: NumberFormatException) {
            return 0
        }

    }
}



