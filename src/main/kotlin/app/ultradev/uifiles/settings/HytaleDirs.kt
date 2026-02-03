package app.ultradev.uifiles.settings

import kotlin.io.path.Path
import kotlin.io.path.exists

object HytaleDirs {
    fun defaultHytaleDirectory(): String {
        val home = System.getProperty("user.home")
        val os = System.getProperty("os.name")

        return when {
            os.startsWith("Windows") ->
                "$home/AppData/Roaming/Hytale"
            os.startsWith("Mac") ->
                "$home/Library/Application Support/Hytale"
            else -> {
                val flatpak = Path("$home/.var/app/com.hypixel.HytaleLauncher/data/Hytale")
                if (flatpak.exists()) flatpak.toString()
                else "$home/.local/share/Hytale"
            }
        }
    }

    fun defaultAssetsZipPath(hytaleDir: String, patchline: String, build: String): String {
        return "${hytaleDir}/install/${patchline}/package/game/${build}/Assets.zip"
    }
}