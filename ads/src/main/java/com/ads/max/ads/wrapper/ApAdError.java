package com.ads.max.ads.wrapper;

import com.applovin.mediation.MaxError;

public class ApAdError {

    private MaxError maxError;
    private String message = "";

    public ApAdError(MaxError maxError) {
        this.maxError = maxError;
    }

    public ApAdError(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        if (maxError != null)
            return maxError.getMessage();
        if (!message.isEmpty())
            return message;
        return "unknown error";
    }

    public MaxError getMaxError() {
        return maxError;
    }
}
