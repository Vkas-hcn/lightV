package com.light.lightV.orange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.light.lightV.databinding.FragmentOrangeBinding
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.putKv
import com.light.lightV.indigo.PackageAppMsgEntity
import com.light.lightV.indigo.OrangeVM
import com.light.lightV.indigo.allPackage
import com.light.lightV.indigo.isDealPackage
import com.light.lightV.purple.GreenDialog
import com.light.lightV.yellow.OrangeAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


class OrangeFragment : Fragment() {
    private lateinit var binding: FragmentOrangeBinding

    private val orangeAdapter: OrangeAdapter by lazy {
        OrangeAdapter()
    }
    private val vm: OrangeVM by activityViewModels()
    private var cachePackages: MutableList<PackageAppMsgEntity> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            openButton.setOnClickListener {
                searchEdit.setText("")
                orangeAdapter.dataList.forEach { it.canUseVNet = true }
                orangeAdapter.notifyDataSetChanged()
            }
            saveButton.setOnClickListener {
                GreenDialog().apply {
                    content =
                        "Saving will require a reconnect to apply the proxy. Do you want to save?"
                    confirm = "Confirm"
                    cancel = "Cancel"
                    okAction = {
                        var result = ""
                        orangeAdapter.dataList.forEach {
                            if (!it.canUseVNet) {
                                result += "%${it.packageNameSetInSystem}"
                            }
                        }
                        "all no Vpn net packages ===> ${result}".lightVDebugLog()
                        result.putKv("noVpnNetPackages")
                        allPackage = cachePackages as ArrayList<PackageAppMsgEntity>
                        vm.updateNoVpnPs()
                    }
                }.show(requireActivity().supportFragmentManager, "GreenDialog")
            }
            searchEdit.doAfterTextChanged { text ->
                if (cachePackages.isNotEmpty() && !text.isNullOrEmpty()) {
                    val filterList = cachePackages.filter {
                        it.appNameSetOnUI.contains(text.toString())
                    }
                    orangeAdapter.dataList = filterList as ArrayList<PackageAppMsgEntity>
                } else {
                    orangeAdapter.dataList = cachePackages as ArrayList<PackageAppMsgEntity>
                }
                orangeAdapter.notifyDataSetChanged()
                if (orangeAdapter.dataList.isNullOrEmpty()) {
                    binding.textEmpty.visibility = View.VISIBLE
                } else {
                    binding.textEmpty.visibility = View.GONE
                }
            }
            recyclerView.adapter = orangeAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    var job: Job? = null
    override fun onResume() {
        super.onResume()
        "cache resume".lightVDebugLog()

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(10000) {
                while (true) {
                    if (!isDealPackage && allPackage.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            cachePackages = allPackage.map { it.copy() }.toMutableList()
                            cachePackages.toString().lightVDebugLog()
                            allPackage.toString().lightVDebugLog()

                            orangeAdapter.dataList = cachePackages as ArrayList<PackageAppMsgEntity>
                            orangeAdapter.notifyDataSetChanged()
                        }
                        break
                    }
                    delay(500)
                }
            }
        }
    }
}