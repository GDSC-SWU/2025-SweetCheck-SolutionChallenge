package com.example.solutionchallenge.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.solutionchallenge.R
import com.example.solutionchallenge.data.FoodItem

class RecordStep2Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_record_step2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // val result = arguments?.getSerializable("analyzeResult") as? AnalyzeResponse
        val foodList = arguments?.getSerializable("foodList") as? ArrayList<FoodItem>

        /*result?.let {
            view.findViewById<TextView>(R.id.textTotalSugar).text =
                "하루 총 당류: ${it.dailyTotalSugar}g (${it.dailyRiskLevel})"
        }*/

        foodList?.forEach {
            println("미리보기 - ${it.name}: ${it.sugar}g")
        }
    }
}
