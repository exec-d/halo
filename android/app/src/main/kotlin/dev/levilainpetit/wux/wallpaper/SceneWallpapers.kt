package dev.levilainpetit.wux.wallpaper

import dev.levilainpetit.wux.wallpaper.blueprint.BlueprintScene
import dev.levilainpetit.wux.wallpaper.blueprint.LightCycle

/** Fond d'écran animé en plan technique : la moto de lumière de Tron. */
class TronWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(LightCycle())
}
