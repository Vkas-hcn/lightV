package com.light.lightV.yellow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.light.lightV.orange.OrangeFragment
import com.light.lightV.orange.RedFragment
import com.light.lightV.orange.YellowFragment


class RedAdapter(fragmentManager: FragmentManager?, lifecycle: Lifecycle?) :
    FragmentStateAdapter(fragmentManager!!, lifecycle!!) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RedFragment()
            1 -> OrangeFragment()
            else -> YellowFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}