package com.example.wordapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class StudyWordFragment(word_text:String, private val listener: MainActivity) : Fragment() {
    private var Word=word_text
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_study_word, container, false)

        val text:TextView=view.findViewById(R.id.Word_text)
        text.setText(Word)
        return view
    }
}