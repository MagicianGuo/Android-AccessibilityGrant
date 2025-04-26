package com.magicianguo.accessibilitygrant.bean

import android.graphics.drawable.Drawable

data class AccessibilityItemBean(
    val packageName: String,
    val applicationIcon: Drawable,
    val applicationLabel: CharSequence,
    val enabled: Boolean,
    val serviceLabel: String,
    val serviceName: String
)