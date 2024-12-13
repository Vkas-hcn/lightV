package com.light.lightV.purple

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.DialogFragment
import com.light.lightV.databinding.DialogOrangeBinding


class OrangeDialog : DialogFragment() {

    private lateinit var binding: DialogOrangeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogOrangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        isCancelable = false
        binding.confirmAction.setOnClickListener { finishAffinity(requireActivity()) }

    }
}