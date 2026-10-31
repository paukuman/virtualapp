package com.example.virtualapp.virtual

import android.os.Build
import android.util.Log

/**
 * Best-effort pelonggaran pembatasan hidden API Android 9+ agar refleksi ke
 * field/method framework (mis. IActivityTaskManagerSingleton) tidak diblokir.
 */
object HiddenApiBypass {

    private const val TAG = "HiddenApiBypass"

    fun exempt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        try {
            val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")
            val getRuntime = vmRuntimeClass.getDeclaredMethod("getRuntime")
            getRuntime.isAccessible = true
            val runtime = getRuntime.invoke(null)
            val setExemptions = vmRuntimeClass.getDeclaredMethod(
                "setHiddenApiExemptions", Array<String>::class.java
            )
            setExemptions.isAccessible = true
            setExemptions.invoke(runtime, arrayOf("L"))
            Log.i(TAG, "setHiddenApiExemptions(\"L\") berhasil")
        } catch (t: Throwable) {
            Log.w(TAG, "Gagal exempt hidden API via VMRuntime", t)
            try {
                val forName = Class::class.java.getDeclaredMethod(
                    "forName", String::class.java
                )
                val vmRuntime = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
                val getRuntime = vmRuntime.getDeclaredMethod("getRuntime")
                getRuntime.isAccessible = true
                val runtime = getRuntime.invoke(null)
                val method = vmRuntime.getDeclaredMethod(
                    "setHiddenApiExemptions", Array<String>::class.java
                )
                method.isAccessible = true
                method.invoke(runtime, arrayOf("L"))
                Log.i(TAG, "setHiddenApiExemptions(\"L\") berhasil (fallback)")
            } catch (t2: Throwable) {
                Log.w(TAG, "Fallback hidden API exempt juga gagal", t2)
            }
        }
    }
}