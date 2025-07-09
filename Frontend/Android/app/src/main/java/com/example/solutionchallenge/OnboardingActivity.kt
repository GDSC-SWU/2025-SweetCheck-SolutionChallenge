package com.example.solutionchallenge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.solutionchallenge.api.AuthRetrofitClient
import com.example.solutionchallenge.api.TokenRequest
import com.example.solutionchallenge.NicknameSetupActivity
import kotlinx.coroutines.launch

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
        Log.d("OnboardingActivity", "onActivityResult 호출됨")

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("OnboardingActivity", "구글 계정 가져옴: ${account?.email}")
                account?.idToken?.let { sendTokenToServer(it) }
            } catch (e: ApiException) {
                Log.e("OnboardingActivity", "구글 로그인 실패: ${e.message}")
            }
        }
    }

    private fun sendTokenToServer(idToken: String) {
        lifecycleScope.launch {
            try {
                val response = AuthRetrofitClient.apiService.login(TokenRequest(idToken))
                Log.d("OnboardingActivity", "서버 인증 성공: $response")


                val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("uid", response.uid)
                    .putString("email", response.email)
                    .putString("name", response.name)
                    .putString("profileImage", response.profileImage)
                    .apply()

                // 서버에서 받은 유저 정보(response) 활용 가능
                startActivity(Intent(this@OnboardingActivity, NicknameSetupActivity::class.java))

            } catch (e: Exception) {
                Log.e("OnboardingActivity", "서버 인증 실패: ${e.message}")
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
