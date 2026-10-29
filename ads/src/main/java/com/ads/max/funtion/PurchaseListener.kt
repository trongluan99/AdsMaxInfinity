package com.ads.max.funtion

interface PurchaseListener {
    fun onProductPurchased(productId: String?, transactionDetails: String?)
    fun displayErrorMessage(errorMsg: String?)
    fun onUserCancelBilling()
}
