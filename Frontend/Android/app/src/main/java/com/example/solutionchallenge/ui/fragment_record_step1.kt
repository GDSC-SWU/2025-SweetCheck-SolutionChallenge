package com.example.solutionchallenge.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.R
import com.example.solutionchallenge.api.FastApiRetrofitClient
import com.example.solutionchallenge.api.toMultipartBodyPart
import com.example.solutionchallenge.data.FoodItem
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordStep1Fragment : Fragment() {

    private var morningUri: Uri? = null
    private var lunchUri: Uri? = null
    private var dinnerUri: Uri? = null
    private var snackUri: Uri? = null

    private var currentMealType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_record_step1, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnBreakfast).setOnClickListener {
            currentMealType = "morning"
            openGallery()
        }

        view.findViewById<Button>(R.id.btnLunch).setOnClickListener {
            currentMealType = "lunch"
            openGallery()
        }

        view.findViewById<Button>(R.id.btnDinner).setOnClickListener {
            currentMealType = "dinner"
            openGallery()
        }

        view.findViewById<Button>(R.id.btnSnack).setOnClickListener {
            currentMealType = "snack"
            openGallery()
        }

        view.findViewById<Button>(R.id.nextButton).setOnClickListener {
            lifecycleScope.launch {
                sendImagesToServer()
            }
        }

        checkIfAllSelected()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            when (currentMealType) {
                "morning" -> {
                    morningUri = uri
                    requireView().findViewById<ImageView>(R.id.imageView1).setImageURI(uri)
                }
                "lunch" -> {
                    lunchUri = uri
                    requireView().findViewById<ImageView>(R.id.imageView2).setImageURI(uri)
                }
                "dinner" -> {
                    dinnerUri = uri
                    requireView().findViewById<ImageView>(R.id.imageView3).setImageURI(uri)
                }
                "snack" -> {
                    snackUri = uri
                    requireView().findViewById<ImageView>(R.id.imageView4).setImageURI(uri)
                }
            }
            checkIfAllSelected()
        }
    }

    private fun checkIfAllSelected() {
        val nextButton = requireView().findViewById<Button>(R.id.nextButton)
        nextButton.isEnabled = morningUri != null && lunchUri != null && dinnerUri != null && snackUri != null
    }

    private suspend fun sendImagesToServer() = withContext(Dispatchers.IO) {
        try {
            val morningPart = morningUri?.toMultipartBodyPart("morning", requireContext()) ?: return@withContext
            val lunchPart = lunchUri?.toMultipartBodyPart("lunch", requireContext()) ?: return@withContext
            val dinnerPart = dinnerUri?.toMultipartBodyPart("dinner", requireContext()) ?: return@withContext
            val snackPart = snackUri?.toMultipartBodyPart("snack", requireContext()) ?: return@withContext

            val response = FastApiRetrofitClient.apiService.analyzeDay(
                morning = morningPart,
                lunch = lunchPart,
                dinner = dinnerPart,
                snack = snackPart
            )

            if (response.isSuccessful) {
                val result = response.body()
                result?.let {
                    val foodList = arrayListOf<FoodItem>()
                    val meals = listOf(
                        "Breakfast" to it.meals.morning,
                        "Lunch" to it.meals.lunch,
                        "Dinner" to it.meals.dinner,
                        "Snack" to it.meals.snack
                    )

                    meals.forEach { (mealName, mealData) ->
                        mealData.detectedClasses.forEach { foodName ->
                            val sugarInfo = mealData.foodSugarData[foodName]
                            val sugarValue = when (sugarInfo) {
                                is Number -> sugarInfo.toInt()
                                is String -> Regex("""\d+""").find(sugarInfo)?.value?.toIntOrNull() ?: 0
                                else -> 0
                            }
                            foodList.add(
                                FoodItem(
                                    name = "$mealName - $foodName",
                                    amount = 1,
                                    sugar = sugarValue.toFloat()
                                )
                            )
                        }
                    }

                    val gson = Gson()
                    val resultJson = gson.toJson(result)

                    val bundle = Bundle().apply {
                        putSerializable("analyzeResult", resultJson)
                        putSerializable("foodList", foodList)
                    }

                    withContext(Dispatchers.Main) {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, RecordStep2Fragment().apply { arguments = bundle })
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "통신 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
