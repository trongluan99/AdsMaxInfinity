package com.ads.max.module

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ads.max.applovin.AppLovin
import com.ads.max.applovin.AppLovinCallback
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdView

class DemoTabFragment : Fragment() {

    companion object {
        private const val TAG = "DemoTabFragment"
        private const val ARG_TAB_INDEX = "arg_tab_index"
        private const val ARG_TAB_TITLE = "arg_tab_title"

        fun newInstance(tabIndex: Int, tabTitle: String): DemoTabFragment {
            return DemoTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_INDEX, tabIndex)
                    putString(ARG_TAB_TITLE, tabTitle)
                }
            }
        }
    }

    private var tabIndex: Int = 1
    private var tabTitle: String = "Tab 1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabIndex = arguments?.getInt(ARG_TAB_INDEX) ?: 1
        tabTitle = arguments?.getString(ARG_TAB_TITLE) ?: "Tab 1"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_demo_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tv_tab_title)
        val tvDesc = view.findViewById<TextView>(R.id.tv_tab_desc)
        val tvStatus = view.findViewById<TextView>(R.id.tv_ad_status)

        tvTitle.text = tabTitle
        tvDesc.text = "Fragment #$tabIndex with its own independent Native Ad"
        tvStatus.text = "Ad Status: Loading ad for $tabTitle..."

        // Load Native Ad cho Fragment này bằng hàm loadNativeFragment
        AppLovin.getInstance().loadNativeFragment(
            requireActivity(),
            BuildConfig.ad_native,
            view
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy an toàn ad của RIÊNG Fragment này, không ảnh hưởng đến các Fragment khác
        view?.let {
            Log.d(TAG, "[$tabTitle] onDestroyView: Destroying native ad for this tab")
            AppLovin.getInstance().destroyNativeAd()
        }
    }
}
