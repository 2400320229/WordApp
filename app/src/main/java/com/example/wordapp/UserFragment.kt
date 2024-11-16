package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.system.Os.remove
import android.text.TextUtils.replace
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlin.math.E

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         val view=inflater.inflate(R.layout.fragment_user, container, false)
        val sharedPreferences1 = requireActivity().getSharedPreferences("Check", Context.MODE_PRIVATE )
        val sharedPreferences2 = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )
        val editor2=sharedPreferences1.edit()

        val Time:TextView=view.findViewById(R.id.Time)
        Time.setText("${sharedPreferences2.getLong("Time",3)/60000}分钟")
        val Exit:Button=view.findViewById(R.id.Exit)
        Exit.setOnClickListener{

            editor2.putBoolean("auto_login", false)
            editor2.apply()
            val intent=Intent(requireContext(),FirstActivity::class.java)
            startActivity(intent)

        }
        return view
    }



}