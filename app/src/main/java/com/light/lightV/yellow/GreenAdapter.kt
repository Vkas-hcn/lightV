package com.light.lightV.yellow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.light.lightV.R
import com.light.lightV.databinding.ItemYellowBinding
import com.light.lightV.indigo.WaterBt
import com.light.lightV.indigo.currentSelectSever
import com.light.lightV.indigo.vcurrentSelectSeverIsSmart


class GreenAdapter : RecyclerView.Adapter<GreenAdapter.GreenVh>() {

    var greenUpdateAll: ((WaterBt) -> Unit)? = null
    var dataList: ArrayList<WaterBt> = arrayListOf()

    inner class GreenVh(val binding: ItemYellowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenVh {
        val binding = ItemYellowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GreenVh(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: GreenVh, position: Int) {
        with(holder.binding) {
            val data = dataList[position]
            textTitle.text = data.cityName + "-" + position + 1
            imageSelect.setImageResource(if (!vcurrentSelectSeverIsSmart && data.ip == currentSelectSever?.ip) R.mipmap.sever_select_logo else R.mipmap.sever_no_select_logo)

            root.setOnClickListener {
                greenUpdateAll?.invoke(data)
            }
        }
    }
}


