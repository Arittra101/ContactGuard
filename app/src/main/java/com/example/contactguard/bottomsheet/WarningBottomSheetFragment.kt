package com.example.contactguard.bottomsheet

import android.os.Bundle
import android.view.View
import com.example.contactguard.R
import com.example.contactguard.databinding.FragmentWarningBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WarningBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_warning_bottom_sheet) {
    private lateinit var binding: FragmentWarningBottomSheetBinding
    private lateinit var bottomSheetCallBack : BottomSheetCallBack

    var title : String?=null
    var msg : String?=null

    companion object {
        const val TAG = "ToffeePremiumPurchaseFragment"
        const val SHEET_TITLE = "SHEET_TITLE"
        const val SHEET_MSG = "SHEET_MSG"
        fun createBundle(sheetTitle: String, sheetMsg: String): Bundle {
            return Bundle().apply {
                putString(SHEET_TITLE, sheetTitle)
                putString(SHEET_MSG, sheetMsg)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(SHEET_TITLE)
        msg = arguments?.getString(SHEET_MSG)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWarningBottomSheetBinding.bind(view)

        binding.title.text = title
        binding.msg.text = msg

        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            bottomSheetCallBack.confirmClick()
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    fun setListener(bottomSheetCallBack: BottomSheetCallBack){
        this.bottomSheetCallBack = bottomSheetCallBack
    }

}
interface BottomSheetCallBack{
    fun confirmClick()
}