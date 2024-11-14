package com.example.wordapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

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
        val DeleteButton:Button=view.findViewById(R.id.DeleteWord)
        val GoalText:TextView=view.findViewById(R.id.goal)
        val StudyText:TextView=view.findViewById(R.id.Study)

        StudyButton.setOnClickListener{
            val intent=Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
        }
        val ReviewButton:Button=view.findViewById(R.id.ReviewWord)
        ReviewButton.setOnClickListener{
            val intent=Intent(requireContext(),ReviewActivity::class.java)
            startActivity(intent)
        }
        DeleteButton.setOnClickListener{
            val sharedPreferences3 = requireContext().getSharedPreferences("wordId", Context.MODE_PRIVATE )
            val editor_id = sharedPreferences3.edit()
            editor_id.putInt("studiedId",1)
            editor_id.putInt("goalId",20)
            editor_id.apply()

            val dbHelper2=StarWordDatabaseHelper(requireContext())
            dbHelper2.deleteAllData()
            Log.d("Delete","delete")
        }
        return view
    }


}