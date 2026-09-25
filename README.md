# Halo

*[English](README.en.md)*

[![CI](https://github.com/exec-d/halo/actions/workflows/ci.yml/badge.svg)](https://github.com/exec-d/halo/actions/workflows/ci.yml)
[![Licence MIT](https://img.shields.io/badge/licence-MIT-blue.svg)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84)

**Des widgets néon et un fond d'écran animé pour personnaliser un téléphone
Android.** Les couleurs suivent celles du téléphone (Material You) ; rien ne
quitte l'appareil, hormis les coordonnées envoyées à Open-Meteo pour la météo.

*Nom de code : WUX. Le paquet Android (`dev.levilainpetit.wux`) et le code
gardent ce nom.*

## Ce que contient Halo

**23 widgets** pour l'écran d'accueil et l'écran de verrouillage :

| Famille | Widgets |
| --- | --- |
| Temps | Horloge (avec la prochaine alarme), Horloge analogique, Fuseaux horaires, Compte à rebours, Chronomètre et minuteur, Mois, Éphéméride |
| Agenda | Agenda, Agenda 2 colonnes — l'événement en cours mis en avant, « toute la journée » masquable |
| Météo | Météo (tableau de bord et barres des 24 h), Prévisions 5 jours, Pluie, Allergies (pollens), Soleil et Lune |
| Système | Système, Système avancé, Batterie détaillée, Appareil (façon console), Écouteurs et montre, Temps d'écran, Données mobiles |
| Musique | Lecture en cours (pochette néon, commandes) |
| Raccourcis | Contrôles (lampe torche, Wi-Fi, Bluetooth, son, appareil photo) |

**Six fonds d'écran animés** :

- **Circuit** : l'intérieur d'un Pixel 7 en schéma néon, qui réagit à
  l'inclinaison, suit la vraie batterie, s'illumine au passage des données et
  s'allume composant par composant avec l'écran ;
- cinq fonds inspirés de la science-fiction : **Grille** (motos de lumière
  sur une grille à perte de vue), **Mégapole** (ville immense sous la pluie),
  **Code** (pluie de caractères verts), **Néon** (rue du futur, enseignes qui
  grésillent et image qui bugue) et **Sentinelle** (l'œil rouge d'une IA, qui
  vous suit du regard).

**Ailleurs dans Android** : un écran de veille (horloge néon pendant la
charge), trois tuiles de réglages rapides (Météo, Fond Halo, Batterie) et des
raccourcis sur l'icône (Fonds d'écran, Widgets, Réglages).

**L'application** : galerie en trois onglets (galerie, fonds, mes ajouts), réglages de chaque widget (aussi
par un appui long sur le widget), autorisations, à propos et licences, en
français et en anglais.

Le détail de chaque widget : [docs/widgets.md](docs/widgets.md) ; les fonds
d'écran, l'écran de veille et les tuiles : [docs/wallpaper.md](docs/wallpaper.md).

## Installer

Halo n'est pas sur le Play Store. Chaque version publiée sur la page
[Releases](https://github.com/exec-d/halo/releases) porte un APK ; la dernière
build de la branche principale est aussi un artefact de
[la CI](https://github.com/exec-d/halo/actions/workflows/ci.yml).

1. Télécharger l'APK sur le téléphone et l'ouvrir (autoriser l'installation
   depuis le navigateur ou le gestionnaire de fichiers si Android le demande).
2. Les mises à jour s'installent par-dessus : toutes les builds sont signées
   avec la même clé.
3. Pour **Temps d'écran** et **Données mobiles** : Android réserve l'accès aux
   données d'utilisation aux applications du Play Store. Dans
   *Paramètres → Applis → Halo*, touchez **⋮ → Autoriser les paramètres
   restreints**, puis activez l'accès depuis Halo.

Configuration requise : Android 8.0 (API 26) ; Android 12 ou plus pour les
couleurs du téléphone, l'aperçu dans la liste des widgets et le réglage par
appui long.

## Autorisations

Toutes sont facultatives ; chacune ne sert qu'aux widgets indiqués.

| Autorisation | Pour | Demandée |
| --- | --- | --- |
| Agenda (lecture) | Agenda, Agenda 2 colonnes, Mois | Au premier lancement |
| Position approximative | Météo, Prévisions, Pluie, Allergies, Soleil et Lune — seulement si vous choisissez « Ma position » | Dans l'écran Météo |
| Appareils à proximité | Écouteurs et montre | Dans l'écran du widget |
| Données d'utilisation | Temps d'écran, Données mobiles | Dans les réglages d'Android |
| Accès aux notifications | Lecture en cours (pour voir ce qui joue ; aucune notification lue) | Dans les réglages d'Android |
| Notifications | Chronomètre et minuteur (sonnerie à la fin) | Dans l'écran du widget |
| Internet | Prévisions Open-Meteo | — |

Ce que Halo fait de ces données : [PRIVACY.md](PRIVACY.md).

## Compiler

```bash
flutter pub get
dart format .
flutter analyze
flutter test
flutter build apk --debug    # ou : flutter run, sur un appareil branché
```

Il faut Flutter (canal stable), Java 17 et le SDK Android. La bibliothèque
d'interface [IUX](https://github.com/systm-d/IUX) est tirée de GitHub,
épinglée sur un commit dans `pubspec.yaml`.

L'architecture — pourquoi deux moitiés, Flutter et Kotlin, et comment elles se
parlent — est décrite dans [docs/architecture.md](docs/architecture.md).

## Contribuer

Les signalements et propositions sont les bienvenus : voir
[CONTRIBUTING.md](CONTRIBUTING.md), et le
[code de conduite](CODE_OF_CONDUCT.md). Une faille de sécurité se signale en
privé : [SECURITY.md](SECURITY.md). L'historique des versions :
[CHANGELOG.md](CHANGELOG.md).

## Licence et crédits

Halo est distribué sous [licence MIT](LICENSE).

- Météo, pluie, pollens et qualité de l'air : [Open-Meteo](https://open-meteo.com)
  (CC BY 4.0), pollens et air issus du Copernicus Atmosphere Monitoring
  Service ; géocodage GeoNames (CC BY 4.0).
- Icônes : Material Icons de Google (Apache 2.0).
- Interface : [Flutter](https://flutter.dev) et [IUX](https://github.com/systm-d/IUX).
