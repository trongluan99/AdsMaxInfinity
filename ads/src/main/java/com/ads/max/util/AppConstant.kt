package com.ads.max.util

import androidx.annotation.StringDef

class AppConstant {
    @StringDef(CollapsibleGravity.TOP, CollapsibleGravity.BOTTOM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CollapsibleGravity {
        companion object {
            const val TOP = "top"
            const val BOTTOM = "bottom"
        }
    }
}
