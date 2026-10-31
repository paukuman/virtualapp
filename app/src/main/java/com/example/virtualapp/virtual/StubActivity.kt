package com.example.virtualapp.virtual

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * Activity nyata yang terdaftar di manifest host. Instance-nya di-override
 * oleh VInstrumentation.newActivity menjadi instance Activity milik aplikasi
 * target. Kelas ini hanya dipakai bila hook gagal / sebagai fallback.
 */
class StubActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w("StubActivity", "StubActivity dibuat langsung (hook tidak aktif?)")
        finish()
    }
}