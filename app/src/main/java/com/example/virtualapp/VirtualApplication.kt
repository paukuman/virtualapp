package com.example.virtualapp

import android.app.Application
import com.example.virtualapp.virtual.VirtualCore

class VirtualApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        VirtualCore.attachBaseContext(this)
    }
}