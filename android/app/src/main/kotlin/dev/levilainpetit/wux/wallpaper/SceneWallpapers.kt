package dev.levilainpetit.wux.wallpaper

import dev.levilainpetit.wux.wallpaper.blueprint.*

/** Fond d'écran animé en plan technique : LightCycle. */
class TronWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(LightCycle())
}

/** Fond d'écran animé en plan technique : Spinner. */
class SpinnerWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(Spinner())
}

/** Fond d'écran animé en plan technique : Hovercraft. */
class NebuchadnezzarWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(Hovercraft())
}

/** Fond d'écran animé en plan technique : Hal. */
class HalWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(Hal())
}

/** Fond d'écran animé en plan technique : FluxCapacitor. */
class FluxWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(FluxCapacitor())
}

/** Fond d'écran animé en plan technique : Endurance. */
class EnduranceWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(Endurance())
}

/** Fond d'écran animé en plan technique : PowerLoader. */
class LoaderWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(PowerLoader())
}

/** Fond d'écran animé en plan technique : KanedaBike. */
class AkiraWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(KanedaBike())
}

/** Fond d'écran animé en plan technique : Endoskeleton. */
class T800WallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(Endoskeleton())
}

/** Fond d'écran animé en plan technique : ArcReactor. */
class ArcWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(ArcReactor())
}
