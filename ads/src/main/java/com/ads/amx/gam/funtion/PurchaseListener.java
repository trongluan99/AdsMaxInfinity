package com.ads.amx.gam.funtion;

public interface PurchaseListener {
    void onProductPurchased(String productId, String transactionDetails);

    void displayErrorMessage(String errorMsg);

    void onUserCancelBilling();
}
