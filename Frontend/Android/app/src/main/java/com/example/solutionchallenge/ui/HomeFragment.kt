package com.example.solutionchallenge.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.R
import com.example.solutionchallenge.api.RetrofitClient
import com.example.solutionchallenge.api.HomeDataResponse
import com.example.solutionchallenge.data.DailyMealResponse // ✅ 추가!
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate // ✅ 추가!

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 상단 프로필 아이콘 → 로그아웃
        val profileIcon = view.findViewById<ImageButton>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            logoutUser()
        }

        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        // ✅ 1) 주간 그래프용 /api/home
        lifecycleScope.launch {
            try {
                val response: HomeDataResponse =
                    RetrofitClient.apiService.getHomeData("Bearer $token")

                updateLineChart(view, response.dailyIntake)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "그래프 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ 2) 오늘 식사 요약용 /api/meals/{date}
        lifecycleScope.launch {
            try {
                val today = LocalDate.now().toString() // ex: "2025-07-10"

                val dailyResponse: DailyMealResponse =
                    RetrofitClient.apiService.getDailyMeals(today)

                val todaySugar = dailyResponse.dailyTotalSugar
                val meals = dailyResponse.meals

                // 👉 TODO: UI에 오늘 당류/식사 정보 뿌리기 예시
                println("✅ 오늘 총 당류: $todaySugar g")
                meals.forEach { meal ->
                    println("🍽️ ${meal.mealType} / 총 당류: ${meal.totalSugar}g")
                    meal.mealItems.forEach {
                        println(" - ${it.name}: ${it.sugar}g")
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "오늘 식사 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ 카드 클릭 이벤트
        val photoCard = view.findViewById<CardView>(R.id.photoCard)
        val menuCard = view.findViewById<CardView>(R.id.menuCard)
        val viewMoreButton = view.findViewById<CardView>(R.id.viewMoreCard)

        photoCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecordStep1Fragment())
                .addToBackStack(null)
                .commit()
        }

        menuCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MenuSelectDialogFragment())
                .addToBackStack(null)
                .commit()
        }

        viewMoreButton.setOnClickListener {
            val intent = Intent(requireContext(), StatisticsActivity::class.java)
            startActivity(intent)
        }
    }

    // ✅ 그래프 갱신 함수
    private fun updateLineChart(view: View, intakeList: List<Int>) {
        val lineChart = view.findViewById<LineChart>(R.id.sugarLineChart)

        val entries = intakeList.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Sugar Intake (g)").apply {
            color = Color.parseColor("#FFA726")
            lineWidth = 3f
            setCircleColor(Color.parseColor("#FFA726"))
            circleRadius = 5f
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.data = LineData(dataSet)

        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(days)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            textSize = 12f
            setDrawGridLines(false)
        }

        val avgLine = LimitLine(25f, "Average (25g)").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
        }
        lineChart.axisLeft.removeAllLimitLines()
        lineChart.axisLeft.addLimitLine(avgLine)

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.textSize = 12f
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.legend.isEnabled = false
        lineChart.invalidate()
    }

    // ✅ 로컬 로그아웃 함수
    private fun logoutUser() {
        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), OnboardingActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
