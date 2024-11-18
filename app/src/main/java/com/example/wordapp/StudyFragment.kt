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
import android.widget.ProgressBar
import android.widget.TextView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            0 -> progressBar.progress = msg.arg1  // 更新进度
        }
        true
    })

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
        val StudyInSumText:TextView=view.findViewById(R.id.goal)
        val StudyInGoalText:TextView=view.findViewById(R.id.Study)
        val Showf:Button=view.findViewById(R.id.edit_new_goal)
         progressBar = view.findViewById(R.id.progressBar)

        val sharedPreferences = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )



        StudyInSumText.setText("${sharedPreferences.getInt("studiedId",0)}/19702")
        StudyInGoalText.setText("${sharedPreferences.getInt("studiedId",0)}/${sharedPreferences.getInt("goalId",0)}")
        progressBar.progress=(2000/19702) * 100

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

            val dbHelper1= context?.let { MistakeWordIDDatabaseHelper(it) }
            if (dbHelper1 != null) {
                dbHelper1.resetDatabase()
            }
            val dbHelper=WordDatabaseHelper(requireContext())
            var id=0
            while (id<sharedPreferences3.getInt("studiedId",0)){
                dbHelper.deleteErrorCount(id)

                Log.d("${id}",dbHelper.getErrorCount(id).toString())
                id++
            }
            val dbHelper2=StarWordDatabaseHelper(requireContext())
            dbHelper2.deleteAllData()
            Log.d("Delete","delete")


            val editor_id = sharedPreferences3.edit()
            editor_id.putInt("studiedId",1)
            editor_id.putInt("well_known",0)

            editor_id.putInt("goalId",20)
            editor_id.apply()
        }
        Showf.setOnClickListener {
            showAddFragment()
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