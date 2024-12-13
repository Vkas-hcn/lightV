package com.light.lightV.yellow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.light.lightV.databinding.ItemOrangeBinding
import com.light.lightV.green.getIntFromString
import com.light.lightV.indigo.SeverCountryContainer
import com.light.lightV.indigo.WaterBt


class YellowAdapter : RecyclerView.Adapter<YellowAdapter.YellowVh>() {
    var yellowUpdateAll: ((WaterBt) -> Unit)? = null

    var dataList: ArrayList<SeverCountryContainer> = arrayListOf()

    inner class YellowVh(val binding: ItemOrangeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YellowVh {
        val binding = ItemOrangeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YellowVh(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: YellowVh, position: Int) {
        with(holder.binding) {
            val data = dataList[position]
            typeLogo.setImageResource(getIntFromString(data.countryCode))
            typeCountry.text = data.countryName

            recyclerView.apply {
                adapter = GreenAdapter().apply {
                    greenUpdateAll = yellowUpdateAll
                    dataList = data.detailList ?: arrayListOf()
                    notifyDataSetChanged()
                }
                layoutManager = LinearLayoutManager(context)
            }

            root.setOnClickListener {
                recyclerView.isVisible = !recyclerView.isVisible
                if (recyclerView.isVisible) {
                    moreDetail.rotation = 90f
                } else {
                    moreDetail.rotation = 0f
                }
            }
        }
    }
}


