package com.example.contactguard.bottomsheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contactguard.R
import com.example.contactguard.databinding.FragmentHomeBinding
import com.example.contactguard.databinding.FragmentWarningBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WarningBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_warning_bottom_sheet) {
    private lateinit var binding: FragmentWarningBottomSheetBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWarningBottomSheetBinding.bind(view)

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }



}