package com.example.solutionchallenge.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.solutionchallenge.R
import com.example.solutionchallenge.api.RetrofitClient
import com.example.solutionchallenge.data.DailyMealResponse // ✅ 추가
import kotlinx.coroutines.launch

class ArchiveDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_archive_detail, container, false)

        // ✅ XML View 연결
        val dateLabel = view.findViewById<TextView>(R.id.selectedDateLabel)
        val recordedImage = view.findViewById<ImageView>(R.id.recordedImage)
        val sugarInfo = view.findViewById<TextView>(R.id.sugarInfo)
        val sugarCubesInfo = view.findViewById<TextView>(R.id.sugarCubesInfo)
        val sugarCubeGrid = view.findViewById<GridLayout>(R.id.sugarCubeGrid)
        val mealCardGrid = view.findViewById<GridLayout>(R.id.mealCardGrid)

        // ✅ 선택된 날짜 받기
        val selectedDate = arguments?.getString("selectedDate") ?: "날짜 없음"
        dateLabel.text = selectedDate

        // ✅ 서버 호출: 하루 식사 요약 가져오기
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response: DailyMealResponse =
                    RetrofitClient.apiService.getDailyMeals(selectedDate)

                val sugarGrams = response.dailyTotalSugar
                val sugarCubes = (sugarGrams / 3).toInt()

                sugarInfo.text = "총 ${sugarGrams.toInt()}g의 당류를 섭취했어요."
                sugarCubesInfo.text = "각설탕 약 ${sugarCubes}개와 같아요."

                renderSugarCubes(sugarCubeGrid, sugarGrams)

                // ✅ 대표 이미지 (예: 첫 식사 이미지)
                if (response.meals.isNotEmpty()) {
                    Glide.with(this@ArchiveDetailFragment)
                        .load(response.meals[0].imageUrl)
                        .into(recordedImage)
                }

                // ✅ mealCardGrid 예시: 식사별로 이름 + 총 당류 표시
                // 👉 네 XML에 CardView는 이미 있으니까 필요하면 내용만 갱신해줘!
                response.meals.forEach { meal ->
                    println("🍽️ ${meal.mealType} / 총당류: ${meal.totalSugar}g")
                    meal.mealItems.forEach {
                        println(" - ${it.name}: ${it.sugar}g")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return view
    }

    private fun renderSugarCubes(gridLayout: GridLayout, sugarGrams: Float) {
        gridLayout.removeAllViews()

        val totalCubes = 30
        val maxSugar = 100f
        val filledCubes = ((sugarGrams / maxSugar) * totalCubes).toInt()

        val size = (20 * resources.displayMetrics.density).toInt()
        val margin = (2 * resources.displayMetrics.density).toInt()

        repeat(totalCubes) { index ->
            val cube = View(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(margin, margin, margin, margin)
                }
                setBackgroundResource(
                    if (index < filledCubes) R.drawable.sugar_box_filled
                    else R.drawable.sugar_box_background
                )
            }
            gridLayout.addView(cube)
        }
    }
}
