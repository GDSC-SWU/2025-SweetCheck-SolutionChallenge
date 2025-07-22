package com.example.solutionchallenge.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.R
import com.example.solutionchallenge.data.ConfirmRequest
import com.example.solutionchallenge.data.FoodItem
import com.example.solutionchallenge.api.RetrofitClient
import kotlinx.coroutines.launch

class RecordStep3Fragment : Fragment() {

    private var mealId: String = ""   // AI가 draft 만들 때 받은 mealId
    private var userId: String = "abc123"  // 예시

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_record_step3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val titleText = view.findViewById<TextView>(R.id.finalSummaryTitle)
        val mealText = view.findViewById<TextView>(R.id.mealType)
        val summaryContainer = view.findViewById<LinearLayout>(R.id.summaryContainer)
        val totalSugarText = view.findViewById<TextView>(R.id.totalSugarText)

        titleText.text = "AI 분석 결과 요약"

        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        mealId = arguments?.getString("mealId") ?: ""  // ✅ mealId 꼭 받아오기

        // ✅ AI가 준 초안 보여주기 예시 (너가 이미 하고 있던 부분)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMealResults("Bearer $token")

                mealText.text = "분석 일자: ${response.date}"
                var total = 0f

                response.meals.forEach { (mealTypeKey, mealDetail) ->
                    mealDetail.foods.forEach { food ->
                        val itemView = layoutInflater.inflate(R.layout.item_food_summary, summaryContainer, false)
                        itemView.findViewById<TextView>(R.id.foodName).text = "$mealTypeKey - ${food.name}"
                        itemView.findViewById<TextView>(R.id.foodSugar).text = "${food.sugar}g"
                        summaryContainer.addView(itemView)
                        total += food.sugar
                    }
                }

                totalSugarText.text = "총 당류: ${total}g (총 당류 큐브: ${response.totalSugarCubes})"

                // ✅ 수정 값도 변수에 보관해두면 됨 (예: 수정된 foods 리스트)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnOk).setOnClickListener {
            confirmMeal()
        }
    }

    private fun confirmMeal() {
        lifecycleScope.launch {
            try {
                // ✅ 수정된 데이터 예시
                val updatedFoods = listOf(
                    FoodItem(name = "Pasta", amount = 150, sugar = 20f),
                    FoodItem(name = "Salad", amount = 100, sugar = 5f)
                )

                val confirmRequest = ConfirmRequest(
                    userId = userId,
                    imageUrl = "https://your-bucket/image.jpg",
                    mealDateTime = "2025-05-03T18:30:00",
                    mealType = "Dinner",
                    totalSugar = 25f,
                    items = updatedFoods
                )

                val response = RetrofitClient.apiService.confirmMeal(
                    mealId = mealId,
                    body = confirmRequest
                )

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "식단 확정 완료!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "확정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "에러: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
