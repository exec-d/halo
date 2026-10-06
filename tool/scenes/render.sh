#!/bin/bash
# Dessine les fonds d'écran animés de Halo (Circuit et le convecteur) en PNG,
# sans téléphone ni SDK Android : les scènes sont compilées contre une
# imitation d'android.graphics faite avec Java2D (stub/Graphics.kt). Le rendu
# est proche de celui du téléphone, pas identique (flous approchés).
#
#   tool/scenes/render.sh                 tous les fonds, dans build/scenes/
#   tool/scenes/render.sh flux            un seul fond
#   tool/scenes/render.sh --thumbs        les miniatures du sélecteur d'Android
#   tool/scenes/render.sh --frames arc 60 50 [charge]
#                                         une séquence d'images (allumage compris)
#
# Il faut Java et le compilateur Kotlin : `kotlinc` dans le PATH, ou KOTLINC
# vers son exécutable (https://github.com/JetBrains/kotlin/releases).
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
KOTLINC=${KOTLINC:-kotlinc}
SCENES=$ROOT/android/app/src/main/kotlin/dev/levilainpetit/wux/wallpaper
BUILD=$ROOT/build/scenes
mkdir -p "$BUILD/classes"
"$KOTLINC" -nowarn -d "$BUILD/classes" "$ROOT"/tool/scenes/stub/*.kt \
  "$SCENES"/CircuitScene.kt "$SCENES"/CircuitPainter.kt "$SCENES"/CoreArt.kt "$SCENES"/*Core.kt
STDLIB=$(dirname "$(readlink -f "$(command -v "$KOTLINC")")")/../lib/kotlin-stdlib.jar
if [[ "${1:-}" == "--frames" ]]; then
  java -Djava.awt.headless=true -cp "$BUILD/classes:$STDLIB" dev.levilainpetit.wux.wallpaper.RenderKt --frames "$BUILD/frames" "${@:2}"
elif [[ "${1:-}" == "--thumbs" ]]; then
  java -Djava.awt.headless=true -cp "$BUILD/classes:$STDLIB" dev.levilainpetit.wux.wallpaper.RenderKt --thumbs "$ROOT/android/app/src/main/res/drawable-nodpi"
else
  java -Djava.awt.headless=true -cp "$BUILD/classes:$STDLIB" dev.levilainpetit.wux.wallpaper.RenderKt "$BUILD" "${1:-}"
fi
