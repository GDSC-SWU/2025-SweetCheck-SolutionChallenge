package com.example.solutionchallenge.api

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun Uri.toMultipartBodyPart(fieldName: String, context: Context): MultipartBody.Part {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(this) ?: "image/jpeg" // 기본값
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            else -> "jpg"
        }

        val inputStream = contentResolver.openInputStream(this)
            ?: throw IllegalArgumentException("파일을 열 수 없습니다: $this")

        val file = File(context.cacheDir, "$fieldName.$extension")
        FileOutputStream(file).use { output -> inputStream.copyTo(output) }

        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
    } catch (e: Exception) {
        Log.e("MultipartError", "변환 실패: ${e.message}")
        throw e
    }
}
