package dev.levilainpetit.wux.wallpaper

import dev.levilainpetit.wux.wallpaper.blueprint.BlueprintScene
import dev.levilainpetit.wux.wallpaper.blueprint.FluxCapacitor
import dev.levilainpetit.wux.wallpaper.blueprint.LightCycle

/** Fond d'écran animé en plan technique : la moto de lumière de Tron. */
class TronWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(LightCycle())
}

/** Fond d'écran animé en plan technique : le convecteur temporel de Retour vers le futur. */
class FluxWallpaperService : SceneWallpaperService() {
    override fun createScene(): LiveScene = BlueprintScene(FluxCapacitor())
}
