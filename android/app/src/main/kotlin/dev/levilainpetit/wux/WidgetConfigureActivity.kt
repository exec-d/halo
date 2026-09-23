package dev.levilainpetit.wux

/**
 * Écran « Paramètres du widget », ouvert par le lanceur depuis un appui long
 * (Android 12+) ou à la pose d'un widget sur les versions plus anciennes.
 *
 * Même hôte Flutter que [MainActivity] ; le code Dart détecte l'intention
 * `APPWIDGET_CONFIGURE` et ouvre directement l'écran du widget concerné.
 */
class WidgetConfigureActivity : MainActivity()
