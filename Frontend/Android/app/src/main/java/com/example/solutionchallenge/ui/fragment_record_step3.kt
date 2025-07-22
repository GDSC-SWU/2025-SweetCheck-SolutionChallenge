package com.example.solutionchallenge.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.R
import com.example.solutionchallenge.data.ConfirmRequest
import com.example.solutionchallenge.data.FoodItem
import com.example.solutionchallenge.api.RetrofitClient
import kotlinx.coroutines.launch

class RecordStep3Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_record_step3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 번들에서 4개의 리스트 받아오기
        val breakfastList = arguments
            ?.getSerializable("finalBreakfast") as? ArrayList<FoodItem> ?: arrayListOf()
        val lunchList = arguments
            ?.getSerializable("finalLunch") as? ArrayList<FoodItem> ?: arrayListOf()
        val dinnerList = arguments
            ?.getSerializable("finalDinner") as? ArrayList<FoodItem> ?: arrayListOf()
        val snackList = arguments
            ?.getSerializable("finalSnack") as? ArrayList<FoodItem> ?: arrayListOf()

        // Fragment 레이아웃의 컨테이너
        val container = view.findViewById<LinearLayout>(R.id.cardContainer)

        // Helper: 리스트에서 totalSugar와 riskLevel 계산
        fun calcTotalSugar(items: List<FoodItem>): Float =
            items.sumOf { it.sugar.toDouble() }.toFloat()

        // TODO: fast api에서 전달받은 당류가 몇 그램 기준인지 확인 후 계산 거쳐야 함
        fun calcRiskLevel(total: Float): String = when {
            total > 15f -> "Excess"   // 과다
            total > 8f -> "Proper"   // 적정
            else         -> "Less"     // 부족
        }

        listOf(
            Triple("Breakfast", breakfastList, calcTotalSugar(breakfastList)),
            Triple("Lunch", lunchList, calcTotalSugar(lunchList)),
            Triple("Dinner", dinnerList, calcTotalSugar(dinnerList)),
            Triple("Snack", snackList, calcTotalSugar(snackList))
        ).forEach { (meal, list, total) ->
            val card = createMealCard(meal, list, total, calcRiskLevel(total))
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20.dpToPx(requireContext())
            }
            card.layoutParams = layoutParams
            container.addView(card)
        }
    }

    private fun createMealCard(
        mealTitle: String,
        items: List<FoodItem>,
        totalSugar: Float,
        riskLevel: String
    ): View {
        val card = layoutInflater.inflate(R.layout.item_food_summary, null, false)
        val mealCardView = card.findViewById<CardView>(R.id.mealCardView)
        val titleView = card.findViewById<TextView>(R.id.mealTitle)
        val foodLayout = card.findViewById<LinearLayout>(R.id.foodListLayout)
        val riskText = card.findViewById<TextView>(R.id.riskLevelText)
        val totalSugarText = card.findViewById<TextView>(R.id.totalSugarText)
        val backgroundContainer = card.findViewById<FrameLayout>(R.id.cardBackgroundContainer)

        // 제목
        titleView.text = mealTitle

        // 음식 목록
        items.forEach { item ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val nameView = TextView(requireContext()).apply {
                text = "• ${item.name}"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val sugarView = TextView(requireContext()).apply {
                text = "${item.sugar}g"
                textSize = 16f
            }
            row.addView(nameView)
            row.addView(sugarView)
            foodLayout.addView(row)
        }

        // 총 당류
        totalSugarText.text = "${totalSugar}g"

        // 위험도별 색상 & 테두리
        when (riskLevel.lowercase()) {
            "proper" -> {
                riskText.setTextColor(Color.parseColor("#009F1D"))
                backgroundContainer.setBackgroundResource(R.drawable.bg_card_proper)
            }
            "less" -> {
                riskText.setTextColor(Color.parseColor("#FFBB00"))
                backgroundContainer.setBackgroundResource(R.drawable.bg_card_less)
            }
            "excess" -> {
                riskText.setTextColor(Color.parseColor("#F77803"))
                backgroundContainer.setBackgroundResource(R.drawable.bg_card_excess)
            }
            else -> {
                riskText.setTextColor(Color.DKGRAY)
            }
        }
        return card
    }
}

fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()