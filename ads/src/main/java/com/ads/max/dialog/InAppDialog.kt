package com.ads.max.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.ads.max.R
import com.ads.max.billing.AppPurchase

class InAppDialog(private val mContext: Context) : Dialog(mContext, R.style.AppTheme) {
    private var callback: ICallback? = null

    fun setCallback(callback: ICallback?) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_inapp)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.iv_close)?.setOnClickListener {
            onBackPressed()
        }

        findViewById<View>(R.id.tv_purchase)?.setOnClickListener {
            callback?.onPurcharse()
        }
        val tvOldPrice = findViewById<TextView>(R.id.tv_old_price)
        val tvPrice = findViewById<TextView>(R.id.tv_price)
        if (AppPurchase.getInstance().discount == 1.0) {
            tvOldPrice?.visibility = View.GONE
            findViewById<View>(R.id.view_split)?.visibility = View.GONE
        } else {
            tvOldPrice?.visibility = View.VISIBLE
            findViewById<View>(R.id.view_split)?.visibility = View.VISIBLE
        }
        tvPrice?.text = AppPurchase.getInstance().getPrice(AppPurchase.PRODUCT_ID_TEST)
        if (tvOldPrice != null) {
            tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    interface ICallback {
        fun onPurcharse()
    }
}
