package com.example.solutionchallenge.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class RecordStep1Fragment : Fragment() {

    private var morningUri: Uri? = null
    private var lunchUri: Uri? = null
    private var dinnerUri: Uri? = null
    private var snackUri: Uri? = null

    private val GALLERY_REQUEST_CODE = 200
    private val CAMERA_REQUEST_CODE  = 300
    private var cameraImageUri: Uri? = null

    private var currentMealType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_record_step1, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnBreakfast = view.findViewById<Button>(R.id.btnBreakfast)
        val btnLunch = view.findViewById<Button>(R.id.btnLunch)
        val btnDinner = view.findViewById<Button>(R.id.btnDinner)
        val btnSnack = view.findViewById<Button>(R.id.btnSnack)
        val btnBack = view.findViewById<ImageButton>(R.id.backButton)

        btnBack.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        // 처음엔 식사 버튼 비활성화
        btnBreakfast.isEnabled = false
        btnLunch.isEnabled = false
        btnDinner.isEnabled = false
        btnSnack.isEnabled = false

        // 날짜 선택하면 버튼 활성화
        val dateContainer = view.findViewById<LinearLayout>(R.id.dateIconContainer)
        for (i in 0 until dateContainer.childCount) {
            val dateItem = dateContainer.getChildAt(i)
            dateItem.setOnClickListener {
                // 식사 버튼 활성화
                btnBreakfast.isEnabled = true
                btnLunch.isEnabled = true
                btnDinner.isEnabled = true
                btnSnack.isEnabled = true

                // 선택 배경 변경
                for (j in 0 until dateContainer.childCount) {
                    dateContainer.getChildAt(j).setBackgroundResource(R.drawable.bg_date_item)
                }
                dateItem.setBackgroundResource(R.drawable.bg_date_item_selected)

            }
        }

        btnBreakfast.setOnClickListener {
            currentMealType = "morning"
            openSourceChooser()
        }

        btnLunch.setOnClickListener {
            currentMealType = "lunch"
            openSourceChooser()
        }

        btnDinner.setOnClickListener {
            currentMealType = "dinner"
            openSourceChooser()
        }

        btnSnack.setOnClickListener {
            currentMealType = "snack"
            openSourceChooser()
        }

        view.findViewById<Button>(R.id.nextButton).setOnClickListener {
            lifecycleScope.launch {
                sendImagesToServer()
            }
        }

        checkIfAllSelected()
    }

    // 사진 찍기 or 갤러리 선택 묻는 다이얼로그
    private fun openSourceChooser() {
        AlertDialog.Builder(requireContext())
            .setTitle("사진을 선택하세요")
            .setItems(arrayOf("사진 찍기", "갤러리에서 선택")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        // 이미지 저장할 Uri
        cameraImageUri = createImageUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 200)
    }

    // 새 이미지 저장용 Uri 생성
    private fun createImageUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireContext()
            .contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
            val uri = when (requestCode) {
                GALLERY_REQUEST_CODE -> data?.data
                CAMERA_REQUEST_CODE  -> cameraImageUri
                else -> null
            } ?: return

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

    private fun checkIfAllSelected() {
        val nextButton = requireView().findViewById<Button>(R.id.nextButton)
        nextButton.isEnabled =
            morningUri != null && lunchUri != null && dinnerUri != null && snackUri != null
    }

    private fun Uri.toCompressedPart(key: String, ctx: Context): MultipartBody.Part {
        // 1) 비트맵으로 디코드
        val input = ctx.contentResolver.openInputStream(this)
            ?: throw IllegalArgumentException("이미지 열기 실패: $this")
        val bmp = BitmapFactory.decodeStream(input)
        input.close()

        // 2) JPEG 포맷으로 80% 압축
        val baos = ByteArrayOutputStream().apply {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
        }
        val bytes = baos.toByteArray()

        // 3) RequestBody로 변환
        val reqBody = bytes
            .toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)

        // 4) MultipartBody.Part 생성
        return MultipartBody.Part.createFormData(key, "img.jpg", reqBody)
    }

    private suspend fun sendImagesToServer() = withContext(Dispatchers.IO) {
        try {
            Log.d("RecordStep1", "▶ sendImages 시작")
            // 압축 버전으로 파트 생성
            val morningPart = morningUri?.toCompressedPart("morning", requireContext()) ?: return@withContext
            val lunchPart   = lunchUri?.toCompressedPart("lunch", requireContext())     ?: return@withContext
            val dinnerPart  = dinnerUri?.toCompressedPart("dinner", requireContext())   ?: return@withContext
            val snackPart   = snackUri?.toCompressedPart("snack", requireContext())     ?: return@withContext

            val t0 = System.currentTimeMillis()

            val response = FastApiRetrofitClient.apiService.analyzeDay(
                morning = morningPart,
                lunch = lunchPart,
                dinner = dinnerPart,
                snack = snackPart
            )
            val t1 = System.currentTimeMillis()
            Log.d("RecordStep1", "◀ 응답 완료: code=${response.code()}, duration=${t1 - t0}ms")


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
                       // putString("mealId", it.mealId)
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
