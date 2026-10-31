package com.example.virtualapp.virtual

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import java.io.File

/**
 * Context tiruan yang disuntikkan sebagai mBase milik Activity target.
 * Membuat aplikasi target "melihat dirinya sendiri" (package, resources,
 * classloader, data dir terisolasi) alih-alih melihat host.
 */
class VirtualContextWrapper(
    base: Context,
    private val plugin: VPluginManager.LoadedPlugin
) : ContextWrapper(base) {

    private val pluginResources: Resources = plugin.resources
    private val pluginAssets: AssetManager = plugin.resources.assets
    private val pluginClassLoader: ClassLoader = plugin.classLoader

    private val isolatedDataDir: File by lazy { VirtualCore.getVirtualDataDir(plugin.packageName) }

    override fun getDataDir(): File = isolatedDataDir

    override fun getPackageName(): String = plugin.packageName

    override fun getOpPackageName(): String = plugin.packageName

    override fun getResources(): Resources = pluginResources

    override fun getAssets(): AssetManager = pluginAssets

    override fun getClassLoader(): ClassLoader = pluginClassLoader

    override fun getApplicationInfo(): ApplicationInfo = plugin.applicationInfo

    override fun getPackageManager(): PackageManager = baseContext.packageManager

    override fun getFilesDir(): File = File(dataDir, "files").apply { mkdirs() }

    override fun getCacheDir(): File = File(dataDir, "cache").apply { mkdirs() }

    override fun getNoBackupFilesDir(): File = File(dataDir, "no_backup").apply { mkdirs() }

    override fun getCodeCacheDir(): File = File(dataDir, "code_cache").apply { mkdirs() }

    override fun getDatabasePath(name: String): File =
        File(File(dataDir, "databases").apply { mkdirs() }, name)

    override fun getDir(name: String, mode: Int): File =
        File(dataDir, "app_$name").apply { mkdirs() }

    override fun getApplicationContext(): Context = this

    companion object {
        private const val TAG = "VirtualContextWrapper"
    }
}