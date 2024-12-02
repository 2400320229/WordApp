package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.times

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudyFragment : Fragment() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view=inflater.inflate(R.layout.fragment_study, container, false)
        val StudyButton:Button=view.findViewById(R.id.StarStudy)
        val StudyInSumText:TextView=view.findViewById(R.id.goal)
        val StudyInGoalText:TextView=view.findViewById(R.id.Study)
        val Showf:Button=view.findViewById(R.id.edit_new_goal)
        val SearchButtom:ImageButton=view.findViewById(R.id.search)
        val TodayWord:Button=view.findViewById(R.id.ReviewWord)
        val BeforeWord:Button=view.findViewById(R.id.ReviewBeforeWord)
        val SpellBforeWord:Button=view.findViewById(R.id.SpellBeforeWord)
        val SpellWord:Button=view.findViewById(R.id.SpellWord)
         progressBar = view.findViewById(R.id.progressBar)

        val sharedPreferences = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )



        val dbHelper=WordDatabaseHelper(requireContext())
        StudyInSumText.setText("${sharedPreferences.getInt("studiedId",0)}/10927")
        StudyInGoalText.setText("${sharedPreferences.getInt("studiedId",0)}/${sharedPreferences.getInt("goalId",0)}")

        progressBar.max=100
        progressBar.progress=((dbHelper.getWordsLearn().size.toDouble()/10927.0) * 100).toInt()


        StudyButton.setOnClickListener{
            val intent=Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
        }

        TodayWord.setOnClickListener{
            val intent=Intent(requireContext(),ReviewActivity::class.java)
            intent.putExtra("WordList","Today")
            startActivity(intent)

        }
        SpellWord.setOnClickListener {
            val intent=Intent(requireContext(),SpellReviewActivity::class.java)
            intent.putExtra("WordList","Today")
            startActivity(intent)
        }
        SpellBforeWord.setOnClickListener {
            val intent=Intent(requireContext(),SpellReviewActivity::class.java)
            intent.putExtra("WordList","Before")
            startActivity(intent)
        }
        BeforeWord.setOnClickListener{
            val intent=Intent(requireContext(),ReviewActivity::class.java)
            intent.putExtra("WordList","Before")
            startActivity(intent)

        }
        Showf.setOnClickListener {
            showAddFragment()
            progressBar.invalidate()
        }
        SearchButtom.setOnClickListener {
            val intent=Intent(requireContext(),SearchActivity::class.java)
            startActivity(intent)

        }
        return view
    }
    private fun showAddFragment(){
        val fragment=BlankFragment()
        val fragmentTransaction=requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.study_f,fragment)
        fragmentTransaction.addToBackStack(null) // 可选，允许后退
        fragmentTransaction.commit()
    }


}