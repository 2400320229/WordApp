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
        val GoalText:TextView=view.findViewById(R.id.goal)
        val StudyText:TextView=view.findViewById(R.id.Study)
        val Showf:Button=view.findViewById(R.id.edit_new_goal)
        progressBar = view.findViewById(R.id.progressBar)


        // 模拟一个后台任务更新进度条
        Thread {
            for (i in 0..100) {
                Thread.sleep(100)  // 模拟一些耗时操作
                val msg = Message()
                msg.what = 0
                msg.arg1 = i  // 设置进度
                handler.sendMessage(msg)  // 发送消息更新进度条
            }
        }.start()


        val sharedPreferences = requireActivity().getSharedPreferences("wordId", Context.MODE_PRIVATE )

        StudyButton.setOnClickListener{
            val intent=Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
        }
        val ReviewButton:Button=view.findViewById(R.id.ReviewWord)
        ReviewButton.setOnClickListener{
            val intent=Intent(requireContext(),ReviewActivity::class.java)
            startActivity(intent)
            GoalText.setText(sharedPreferences.getInt("goalId",0).toString())
            StudyText.setText(sharedPreferences.getInt("studiedId",0).toString())
        }
        DeleteButton.setOnClickListener{
            val sharedPreferences3 = requireContext().getSharedPreferences("wordId", Context.MODE_PRIVATE )
            val editor_id = sharedPreferences3.edit()
            editor_id.putInt("studiedId",1)
            editor_id.putInt("goalId",20)
            editor_id.apply()


            val dbHelper1= context?.let { MistakeWordIDDatabaseHelper(it) }
            if (dbHelper1 != null) {
                dbHelper1.resetDatabase()
            }
            val dbHelper2=StarWordDatabaseHelper(requireContext())
            dbHelper2.deleteAllData()
            Log.d("Delete","delete")

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