// 🔥 NicknameSetupActivity.kt
package com.example.solutionchallenge

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.solutionchallenge.api.NicknameRequest
import com.example.solutionchallenge.api.RetrofitClient
import com.example.solutionchallenge.api.TokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

class NicknameSetupActivity : AppCompatActivity() {
    private lateinit var nextButton: Button
    private lateinit var nicknameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nickname_setup)

        nextButton = findViewById(R.id.nextButton)
        nicknameInput = findViewById(R.id.nicknameInput)

        nicknameInput.addTextChangedListener {
            nextButton.isEnabled = it.toString().isNotEmpty()
            nextButton.setBackgroundColor(
                if (nextButton.isEnabled) Color.parseColor("#FFA726") else Color.GRAY
            )
        }

        nextButton.setOnClickListener {
            val nickname = nicknameInput.text.toString()
            performLoginAndRegisterNickname(nickname)
        }
    }

    private fun performLoginAndRegisterNickname(nickname: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val idToken = getIdToken() // 실제 Firebase에서 받아온 값
                val loginResponse = RetrofitClient.apiService.login(TokenRequest(idToken))

                val uid = loginResponse.uid
                val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("uid", uid)
                    .putString("idToken", idToken)
                    .apply()

                val nicknameRequest = NicknameRequest(nickname)
                RetrofitClient.apiService.registerNickname("Bearer $idToken", nicknameRequest)

                withContext(Dispatchers.Main) {
                    startActivity(
                        Intent(
                            this@NicknameSetupActivity,
                            UserInfoInputActivity::class.java
                        )
                    )
                    finish()
                }

            } catch (e: Exception) {
                Log.e("DEBUG", "❌ 로그인 또는 닉네임 등록 실패: ${e.message}")
            }
        }
    }

    // ✅ 실제 로그인된 사용자에게서 ID 토큰 가져오기
    private suspend fun getIdToken(): String {
        val account =
            com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)
                ?: throw Exception("로그인된 계정이 없습니다.")

        return withContext(Dispatchers.IO) {
            val result = kotlinx.coroutines.suspendCancellableCoroutine<String> { cont ->
                account.idToken?.let {
                    cont.resume(it, onCancellation = null)
                } ?: run {
                    cont.resumeWithException(Exception("ID Token이 없습니다."))
                }
            }
            result
        }
    }
}

