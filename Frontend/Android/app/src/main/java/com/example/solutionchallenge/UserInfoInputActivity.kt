// 🔥 UserInfoInputActivity.kt
package com.example.solutionchallenge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
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

    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_info_input)

        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        ageInput = findViewById(R.id.ageInput)
        nextButton = findViewById(R.id.nextButton)
        btnMale = findViewById(R.id.btnMale)
        btnFemale = findViewById(R.id.btnFemale)

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

        heightInput.addTextChangedListener { checkAllInputs() }
        weightInput.addTextChangedListener { checkAllInputs() }
        ageInput.addTextChangedListener { checkAllInputs() }

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
        val idToken = prefs.getString("idToken", "") ?: ""

        val request = UserProfileRequest(
            uid = uid,
            gender = selectedGender ?: "",
            height = heightInput.text.toString().toIntOrNull(),
            weight = weightInput.text.toString().toIntOrNull(),
            age = ageInput.text.toString().toIntOrNull(),
            nickname = null
        )

        val bearerToken = "Bearer $idToken"

        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.registerProfile(bearerToken, request)
                Toast.makeText(this@UserInfoInputActivity, "프로필 등록 성공!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@UserInfoInputActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@UserInfoInputActivity, "프로필 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
