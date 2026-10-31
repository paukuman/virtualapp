package com.example.virtualapp.virtual

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * Inti engine virtualisasi minimal.
 *
 * Bertanggung jawab:
 *  - menyimpan context host
 *  - memasang hook pada ActivityThread.mInstrumentation agar peluncuran Activity
 *    yang menunjuk ke komponen aplikasi target bisa dibelokkan ke StubActivity
 *  - memuat APK target (DexClassLoader + Resources) lewat [VPluginManager]
 */
object VirtualCore {

    private const val TAG = "VirtualCore"
    const val STUB_PACKAGE = "com.example.virtualapp"
    const val STUB_ACTIVITY = "$STUB_PACKAGE.virtual.StubActivity"

    private lateinit var hostContext: Application
    private var hooked = false

    lateinit var pluginManager: VPluginManager
        private set

    val isAttached: Boolean
        get() = this::hostContext.isInitialized

    fun attachBaseContext(application: Application) {
        hostContext = application
        pluginManager = VPluginManager(application)
        hookInstrumentation()
    }

    fun getContext(): Context = hostContext

    /** Direktori data terisolasi untuk tiap package target. */
    fun getVirtualDataDir(packageName: String): File {
        val dir = File(hostContext.filesDir, "virtual/$packageName")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getVirtualApkDir(): File {
        val dir = File(hostContext.filesDir, "virtual_apk")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // ---- Registry paket yang dikelola sandbox ----
    private val virtualPackages = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    fun registerVirtualPackage(packageName: String) {
        virtualPackages.add(packageName)
    }

    fun unregisterVirtualPackage(packageName: String) {
        virtualPackages.remove(packageName)
    }

    fun isVirtualPackage(packageName: String?): Boolean =
        packageName != null && virtualPackages.contains(packageName)

    /**
     * Mengganti ActivityThread.mInstrumentation dengan [VInstrumentation].
     * Dilakukan via refleksi (API-stable sejak lama, meski bukan kontrak resmi).
     */
    private fun hookInstrumentation() {
        if (hooked) return
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThreadClass
                .getMethod("currentActivityThread")
                .invoke(null)

            val mInstrumentationField = activityThreadClass
                .getDeclaredField("mInstrumentation")
                .apply { isAccessible = true }

            val original = mInstrumentationField.get(currentActivityThread) as Instrumentation
            val replacement = VInstrumentation(original, hostContext)
            mInstrumentationField.set(currentActivityThread, replacement)
            hooked = true
            android.util.Log.i(TAG, "Instrumentation hooked (API ${Build.VERSION.SDK_INT})")
        } catch (t: Throwable) {
            android.util.Log.e(TAG, "Gagal hook instrumentation", t)
        }
    }

    /** Membaca path APK sumber dari package target. */
    fun sourceDirOf(packageName: String): String? {
        return try {
            hostContext.packageManager
                .getApplicationInfo(packageName, 0)
                .sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}