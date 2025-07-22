package com.example.solutionchallenge.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.solutionchallenge.R
import com.example.solutionchallenge.api.RetrofitClient
import kotlinx.coroutines.launch

class ArchiveGalleryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_archive_gallery, container, false)

        val galleryContainer = view.findViewById<LinearLayout>(R.id.galleryContainer)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMonthlySummary(
                    userId = "abc123",
                    month = "2025-05"
                )

                response.dailyRecords.forEach { record ->
                    val dateTextView = TextView(requireContext()).apply {
                        text = record.date
                        textSize = 18f
                        setPadding(0, 16, 0, 8)
                    }
                    galleryContainer.addView(dateTextView)

                    val gridLayout = GridLayout(requireContext()).apply {
                        columnCount = 2
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    record.imageUrls.forEach { imageUrl ->
                        val imageView = ImageView(requireContext()).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = 0
                                height = (150 * resources.displayMetrics.density).toInt()
                                setMargins(8, 8, 8, 8)
                                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        Glide.with(this@ArchiveGalleryFragment)
                            .load(imageUrl)
                            .into(imageView)

                        gridLayout.addView(imageView)
                    }

                    galleryContainer.addView(gridLayout)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return view
    }
}
