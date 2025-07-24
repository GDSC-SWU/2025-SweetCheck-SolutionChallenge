package com.example.solutionchallenge.ui

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solutionchallenge.R
import com.example.solutionchallenge.adapter.FoodItemAdapter
import com.example.solutionchallenge.api.AnalyzeResponse
import com.example.solutionchallenge.data.FoodItem
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson

class RecordStep2Fragment : Fragment() {
    private lateinit var adapter: FoodItemAdapter
    // 4개의 끼니 리스트
    private lateinit var breakfastList: MutableList<FoodItem>
    private lateinit var lunchList:     MutableList<FoodItem>
    private lateinit var dinnerList:    MutableList<FoodItem>
    private lateinit var snackList:     MutableList<FoodItem>

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, b: Bundle?) =
        inflater.inflate(R.layout.fragment_record_step2, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) JSON -> AnalyzeResponse
        val resultJson = arguments?.getString("analyzeResult")!!
        val response = Gson().fromJson(resultJson, AnalyzeResponse::class.java)

        // 2) 끼니별 FoodItem 리스트 생성
        breakfastList = response.meals.morning.detectedClasses.map { name ->
            val sugarValue = response.meals.morning.foodSugarData[name]
            val sugar = if (sugarValue is Number) sugarValue.toFloat() else 0f
            FoodItem(
                name = response.meals.morning.refinedNames[name] ?: name,
                amount = 1,
                sugar  = sugar )
        }.toMutableList()

        lunchList = response.meals.lunch.detectedClasses.map { name ->
            val sugarValue = response.meals.lunch.foodSugarData[name]
            val sugar = if (sugarValue is Number) sugarValue.toFloat() else 0f
            FoodItem(
                name = response.meals.lunch.refinedNames[name] ?: name,
                amount = 1,
                sugar  = sugar )
        }.toMutableList()

        dinnerList = response.meals.dinner.detectedClasses.map { name ->
            val sugarValue = response.meals.dinner.foodSugarData[name]
            val sugar = if (sugarValue is Number) sugarValue.toFloat() else 0f
            FoodItem(
                name = response.meals.dinner.refinedNames[name] ?: name,
                amount = 1,
                sugar  = sugar )
        }.toMutableList()

        snackList = response.meals.snack.detectedClasses.map { name ->
            val sugarValue = response.meals.snack.foodSugarData[name]
            val sugar = if (sugarValue is Number) sugarValue.toFloat() else 0f
            FoodItem(
                name = response.meals.snack.refinedNames[name] ?: name,
                amount = 1,
                sugar = sugar
            )
        }.toMutableList()

        // 3) RecyclerView + Adapter (기본 Breakfast)
        adapter = FoodItemAdapter(breakfastList)
        view.findViewById<RecyclerView>(R.id.foodRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@RecordStep2Fragment.adapter
        }

        // 4) 탭 클릭 시 swapData 호출
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> adapter.swapData(breakfastList)
                    1 -> adapter.swapData(lunchList)
                    2 -> adapter.swapData(dinnerList)
                    3 -> adapter.swapData(snackList)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        tabLayout.getTabAt(0)!!.select()

        // 5) 추가 버튼: 현재 리스트에 빈 항목 추가 (option)
        view.findViewById<Button>(R.id.btnAddMore).setOnClickListener {
            val list = when (tabLayout.selectedTabPosition) {
                0 -> breakfastList
                1 -> lunchList
                2 -> dinnerList
                else -> snackList
            }
            list.add(FoodItem(name = "", amount = 1, sugar = 0f))
            adapter.notifyItemInserted(list.size - 1)
        }

        // 6) Next 버튼: final 데이터 넘기기
// Step2Fragment.kt 중 btnNext 클릭 리스너 내부 수정
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("finalBreakfast", ArrayList(breakfastList))
                putSerializable("finalLunch",     ArrayList(lunchList))
                putSerializable("finalDinner",    ArrayList(dinnerList))
                putSerializable("finalSnack",     ArrayList(snackList))
                putString("mealId",       arguments?.getString("mealId"))
                putString("mealType", arguments?.getString("mealType"))         // ✅ Step1에서 넘긴 끼니
                putString("mealDateTime", arguments?.getString("mealDateTime")) // ✅ Step1에서 넘긴 날짜
                putString("imageUrl", arguments?.getString("imageUrl"))         // ✅ Step1에서 넘긴 이미지 URL
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecordStep3Fragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
        }

    }
}
