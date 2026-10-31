package com.example.virtualapp.virtual

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Hook ke singleton IActivityTaskManager (Android 10+) / IActivityManager (<=9)
 * memakai dynamic Proxy. Tidak ada referensi compile-time ke hidden API:
 * semuanya lewat refleksi, sehingga aman dikompilasi terhadap android.jar publik.
 *
 * Saat app target memanggil startActivity ke komponennya sendiri, Intent
 * di-rewrite menjadi StubActivity + extras target, sehingga VInstrumentation
 * dapat membuat instance Activity target yang benar.
 */
object ActivityManagerHook {

    private const val TAG = "ActivityManagerHook"
    @Volatile
    private var installed = false

    fun install() {
        if (installed) return
        HiddenApiBypass.exempt()

        // Android 10+ : android.app.ActivityTaskManager.IActivityTaskManagerSingleton
        // Interface: android.app.IActivityTaskManager
        if (installForSingleton(
                amClassName = "android.app.ActivityTaskManager",
                singletonFieldName = "IActivityTaskManagerSingleton",
                interfaceClassName = "android.app.IActivityTaskManager"
            )
        ) {
            installed = true
            Log.i(TAG, "Hook IActivityTaskManager terpasang")
            return
        }
        // Android <= 9 : android.app.ActivityManager.IActivityManagerSingleton
        // Interface: android.app.IActivityManager
        if (installForSingleton(
                amClassName = "android.app.ActivityManager",
                singletonFieldName = "IActivityManagerSingleton",
                interfaceClassName = "android.app.IActivityManager"
            )
        ) {
            installed = true
            Log.i(TAG, "Hook IActivityManager terpasang")
            return
        }
        Log.e(TAG, "Tidak bisa menemukan singleton activity manager")
    }

    private fun installForSingleton(
        amClassName: String,
        singletonFieldName: String,
        interfaceClassName: String
    ): Boolean {
        return try {
            val amClass = Class.forName(amClassName)
            val singletonField = amClass.getDeclaredField(singletonFieldName).apply {
                isAccessible = true
            }
            val singleton = singletonField.get(null) ?: return false

            val singletonClass = Class.forName("android.util.Singleton")
            val mInstanceField = singletonClass.getDeclaredField("mInstance").apply {
                isAccessible = true
            }
            val original = mInstanceField.get(singleton) ?: return false

            val interfaceClass = Class.forName(interfaceClassName)
            val proxy = Proxy.newProxyInstance(
                interfaceClass.classLoader,
                arrayOf(interfaceClass),
                ActivityManagerInvocationHandler(original)
            )
            mInstanceField.set(singleton, proxy)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Gagal hook $amClassName.$singletonFieldName", t)
            false
        }
    }

    private class ActivityManagerInvocationHandler(
        private val base: Any
    ) : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any?>?): Any? {
            try {
                if (args != null) rewriteIntentsInArgs(args)
            } catch (t: Throwable) {
                Log.e(TAG, "rewrite gagal", t)
            }
            return try {
                method?.invoke(base, *(args ?: emptyArray()))
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.targetException
            }
        }

        private fun rewriteIntentsInArgs(args: Array<out Any?>) {
            for (i in args.indices) {
                val a = args[i]
                if (a is Intent) rewrite(a)
            }
        }

        private fun rewrite(intent: Intent) {
            val comp = intent.component ?: return
            val pkg = comp.packageName ?: return
            if (!VirtualCore.isVirtualPackage(pkg)) return
            if (comp.className == VirtualCore.STUB_ACTIVITY) return

            Log.d(TAG, "Rewrite ${comp.packageName}/${comp.className} -> Stub")
            intent.putExtra(VirtualLauncher.EXTRA_TARGET_PKG, comp.packageName)
            intent.putExtra(VirtualLauncher.EXTRA_TARGET_CLASS, comp.className)
            intent.component = ComponentName(
                VirtualCore.STUB_PACKAGE, VirtualCore.STUB_ACTIVITY
            )
        }
    }
}