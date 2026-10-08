# Journal des versions

Le numéro de version est celui de `pubspec.yaml` (`version:`) ; le numéro de
build est fixé par la CI. Le projet suit le [versionnage
sémantique](https://semver.org/lang/fr/) ; tant qu'il est en 0.x, une version
mineure peut changer des réglages.

## À venir

### Nouveau

- **Retour vers le futur** : la carte mère du convecteur devient bien plus
  dense (plan de cuivre, vias, composants, nouveaux bus).
- **Iron Man**, refait : le téléphone porte l'armure, avec le réacteur arc
  qui bat en son centre.
- **Cinq thèmes de plus**, chacun avec son fond animé, sa palette et ses
  trois sons : Physique quantique (tout le téléphone devient le lustre
  d'un ordinateur quantique), Intelligence artificielle (un cerveau de silicium à la place de la
  batterie, sur la carte détaillée de Circuit),
  Énergie atomique (tout le téléphone devient la cuve d'un réacteur), Fallout (tout le téléphone devient le
  Pip-Boy) et Ghost in the Shell (tout le téléphone devient le cyberespace du
  film).
- **Thèmes** : Circuit, Retour vers le futur et Iron Man. Un toucher applique
  le fond animé dans sa palette ; le fond annonce ces couleurs à Android, qui
  en tire celles du système et des widgets ; trois sons originaux,
  synthétisés (tool/theme_sounds.py), remplacent au choix la sonnerie, la
  notification et l'alarme.
- **Cinq widgets** : Lecture en cours, Prévisions 5 jours, Horloge
  analogique, Éphéméride, Chronomètre et minuteur.
- **Six widgets de plus** : Progression, Mer et vagues, Lune, Réseau,
  Contacts favoris, Pas et sommeil (Santé Connect). La Lune est dessinée
  dans sa phase du moment, avec ses mers et ses cratères ; le widget donne
  le compte à rebours jusqu'à la pleine lune, son lever et son coucher, et
  les quatre phases à venir. Soleil et Lune suit le même calcul, plus précis.
- **Retour vers le futur**, fond d'écran : l'intérieur du téléphone de
  Circuit, avec le convecteur temporel à la place de la batterie ; ses
  impulsions courent vers le cœur, plus vite en charge ; circuits temporels
  à l'heure réelle et Mr. Fusion sur les cartes.
- **Iron Man**, fond d'écran : le même téléphone avec le réacteur arc de Tony
  Stark ; ses bobines suivent la batterie, son cœur respire.
- **Galerie** : un nouvel accueil en trois onglets. Galerie (le fond du
  moment en grand, les fonds animés en carrousel, les widgets en mosaïque,
  filtrables par famille et par recherche), Fonds (les fonds en grand et
  l'écran de veille), Mes ajouts (le fond appliqué et les widgets posés).
- **Fiche d'un widget** : l'aperçu en grand, posé sur le fond Halo de son
  choix ou sur l'écran de verrouillage, ses étiquettes, ses réglages et le
  bouton « Ajouter à l'écran d'accueil » toujours en bas.
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
