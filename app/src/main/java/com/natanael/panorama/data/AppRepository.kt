package com.natanael.panorama.data

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.natanael.panorama.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    suspend fun getInstalledApps(
        hiddenKeys: Set<String>,
        favoriteKeys: Set<String>
    ): List<AppItem> = withContext(Dispatchers.Default) {
        val myUserHandle = Process.myUserHandle()
        val userProfiles: List<UserHandle> = try {
            userManager.userProfiles
        } catch (e: Exception) {
            listOf(myUserHandle)
        }

        val appItems = mutableListOf<AppItem>()

        for (userHandle in userProfiles) {
            val isClone = (userHandle != myUserHandle)
            val activities = try {
                launcherApps.getActivityList(null, userHandle)
            } catch (e: Exception) {
                emptyList()
            }

            for (info in activities) {
                val pkgName = info.componentName.packageName
                if (pkgName == context.packageName) continue

                val className = info.componentName.className
                val uniqueKey = "${pkgName}_${className}_${userHandle.hashCode()}"
                val label = info.label.toString()

                val badgedIconDrawable = try {
                    info.getBadgedIcon(0)
                } catch (e: Exception) {
                    try {
                        info.getIcon(0)
                    } catch (ex: Exception) {
                        null
                    }
                }

                val iconBitmap = badgedIconDrawable?.let { drawableToImageBitmap(it, 96) }

                appItems.add(
                    AppItem(
                        uniqueKey = uniqueKey,
                        packageName = pkgName,
                        activityName = className,
                        componentName = info.componentName,
                        userHandle = userHandle,
                        label = label,
                        isClone = isClone,
                        iconBitmap = iconBitmap,
                        isHidden = hiddenKeys.contains(uniqueKey),
                        isFavorite = favoriteKeys.contains(uniqueKey)
                    )
                )
            }
        }

        appItems.sortedBy { it.label.lowercase() }
    }

    private fun drawableToImageBitmap(drawable: Drawable, targetSizePx: Int = 96): ImageBitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, targetSizePx, targetSizePx)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
