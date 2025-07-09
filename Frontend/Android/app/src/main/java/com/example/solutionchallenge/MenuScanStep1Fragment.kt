package com.example.solutionchallenge

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.solutionchallenge.api.RetrofitClient
import com.example.solutionchallenge.api.toMultipartBodyPart
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class MenuScanStep1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_menu_scan_step1, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val resultTextView = view.findViewById<TextView>(R.id.foodResultText)
        val photoUri = arguments?.getString("photoUri")

        if (photoUri.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Uri → Multipart
        val imagePart = Uri.parse(photoUri).toMultipartBodyPart("image", requireContext())

        // ✅ SharedPreferences에서 userId 가져오기
        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("uid", "") ?: ""

        val userIdPart = MultipartBody.Part.createFormData("userId", uid)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.analyzeMenuPhoto(
                    userId = userIdPart,
                    image = imagePart
                )

                Log.d("MenuScan", "서버 응답: $response")

                resultTextView.text = response.message

            } catch (e: Exception) {
                Log.e("MenuScan", "API 호출 실패", e)
                Toast.makeText(requireContext(), "메뉴 추천 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
