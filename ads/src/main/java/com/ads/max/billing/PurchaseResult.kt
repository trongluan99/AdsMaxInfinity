package com.ads.max.billing

class PurchaseResult {
    var orderId: String? = null
    var packageName: String? = null
    var productId: List<String>? = null
    var purchaseTime: Long = 0
    var purchaseState: Int = 0
    var purchaseToken: String? = null
    var quantity: Int = 0
    var isAutoRenewing: Boolean = false
    var isAcknowledged: Boolean = false

    constructor(packageName: String?, productId: List<String>?, purchaseState: Int, autoRenewing: Boolean) {
        this.packageName = packageName
        this.productId = productId
        this.purchaseState = purchaseState
        this.isAutoRenewing = autoRenewing
    }

    constructor(
        orderId: String?,
        packageName: String?,
        productId: List<String>?,
        purchaseTime: Long,
        purchaseState: Int,
        purchaseToken: String?,
        quantity: Int,
        autoRenewing: Boolean,
        acknowledged: Boolean
    ) {
        this.orderId = orderId
        this.packageName = packageName
        this.productId = productId
        this.purchaseTime = purchaseTime
        this.purchaseState = purchaseState
        this.purchaseToken = purchaseToken
        this.quantity = quantity
        this.isAutoRenewing = autoRenewing
        this.isAcknowledged = acknowledged
    }
}
