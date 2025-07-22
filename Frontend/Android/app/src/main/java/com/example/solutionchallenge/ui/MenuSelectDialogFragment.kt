package com.example.solutionchallenge.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.solutionchallenge.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuSelectDialogFragment : BottomSheetDialogFragment() {

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val purpose = arguments?.getString("cameraPurpose") ?: "record"

                    val destination = if (purpose == "menu") {
                        MenuScanStep1Fragment().apply {
                            arguments = Bundle().apply {
                                putString("photoUri", uri.toString())
                            }
                        }
                    } else {
                        RecordStep1Fragment().apply {
                            arguments = Bundle().apply {
                                putString("photoUri", uri.toString())
                                putInt("selectedDateIndex", arguments?.getInt("selectedDateIndex") ?: -1)
                                putInt("selectedMealIndex", arguments?.getInt("selectedMealIndex") ?: -1)
                            }
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, destination)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val purpose = arguments?.getString("cameraPurpose") ?: "record"
                val cameraFragment = CameraFragment().apply {
                    arguments = Bundle().apply {
                        putString("cameraPurpose", purpose)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, cameraFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_menu_select, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)

        btnGallery.setOnClickListener {
            dismiss()
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            galleryLauncher.launch(intent)
        }

        btnCamera.setOnClickListener {
            dismiss()
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val cameraFragment = CameraFragment().apply {
                    arguments = Bundle().apply {
                        putString("cameraPurpose", "menu") // 목적 전달
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, cameraFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                requestCameraPermissionLauncher.launch(permission)
            }
        }

    }
}
