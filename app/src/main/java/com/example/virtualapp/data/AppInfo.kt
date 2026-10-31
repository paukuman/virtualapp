package com.example.virtualapp.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val versionName: String?,
    val uid: Int,
    val icon: Drawable?,
    val isSystem: Boolean
)