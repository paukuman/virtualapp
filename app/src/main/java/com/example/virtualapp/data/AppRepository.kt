package com.example.virtualapp.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages
            .asSequence()
            .filter { it.packageName != context.packageName }
            .map { ai ->
                AppInfo(
                    label = pm.getApplicationLabel(ai).toString(),
                    packageName = ai.packageName,
                    versionName = runCatching {
                        pm.getPackageInfo(ai.packageName, 0).versionName
                    }.getOrNull(),
                    uid = ai.uid,
                    icon = runCatching { pm.getApplicationIcon(ai) }.getOrNull(),
                    isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun getAppInfo(packageName: String): AppInfo? {
        val pm = context.packageManager
        return runCatching {
            val ai = pm.getApplicationInfo(packageName, 0)
            AppInfo(
                label = pm.getApplicationLabel(ai).toString(),
                packageName = ai.packageName,
                versionName = pm.getPackageInfo(packageName, 0).versionName,
                uid = ai.uid,
                icon = pm.getApplicationIcon(ai),
                isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }.getOrNull()
    }
}