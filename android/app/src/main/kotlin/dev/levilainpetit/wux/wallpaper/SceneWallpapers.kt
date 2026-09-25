package dev.levilainpetit.wux.wallpaper

/** Fond d'écran animé Grille (GridScene). */
class GridWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = GridScene()
}

/** Fond d'écran animé Mégapole (MegacityScene). */
class MegacityWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = MegacityScene()
}

/** Fond d'écran animé Code (CodeScene). */
class CodeWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = CodeScene()
}

/** Fond d'écran animé Néon (NeonScene). */
class NeonWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = NeonScene()
}

/** Fond d'écran animé Sentinelle (SentinelScene). */
class SentinelWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = SentinelScene()
}
