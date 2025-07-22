package com.example.solutionchallenge.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.solutionchallenge.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        setupLineChart()
        setupPieChart()
        setupBarChart()
    }

    private fun setupLineChart() {
        val lineChart = findViewById<LineChart>(R.id.statisticsLineChart)

        val entries = listOf(
            Entry(0f, 20f),
            Entry(1f, 25f),
            Entry(2f, 15f),
            Entry(3f, 35f),
            Entry(4f, 22f),
            Entry(5f, 40f),
            Entry(6f, 28f)
        )

        val dataSet = LineDataSet(entries, "Sugar Intake").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            lineWidth = 2f
            valueTextSize = 10f
        }

        lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            description.isEnabled = false
            invalidate()
        }
    }

    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val pieEntries = listOf(
            PieEntry(25f, "Breakfast"),
            PieEntry(35f, "Lunch"),
            PieEntry(20f, "Dinner"),
            PieEntry(20f, "Snack")
        )

        val pieDataSet = PieDataSet(pieEntries, "").apply {
            colors = listOf(
                Color.parseColor("#FFA726"), // 오렌지
                Color.parseColor("#66BB6A"), // 초록
                Color.parseColor("#42A5F5"), // 파랑
                Color.parseColor("#EF5350")  // 빨강
            )
            valueTextSize = 14f
        }

        pieChart.apply {
            data = PieData(pieDataSet)
            description.isEnabled = false
            centerText = "Meals"
            setEntryLabelColor(Color.BLACK)
            invalidate()
        }
    }

    private fun setupBarChart() {
        val barChart = findViewById<BarChart>(R.id.barChart)

        val entries = listOf(
            BarEntry(0f, 30f), // WHO 기준
            BarEntry(1f, 35f), // 지난 주
            BarEntry(2f, 50f)  // 이번 주
        )

        val dataSet = BarDataSet(entries, "Sugar Comparison").apply {
            color = Color.parseColor("#29B6F6")
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)

        barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("WHO", "Last", "This"))
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            description.isEnabled = false
            invalidate()
        }
    }
}
