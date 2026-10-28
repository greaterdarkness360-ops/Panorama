package com.natanael.panorama.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.natanael.panorama.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(
        hiddenPackages: Set<String>,
        favoritePackages: Set<String>
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)

        val apps = resolveInfoList
            .filter { resolveInfo ->
                resolveInfo.activityInfo.packageName != context.packageName
            }
            .map { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                val actName = resolveInfo.activityInfo.name
                val label = resolveInfo.loadLabel(packageManager).toString()
                val icon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }

                AppItem(
                    packageName = pkgName,
                    activityName = actName,
                    label = label,
                    icon = icon,
                    isHidden = hiddenPackages.contains(pkgName),
                    isFavorite = favoritePackages.contains(pkgName)
                )
            }
            .sortedBy { it.label.lowercase() }

        apps
    }
}
