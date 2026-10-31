package com.example.virtualapp.virtual

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Menyiapkan Intent yang menunjuk ke StubActivity host, sambil menyimpan
 * identitas komponen target di extras. VInstrumentation kemudian membaca
 * extras ini pada saat Activity hendak dibuat.
 */
object VirtualLauncher {

    private const val TAG = "VirtualLauncher"
    const val EXTRA_TARGET_PKG = "virtual.target.package"
    const val EXTRA_TARGET_CLASS = "virtual.target.class"

    data class Target(val packageName: String, val className: String)

    /**
     * Meluncurkan launcher activity milik aplikasi target di dalam proses host.
     * Mengembalikan true jika berhasil memulai.
     */
    fun launch(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            Log.e(TAG, "Tidak ada launcher intent untuk $packageName")
            return false
        }
        val component = launchIntent.component
        val className = component?.className
            ?: resolveLauncherClass(packageName, context)
        if (className == null) {
            Log.e(TAG, "Tidak bisa resolve launcher class untuk $packageName")
            return false
        }
        return launchActivity(context, packageName, className)
    }

    fun launchActivity(context: Context, packageName: String, className: String): Boolean {
        return try {
            val plugin = VirtualCore.pluginManager.load(packageName)
            if (plugin == null) {
                Log.e(TAG, "Plugin gagal dimuat: $packageName")
                return false
            }
            val stub = Intent().apply {
                component = ComponentName(VirtualCore.STUB_PACKAGE, VirtualCore.STUB_ACTIVITY)
                putExtra(EXTRA_TARGET_PKG, packageName)
                putExtra(EXTRA_TARGET_CLASS, className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(stub)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal meluncurkan $packageName/$className", t)
            false
        }
    }

    fun resolveTarget(intent: Intent?): Target? {
        if (intent == null) return null
        val pkg = intent.getStringExtra(EXTRA_TARGET_PKG) ?: return null
        val cls = intent.getStringExtra(EXTRA_TARGET_CLASS) ?: return null
        return Target(pkg, cls)
    }

    private fun resolveLauncherClass(packageName: String, context: Context): String? {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(
                packageName,
                android.content.pm.PackageManager.GET_ACTIVITIES
            )
            val activities = info.activities ?: return null
            for (a in activities) {
                for (f in a.javaClass.fields) { /* no-op, keep simple */ }
            }
            // Heuristik: cari activity dengan intent-filter MAIN/LAUNCHER.
            // PackageManager tidak mengekspos intent-filter per activity via API lama,
            // jadi kita gunakan getLaunchIntentForPackage sebagai sumber utama.
            activities.firstOrNull()?.name
        } catch (e: Exception) {
            null
        }
    }
}