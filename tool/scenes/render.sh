#!/bin/bash
# Dessine les fonds d'écran animés de Halo (GridScene, MegacityScene…) en PNG,
# sans téléphone ni SDK Android : les scènes sont compilées contre une
# imitation d'android.graphics faite avec Java2D (stub/Graphics.kt). Le rendu
# est proche de celui du téléphone, pas identique (flous approchés).
#
#   tool/scenes/render.sh                 toutes les scènes, dans build/scenes/
#   tool/scenes/render.sh grid,code       quelques scènes
#   tool/scenes/render.sh --thumbs        les miniatures du sélecteur d'Android
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
  "$SCENES"/LiveScene.kt "$SCENES"/SceneKit.kt "$SCENES"/GridScene.kt "$SCENES"/MegacityScene.kt \
  "$SCENES"/CodeScene.kt "$SCENES"/NeonScene.kt "$SCENES"/SentinelScene.kt
STDLIB=$(dirname "$(readlink -f "$(command -v "$KOTLINC")")")/../lib/kotlin-stdlib.jar
if [[ "${1:-}" == "--thumbs" ]]; then
  java -Djava.awt.headless=true -cp "$BUILD/classes:$STDLIB" dev.levilainpetit.wux.wallpaper.RenderKt --thumbs "$ROOT/android/app/src/main/res/drawable-nodpi"
else
  java -Djava.awt.headless=true -cp "$BUILD/classes:$STDLIB" dev.levilainpetit.wux.wallpaper.RenderKt "$BUILD" "${1:-}"
fi
