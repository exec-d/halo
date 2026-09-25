# Journal des versions

Le numéro de version est celui de `pubspec.yaml` (`version:`) ; le numéro de
build est fixé par la CI. Le projet suit le [versionnage
sémantique](https://semver.org/lang/fr/) ; tant qu'il est en 0.x, une version
mineure peut changer des réglages.

## À venir

### Nouveau

- **Cinq widgets** : Lecture en cours, Prévisions 5 jours, Horloge
  analogique, Éphéméride, Chronomètre et minuteur.
- **Cinq fonds d'écran animés** inspirés de la science-fiction : Grille,
  Mégapole, Code, Néon, Sentinelle.
- **Galerie** : le fond du moment en grand, les fonds animés en carrousel,
  les widgets filtrables par famille (heure et agenda, météo et ciel,
  système et appareils, médias et contrôles).
- **Écran de veille** Halo, pendant la charge.
- **Tuiles de réglages rapides** : Météo, Fond Halo, Batterie.
- **Raccourcis de l'icône** : Fond d'écran, Widgets, Réglages.
- **Anglais** : l'application, les widgets et la documentation.

## 0.2.0 — 2026-09-25

### Nouveau

- **Fond d'écran animé Circuit** : l'intérieur d'un Pixel 7 en schéma néon,
  pour l'accueil et l'écran de verrouillage. Parallaxe selon l'inclinaison,
  vrai niveau de batterie, impulsions au passage des données, antennes selon
  le signal, allumage composant par composant, intensité réglable.
- **Cinq widgets système** : Batterie détaillée (courbe des 24 h et suite
  prévue), Appareil (façon console, durée depuis le démarrage), Écouteurs et
  montre (batterie des appareils Bluetooth), Temps d'écran (cadran de 24 h),
  Données mobiles (face au forfait).
- **Écran de verrouillage** : tous les widgets peuvent y être posés.
- **Agenda** : filtre des événements « toute la journée » ; événement en
  cours mis en avant, les suivants estompés.

### Amélioré

- Tous les textes des widgets ont une ombre sombre qui les détache d'un fond
  d'écran chargé.
- Widgets système plus lisibles : valeurs et détails qui rétrécissent au lieu
  d'être coupés, séparateurs plus discrets, détails en casse normale.
- Marges hors des coins arrondis du lanceur : plus aucun texte rogné.
- Météo : partie haute plus grande, graphique qui occupe la hauteur restante.
- Aide pour autoriser les « paramètres restreints » d'Android (accès aux
  données d'utilisation d'une application installée hors du Play Store).

### Projet

- Licence MIT, documentation (`docs/`), guide de contribution, code de
  conduite, politique de sécurité et de confidentialité, modèles de
  signalement ; APK joint automatiquement à chaque version publiée.

## 0.1.0 — 2026-09-24

Première version.

- **13 widgets** : Horloge (avec la prochaine alarme), Agenda et Agenda 2
  colonnes, Système et Système avancé, Fuseaux horaires, Compte à rebours,
  Contrôles, Mois, Météo (tableau de bord), Soleil et Lune, Pluie, Allergies.
- Style néon aux couleurs du téléphone (Material You), aperçus dans la liste
  du lanceur et dans l'application, réglages par appui long.
- Application Halo : catalogue, réglages de chaque widget, autorisations,
  à propos, licences, écran de démarrage, icône.
- Météo, pluie, pollens et qualité de l'air d'Open-Meteo, rafraîchis même
  quand Android met l'application en veille.
- Toutes les builds signées avec la même clé : les mises à jour s'installent
  par-dessus.
