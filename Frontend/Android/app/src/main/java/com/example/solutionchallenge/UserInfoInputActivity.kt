package com.example.solutionchallenge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.api.RetrofitClient
import com.example.solutionchallenge.api.UserProfileRequest
import kotlinx.coroutines.launch

class UserInfoInputActivity : AppCompatActivity() {

    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var nextButton: Button
    private lateinit var btnMale: Button
    private lateinit var btnFemale: Button

    private var selectedGender: String? = null // "male" or "female"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_info_input)

        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        ageInput = findViewById(R.id.ageInput)
        nextButton = findViewById(R.id.nextButton)
        btnMale = findViewById(R.id.btnMale)
        btnFemale = findViewById(R.id.btnFemale)

        val selectedColor = ContextCompat.getColor(this, R.color.orange_600)
        val defaultColor = ContextCompat.getColor(this, R.color.gray_300)

        // ✅ 성별 선택 버튼 클릭 처리
        btnMale.setOnClickListener {
            selectedGender = "male"
            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange_600))
            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_300))
            checkAllInputs()
        }

        btnFemale.setOnClickListener {
            selectedGender = "female"
            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange_600))
            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_300))
            checkAllInputs()
        }

        // ✅ 입력 변화 감지
        heightInput.addTextChangedListener { checkAllInputs() }
        weightInput.addTextChangedListener { checkAllInputs() }
        ageInput.addTextChangedListener { checkAllInputs() }

        // ✅ 프로필 등록 + 다음 화면으로 이동
        nextButton.setOnClickListener {
            sendProfileToServer()
        }
    }

    private fun checkAllInputs() {
        val allFilled = heightInput.text.isNotBlank()
                && weightInput.text.isNotBlank()
                && ageInput.text.isNotBlank()
                && selectedGender != null

        nextButton.isEnabled = allFilled
        nextButton.setBackgroundTintList(
            ContextCompat.getColorStateList(
                this,
                if (allFilled) R.color.orange_600 else R.color.gray_300
            )
        )
    }

    private fun sendProfileToServer() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("uid", "") ?: ""

        val gender = selectedGender ?: ""
        val height = heightInput.text.toString()
        val weight = weightInput.text.toString()
        val age = ageInput.text.toString()

        val request = UserProfileRequest(
            uid = uid,
            gender = gender,
            height = height,
            weight = weight,
            age = age
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.registerUserProfile(request)
                Toast.makeText(this@UserInfoInputActivity, "프로필 등록 성공!", Toast.LENGTH_SHORT).show()

                // ✅ 다음 화면으로 이동
                startActivity(Intent(this@UserInfoInputActivity, MainActivity::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@UserInfoInputActivity, "프로필 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
