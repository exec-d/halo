# Fonds d'écran, écran de veille, tuiles et raccourcis

*[English](en/wallpaper.md)*

Halo propose trois fonds d'écran animés (Circuit, et ses variantes Retour
vers le futur et Iron Man), un écran de veille, trois tuiles de réglages rapides et des raccourcis sur son icône.
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
  (accéléromètre), et un reflet traverse le verre. La position de repos
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
  l'allumage ou une charge ; sinon, le fond est une image fixe. Le
  tremblement de la main ne relance pas le dessin : seul un vrai geste le
  fait.
- L'inclinaison vient de l'accéléromètre seul, filtré : le capteur de
  gravité allumerait aussi le gyroscope, bien plus gourmand.
- **Économiseur de batterie** d'Android actif : le fond se fige (ni capteur,
  ni impulsions, ni animation, une image à chaque changement d'état), pour
  les trois fonds.
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

## Retour vers le futur

Le même intérieur de téléphone que Circuit (`CircuitScene.build(…, Core.FLUX)`),
avec le convecteur temporel du film à la place de la batterie et de la
bobine : boîtier à hublot, Y des trois électrodes, câbles qui montent vers la
carte mère. Les cotes du décor (600 × 760 mm) sont ramenées au rectangle de
la batterie. Le service est `FluxWallpaperService`, qui étend
`HaloWallpaperService`.

- **Le convecteur** : une impulsion court de lampe en lampe le long des trois
  bras jusqu'au cœur, qui s'illumine ; plus vite pendant la charge.
- **La jauge**, au-dessus du hublot : dix cases pour le niveau de batterie.
- **Les cartes, plus chargées** : sur la carte mère, les trois afficheurs des
  circuits temporels (destination 2015 · 16:29, présent à l'heure réelle,
  dernier départ 1985 · 01:21) et trois bobines d'alimentation au-dessus des
  câbles ; sur la carte du bas, Mr. Fusion, qui s'éclaire pendant la charge.
- **Le reste est celui de Circuit** : inclinaison, réseau, allumage.
- **Batterie** : à l'écran, le convecteur s'anime à 22 images par seconde
  environ pendant 30 s après l'allumage de l'écran ou un geste sur l'accueil
  (toucher, changement de page), puis ralentit à 5 images par seconde
  jusqu'au geste suivant ; en charge, toujours à pleine cadence. Rien ne tourne quand le fond n'est pas visible.

## Iron Man

Le téléphone porte l'armure (`Core.ARC`, `ArcCore`, `ArcWallpaperService`) :
il redessine tout le téléphone, comme les cœurs des thèmes (voir plus bas).

- **Le cadre** : une ceinture de plaques boulonnées, des vérins blindés à la
  place des boutons, un iris autour de la caméra frontale, un logement
  renforcé pour le port USB, des gaines le long des flancs.
- **Les plaques** : le casque et ses voyants, les clavicules, les pectoraux
  autour du logement du réacteur, les lamelles des côtes, les abdominaux, le
  sternum en vertèbres et deux vérins ; entre elles, une maille hexagonale et
  des conduits d'énergie. Chaque plaque masque celles de derrière.
- **Le réacteur** : logement à dix pans, condensateurs, boîtier vissé,
  couronne graduée, dix bobines à spires et leurs fils, brides, cœur.
- **Il bat** (deux coups, puis un temps) : les arêtes tournées vers lui
  s'éclairent, une onde parcourt la maille et file dans les conduits. Une
  bobine par dixième de batterie. En charge, le courant monte du port au
  réacteur, les anneaux tournent et les vérins se détendent ; avec du réseau,
  les impulsions viennent des coins et les voyants clignotent vite.
- Inclinaison et allumage sont ceux de Circuit ; même cadence que les autres.

Pour voir les fonds sans téléphone, `tool/scenes/render.sh` les dessine en
PNG sur l'ordinateur (voir l'en-tête du script) ; `render.sh --thumbs` refait
les miniatures des fonds dans le sélecteur d'Android.

## Thèmes

