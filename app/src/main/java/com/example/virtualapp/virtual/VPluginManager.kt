package com.example.virtualapp.virtual

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Memuat APK target sebagai "plugin": class loader + resource terpisah,
 * tanpa memasang APK tersebut ke sistem.
 */
class VPluginManager(private val hostContext: Context) {

    private val cache = ConcurrentHashMap<String, LoadedPlugin>()

    inner class LoadedPlugin(
        val packageName: String,
        val apkPath: String,
        val classLoader: DexClassLoader,
        val resources: Resources,
        val applicationInfo: ApplicationInfo
    )

    fun isLoaded(packageName: String): Boolean = cache.containsKey(packageName)

    fun load(packageName: String): LoadedPlugin? {
        cache[packageName]?.let { return it }
        return try {
            val apkPath = VirtualCore.sourceDirOf(packageName) ?: run {
                Log.e(TAG, "APK tidak ditemukan untuk $packageName")
                return null
            }

            val optimizedDir = File(VirtualCore.getVirtualDataDir(packageName), "dex")
            if (!optimizedDir.exists()) optimizedDir.mkdirs()

            val classLoader = DexClassLoader(
                apkPath,
                optimizedDir.absolutePath,
                null,
                hostContext.classLoader
            )

            val resources = buildResources(apkPath, hostContext)
            val appInfo = hostContext.packageManager
                .getApplicationInfo(packageName, 0)

            val plugin = LoadedPlugin(
                packageName = packageName,
                apkPath = apkPath,
                classLoader = classLoader,
                resources = resources,
                applicationInfo = appInfo
            )
            cache[packageName] = plugin
            Log.i(TAG, "Plugin loaded: $packageName")
            plugin
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal load plugin $packageName", t)
            null
        }
    }

    private fun buildResources(apkPath: String, base: Context): Resources {
        val am = AssetManager::class.java.getDeclaredConstructor().newInstance()
        val addAssetPath = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
        addAssetPath.isAccessible = true
        addAssetPath.invoke(am, apkPath)
        val baseRes = base.resources
        return Resources(am, baseRes.displayMetrics, baseRes.configuration)
    }

    fun packageInfo(packageName: String): PackageInfo? = try {
        hostContext.packageManager.getPackageInfo(packageName, 0)
    } catch (e: Exception) {
        null
    }

    companion object {
        private const val TAG = "VPluginManager"
    }
}