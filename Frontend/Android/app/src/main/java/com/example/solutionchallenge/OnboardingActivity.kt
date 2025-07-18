package com.example.solutionchallenge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.api.AuthRetrofitClient
import com.example.solutionchallenge.api.TokenRequest
import com.example.solutionchallenge.NicknameSetupActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OnboardingActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_activity)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.googleSignInButton).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("🌀Onboarding", "onActivityResult 호출됨")

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("🌀Onboarding", "구글 계정 가져옴: ${account?.email}")
                Log.d("🌀Onboarding", "🔥 idToken = ${account?.idToken}")

                account?.idToken?.let { sendTokenToServer(it) }
            } catch (e: ApiException) {
                Log.e("❌Onboarding", "구글 로그인 실패: ${e.message}", e)
            }
        }
    }

    private fun sendTokenToServer(idToken: String) {
        lifecycleScope.launch {
            try {
                Log.d("🌀Onboarding", "서버에 보낼 idToken = $idToken")

                val response = AuthRetrofitClient.apiService.login(TokenRequest(idToken))
                Log.d("✅Onboarding", "서버 인증 성공: $response")

                val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("uid", response.uid)
                    .putString("email", response.email)
                    .putString("name", response.name)
                    .putString("profileImage", response.profileImage)
                    .apply()

                startActivity(Intent(this@OnboardingActivity, NicknameSetupActivity::class.java))

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("❌Onboarding", "HTTP ${e.code()} 오류: $errorBody")
            } catch (e: Exception) {
                Log.e("❌Onboarding", "서버 요청 실패: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
