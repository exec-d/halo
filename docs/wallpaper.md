# Le fond d'écran animé Circuit

L'intérieur d'un Pixel 7, vu à travers l'écran, en schéma néon aux couleurs
du téléphone. Il se choisit dans Halo (section *Fond d'écran*), pour l'accueil,
l'écran de verrouillage ou les deux.

## Ce qui est dessiné

Quatre plans, du fond vers le verre :

1. **Châssis** : cadre, vis, barre photo qui traverse le dos, boutons marche
   et volume à droite, tiroir SIM à gauche, bobine de recharge sans fil.
2. **Batterie**, avec son niveau réel.
3. **Cartes** : carte mère (processeur, mémoire, modem, gestion d'énergie,
   objectifs arrière côte à côte dans leur pastille, flash), carte du bas
   (USB-C, haut-parleur, vibreur, micro), câble plat, pistes et petits
   composants.
4. **Verre** : caméra frontale dans le poinçon, écouteur, capteur de
   luminosité, lecteur d'empreinte.

Le décor ne contient aucun texte, pour ne pas se mêler à celui des widgets.

## Ce qui bouge

- **Inclinaison** : les plans glissent d'autant plus qu'ils sont profonds
  (capteur de gravité), et un reflet traverse le verre. La position de repos
  suit lentement la main : c'est un mouvement qui fait bouger, pas une
  posture.
- **Batterie** : le niveau réel ; pendant la charge, il respire et des
  impulsions montent du port USB-C.
- **Réseau** : des impulsions courent de l'antenne au processeur quand des
  données passent (`TrafficStats`) ; les antennes brillent selon la force du
  signal.
- **Allumage** : à chaque allumage de l'écran, le décor est là tout de suite,
  puis composants et pistes s'illuminent un à un depuis le processeur.
- **Écran de verrouillage** : le haut est assombri pour l'horloge.

## Réglages

**Intensité** : Discret (par défaut), Normal ou Vif. Discret garde les
widgets et les icônes lisibles par-dessus.

## Batterie du téléphone

- Rien ne tourne quand le fond n'est pas visible.
- L'animation ne tourne en continu que pendant un mouvement, une impulsion,
  l'allumage ou une charge ; sinon, le fond est une image fixe.
- Les parties fixes sont dessinées une fois, en masques `ALPHA_8` teintés au
  dessin (halo à demi-résolution).

## Code

`android/app/src/main/kotlin/dev/levilainpetit/wux/wallpaper/` :

| Fichier | Rôle |
| --- | --- |
| `CircuitScene.kt` | La géométrie (vue de dos, retournée), les masques, les parcours des impulsions |
| `CircuitPainter.kt` | Une image : plans décalés, batterie, antennes, impulsions, allumage, reflet |
| `HaloWallpaperService.kt` | Capteur, batterie, trafic, signal, cadence des images |
| `WallpaperPreview.kt` | L'aperçu de l'application et l'ouverture de l'écran système |
| `WallpaperSettings.kt` | L'intensité |