Les packs de thème des Pixel (Fond d'écran et style › Pack de thème) sont
réservés à Google : aucune API ne permet à une application d'y figurer.
Halo fait les siens, dans son écran Thèmes (depuis la galerie et l'onglet
Fonds). Un thème porte le nom et l'identifiant de son fond :

| Thème | Palette (cœur, traits, halo) | Sons : sonnerie, notification, alarme |
| --- | --- | --- |
| Circuit | blanc bleuté, bleu ciel, bleu électrique | Bus de données, Impulsion, Démarrage |
| Retour vers le futur | crème, ambre, orange | 88 mph, Flux, Heure de départ |
| Iron Man | ivoire, or, rouge | Répulseur, Interface, Réacteur |
| Physique quantique | lavande, violet clair, violet | Superposition, Intrication, Effondrement |
| Intelligence artificielle | blanc bleuté, bleu ciel, bleu électrique | Inférence, Jeton, Éveil |
| Énergie atomique | crème, jaune, jaune d'or | Réaction en chaîne, Neutron, Criticité |
| Fallout | blanc vert, vert pomme, vert | Porte de l'abri, Terminal, Compteur Geiger |
| Ghost in the Shell | blanc d'eau, turquoise, vert d'eau | Plongée, Ghost, Synchronisation |

Iron Man et les cinq derniers fonds (`CoreArt` et ses sous-classes :
`ArcCore`, `QuantumCore`, `NeuralCore`, `AtomCore`, `VaultCore`, `GhostCore`) redessinent tout le
téléphone (`wholePhone`) : le fond, le milieu et l'avant, chacun avec son
animation (batterie, charge, allumage), ses contours d'allumage et ses
propres trajets d'impulsions.

- Physique quantique : au fond la forêt de câbles, au milieu le plateau, les
  colonnes et le support de la puce, devant les câbles, où courent les
  impulsions du réseau.
- Intelligence artificielle : au milieu l'accélérateur et son cerveau, devant
  la carte mère et la carte du bas, détaillées ; le réseau court sur le
  faisceau du bord.
- Énergie atomique : devant la cuve et ses mécanismes, au milieu les
  équipements internes et le cœur ; le réseau suit le circuit d'eau.
- Ghost in the Shell : au fond les rayons du tunnel et les cascades de
  signes, au milieu les couches du réseau et leurs nœuds, devant le
  réticule ; le réseau plonge des coins vers le point de fuite.
- Fallout : devant le boîtier du Pip-Boy, au fond la lueur du phosphore ;
  l'écran fait défiler les cinq onglets du jeu (`VaultBoy` porte le masque du
  Vault Boy) ; le réseau suit les jointures du boîtier.

Tous prennent les couleurs du téléphone, ou celles de leur thème. Comme le
convecteur et le réacteur, ils tournent à pleine cadence 30 s après
l'allumage ou un geste, puis ralentissent. Les noms de films et de jeux nomment les thèmes, et les dessins et sons sont
originaux, sauf pour Fallout : son écran reprend les textes du Pip-Boy et le
Vault Boy, tiré d'une capture. Halo est à usage personnel et n'est pas
distribué.

Appliquer un thème :

1. **Le fond** : Halo retient le thème, puis Android affiche son écran
   d'application du fond animé, même s'il est déjà en place : l'interface
   système ne reprend les couleurs d'un fond animé qu'à son application ;
   sinon, elle attend la prochaine mise en veille de l'écran.
2. **Les couleurs** : le fond se dessine dans la palette du thème et
   l'annonce à Android (`onComputeColors`). Si Couleurs est réglé sur « Fond
   d'écran », le système en tire son schéma Material You ; les widgets, qui
   prennent les couleurs du système (`values-v31/colors.xml`), suivent. Sur
   une couleur de base, le système ignore le fond : l'écran du thème le
   signale.
3. **Les sons** : copiés dans les sons du téléphone (Sonneries/Halo,
   Notifications/Halo, Alarmes/Halo), puis réglés par défaut. Il faut
   Android 10 et l'autorisation « Modifier les paramètres système », que Halo
   demande au retour de l'écran du fond. Ce sont des sons originaux,
   synthétisés par `tool/theme_sounds.py` (les mêmes recettes que la
   maquette), en WAV 16 bits.

Sans thème choisi pour le fond en place, le fond reprend les couleurs du
téléphone. Le code : `theme/HaloThemes.kt`, `theme/ThemeSounds.kt`,
`lib/src/screens/themes_screen.dart`.

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
