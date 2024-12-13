package com.light.lightV.purple

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.light.lightV.databinding.DialogRedBinding


class RedDialog(private val show: Boolean = false) : DialogFragment() {

    private lateinit var binding: DialogRedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogRedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        isCancelable = false

        with(binding) {
            textHint.isVisible = show
            val animator: ObjectAnimator = ObjectAnimator.ofFloat(load1, "rotation", 0f, 360f)
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.RESTART
            animator.setDuration(3000)
            animator.start()
        }
    }
}