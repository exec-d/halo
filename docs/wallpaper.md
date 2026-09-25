# Fonds d'écran, écran de veille, tuiles et raccourcis

*[English](en/wallpaper.md)*

Halo propose deux fonds d'écran animés (Circuit et le plan technique de la
moto de Tron), un écran de veille, trois tuiles de réglages rapides et des raccourcis sur son icône.
L'intensité (Discret, Normal, Vif) est commune à tous les fonds.

## Circuit

L'intérieur d'un Pixel 7, vu à travers l'écran, en schéma néon aux couleurs
du téléphone. Il se choisit dans Halo (section *Fond d'écran*), pour l'accueil,
l'écran de verrouillage ou les deux.

### Ce qui est dessiné

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

### Ce qui bouge

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

### Réglages

**Intensité** : Discret (par défaut), Normal ou Vif. Discret garde les
widgets et les icônes lisibles par-dessus.

### Batterie du téléphone

- Rien ne tourne quand le fond n'est pas visible.
- L'animation ne tourne en continu que pendant un mouvement, une impulsion,
  l'allumage ou une charge ; sinon, le fond est une image fixe.
- Les parties fixes sont dessinées une fois, en masques `ALPHA_8` teintés au
  dessin (halo à demi-résolution).

### Code

`android/app/src/main/kotlin/dev/levilainpetit/wux/wallpaper/` :

| Fichier | Rôle |
| --- | --- |
| `CircuitScene.kt` | La géométrie (vue de dos, retournée), les masques, les parcours des impulsions |
| `CircuitPainter.kt` | Une image : plans décalés, batterie, antennes, impulsions, allumage, reflet |
| `HaloWallpaperService.kt` | Capteur, batterie, trafic, signal, cadence des images |
| `WallpaperPreview.kt` | L'aperçu de l'application et l'ouverture de l'écran système |
| `WallpaperSettings.kt` | L'intensité |

## Les plans techniques

Le fond Tron dessine, en plan technique, un objet de film culte : papier
quadrillé, cadre repéré, vues cotées, repères numérotés et cartouche
(échelle, planche, date du jour). Comme Circuit et les widgets, il prend
les couleurs du téléphone. D'autres objets suivront, un à un.

- **Le tracé** : à chaque allumage de l'écran, le plan se dessine trait par
  trait en un peu plus de deux secondes (`Pen` compte la longueur de chaque
  trait et n'en dessine que ce que le temps écoulé permet).
- **Ce qui bouge** : les pièces de l'objet (roues, turbines, voyants…).
- **Inclinaison** : les vues glissent un peu, en sens opposés.

| Planche | Film | Objet | Ce qui bouge | Code |
| --- | --- | --- | --- | --- |
| 01 | Tron : l'héritage | Moto de lumière | Roues, liseré, ruban de lumière | `LightCycle.kt` |

Le cadre commun est dans `wallpaper/blueprint/Blueprint.kt` ; chaque objet
dans son fichier, à côté. Pour voir un plan sans téléphone,
`tool/scenes/render.sh` le dessine en PNG sur l'ordinateur (voir l'en-tête
du script) ; `render.sh --thumbs` refait les miniatures du sélecteur.

## Écran de veille

Pendant la charge ou sur un socle, Android peut afficher un écran de veille
(*Paramètres → Écran → Écran de veille*, choisir « Halo ») : une grande
horloge néon sur le décor Circuit, avec la date, la prochaine alarme, le
prochain événement et la météo. Le texte se déplace un peu chaque minute
pour ne pas marquer l'écran. Code : `dream/`.

## Tuiles de réglages rapides

À ajouter depuis le volet des réglages rapides (crayon) :

| Tuile | Affiche | Toucher |
| --- | --- | --- |
| Météo | Température et temps qu'il fait | Retélécharge les prévisions |
| Fond Halo | L'intensité du fond appliqué | Discret → Normal → Vif |
| Batterie | Niveau et estimation | Ouvre l'utilisation de la batterie |

Code : `tiles/QuickTiles.kt`.

## Raccourcis de l'icône

Un appui long sur l'icône Halo : **Fond d'écran**, **Widgets**, **Réglages**.
Chacun ouvre l'application sur l'écran correspondant (`halo://…`,
`res/xml/shortcuts.xml`).
