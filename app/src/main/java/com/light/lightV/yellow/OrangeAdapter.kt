package com.light.lightV.yellow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.light.lightV.R
import com.light.lightV.databinding.ItemGreenBinding
import com.light.lightV.indigo.PackageAppMsgEntity

class OrangeAdapter() : RecyclerView.Adapter<OrangeAdapter.OrangeVh>() {

    var dataList: ArrayList<PackageAppMsgEntity> = arrayListOf()

    inner class OrangeVh(val binding: ItemGreenBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrangeVh {
        val binding = ItemGreenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrangeVh(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: OrangeVh, position: Int) {
        with(holder.binding) {
            val data = dataList[position]

            titleApp.text = data.appNameSetOnUI
            imageApp.setImageDrawable(data.appIconSetOnUI)
            switchApp.setImageResource(if (data.canUseVNet) R.mipmap.switch_open_logo else R.mipmap.switch_close_logo)

            switchApp.setOnClickListener {
                data.canUseVNet = !data.canUseVNet
                switchApp.setImageResource(if (data.canUseVNet) R.mipmap.switch_open_logo else R.mipmap.switch_close_logo)
            }
        }
    }
}