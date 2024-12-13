package com.light.lightV.purple

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.light.lightV.databinding.DialogGreenBinding

class GreenDialog : DialogFragment() {

    private lateinit var binding: DialogGreenBinding
    var okAction: (() -> Unit)? = null
    var content: String? = null
    var confirm: String? = null
    var cancel: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogGreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        isCancelable = true

        if (!content.isNullOrEmpty()) {
            binding.contentTxt.text = content
        }
        if (!confirm.isNullOrEmpty()) {
            binding.yesClick.text = confirm
        }
        if (!cancel.isNullOrEmpty()) {
            binding.noClick.text = cancel
        }

        binding.closeClick.setOnClickListener { dismiss() }
        binding.noClick.setOnClickListener { dismiss() }
        binding.yesClick.setOnClickListener {
            okAction?.invoke()
            dismiss()
        }
    }
}