package com.example.solutionchallenge

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.api.RetrofitClient
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.MonthDayBinder
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class ArchiveFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val uid = prefs.getString("uid", "") ?: ""

        lifecycleScope.launch {
            try {
                val currentMonth = YearMonth.now().toString()  // "2025-07" 같은 형식
                val response = RetrofitClient.apiService.getMonthlySummary(
                    userId = uid,
                    month = currentMonth
                )

                renderSugarCubes(view, response.totalSugar)

                // TODO: response.dailyRecords 돌려서 캘린더에서 날짜에 표시 추가
                // ex: 기록 있는 날짜에 점 찍기 등

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ 캘린더 초기화
        calendarView = view.findViewById(R.id.calendarView)
        val currentMonth = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.MONDAY

        calendarView.setup(currentMonth, currentMonth.plusMonths(12), firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
            }
        }

        view.findViewById<TextView>(R.id.galleryIcon).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, ArchiveGalleryFragment())
                addToBackStack(null)
            }
        }

        view.findViewById<CardView>(R.id.cardRecord).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, CameraMenuFragment())
                addToBackStack(null)
            }
        }
    }

    private fun renderSugarCubes(view: View, sugarGrams: Float) {
        val sugarGrid = view.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.sugarGrid)

        sugarGrid.removeAllViews()

        val totalCubes = 30
        val maxSugar = 100f
        val filledCubes = ((sugarGrams / maxSugar) * totalCubes).toInt()

        val size = (40 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()

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
            sugarGrid.addView(cube)
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        private val textView = TextView(view.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            textSize = 16f
        }

        init {
            (view as ViewGroup).addView(textView)
        }

        fun bind(day: CalendarDay) {
            textView.text = day.date.dayOfMonth.toString()
            textView.setOnClickListener {
                val selectedDate = day.date.format(dateFormatter)
                val detailFragment = ArchiveDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("selectedDate", selectedDate)
                    }
                }
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, detailFragment)
                    addToBackStack(null)
                }
            }
        }
    }
}
