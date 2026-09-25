package dev.levilainpetit.wux.wallpaper

/** Fond d'écran animé Horizon (HorizonScene). */
class HorizonWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = HorizonScene(this)
}

/** Fond d'écran animé Ciel (SkyScene). */
class SkyWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = SkyScene(this)
}
