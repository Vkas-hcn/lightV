package com.light.lightV.orange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.light.lightV.BuildConfig
import com.light.lightV.databinding.FragmentYellowBinding
import com.light.lightV.green.getKv
import com.light.lightV.green.toNow
import com.light.lightV.indigo.isLoadingSever
import com.light.lightV.indigo.loadSevers
import com.light.lightV.purple.RedDialog
import com.light.lightV.red.BlueActivity
import com.light.lightV.red.GreenActivity
import com.light.lightV.red.noAllowLaunchAgain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class YellowFragment : Fragment() {

    private lateinit var binding: FragmentYellowBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYellowBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lightDate.text = "".toNow()

        binding.lightCode.text = "User ID:" + "userCode".getKv()
        binding.menuSever.setOnClickListener {

            if (isLoadingSever) {
                disLoading()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        dismissLoading()
                    }
                }
                return@setOnClickListener
            }
            if (!isLoadingSever && "seversSecretString".getKv().isEmpty()) {
                loadSevers()
                disLoading()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        dismissLoading()
                        startActivity(
                            Intent(
                                requireActivity(),
                                GreenActivity::class.java
                            )
                        )
                    }
                }
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    requireActivity(),
                    GreenActivity::class.java
                )
            )
        }
        binding.menuShare.setOnClickListener {
            shareAppLink()
        }
        binding.menuPolicy.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    BlueActivity::class.java
                )
            )
        }
    }

    private fun shareAppLink() {
        val packages =
            if (BuildConfig.DEBUG) "com.light.lightV" else "com.sunrise.fast.secure.link.infinity"
        val appLink = "https://play.google.com/store/apps/details?id=" + packages
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "I found this amazing app, check it out: $appLink"
        )
        noAllowLaunchAgain = true
        startActivity(Intent.createChooser(shareIntent, "Share app link via"))
    }

    private fun disLoading(show: Boolean = false) {
        RedDialog(show = show).show(requireActivity().supportFragmentManager, "RedDialog")
    }

    private fun dismissLoading() {
        (requireActivity().supportFragmentManager.findFragmentByTag("RedDialog") as? DialogFragment)?.dismiss()
    }
}