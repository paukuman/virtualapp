package com.example.virtualapp.virtual

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Mengelola daftar aplikasi yang di-"clone" ke ruang virtual.
 *
 * Catatan: cloning penuh (menjalankan APK terpisah dalam sandbox proses) memerlukan
 * engine virtualisasi (mis. VirtualApp/FloatMITM). Di sini kita menyediakan
 * manajemen klon + peluncuran aplikasi dari ruang virtual, sehingga UI & alur
 * siap untuk dihubungkan ke engine tersebut di kemudian hari.
 */
class VirtualAppManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class ClonedApp(
        val packageName: String,
        val label: String,
        val clonedAt: Long
    )

    fun listCloned(): List<ClonedApp> {
        val raw = prefs.getString(KEY_CLONED, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ClonedApp(
                    packageName = o.getString("packageName"),
                    label = o.getString("label"),
                    clonedAt = o.getLong("clonedAt")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun isCloned(packageName: String): Boolean =
        listCloned().any { it.packageName == packageName }

    fun clone(packageName: String, label: String): Boolean {
        val current = listCloned().toMutableList()
        if (current.any { it.packageName == packageName }) return false
        current.add(ClonedApp(packageName, label, System.currentTimeMillis()))
        persist(current)
        VirtualCore.registerVirtualPackage(packageName)
        return true
    }

    fun removeClone(packageName: String) {
        val current = listCloned().filterNot { it.packageName == packageName }
        persist(current)
        VirtualCore.unregisterVirtualPackage(packageName)
    }

    /** Daftarkan ulang semua paket clone ke engine (dipanggil saat startup). */
    fun syncToEngine() {
        listCloned().forEach { VirtualCore.registerVirtualPackage(it.packageName) }
    }

    private fun persist(list: List<ClonedApp>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("packageName", c.packageName)
                put("label", c.label)
                put("clonedAt", c.clonedAt)
            })
        }
        prefs.edit().putString(KEY_CLONED, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "virtual_app_store"
        private const val KEY_CLONED = "cloned_apps"
    }
}