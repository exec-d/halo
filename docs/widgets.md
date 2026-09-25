# Les widgets

*[English](en/widgets.md)*

Tous suivent le même style : heure en Roboto extra gras, texte mono, ombre
sombre sous les textes, halo sur les traits et les icônes, couleurs du
téléphone. Tous se posent sur l'écran d'accueil et sur l'écran de verrouillage
(Android récent) ; un appui long ouvre leurs réglages dans Halo (Android 12+).

| Widget | Taille | Contenu | Toucher |
| --- | --- | --- | --- |
| Horloge | 4x2 | Heure, date, prochaine alarme ; compacte sur une rangée | Ouvre l'horloge |
| Agenda | 2x3 | Événements d'aujourd'hui, ou d'aujourd'hui et demain | Ouvre l'événement ou le jour |
| Agenda 2 colonnes | 4x3 | Idem, en deux colonnes | Idem |
| Système | 4x1 | Batterie, réseau, stockage | Ouvre le réglage correspondant |
| Système avancé | 4x2 | Signal, Wi-Fi, Bluetooth, batterie, mémoire, stockage, position, son | Idem |
| Batterie détaillée | 4x2 | Courbe des 24 h et suite prévue, estimation, température, tension, cycles, santé | Ouvre l'utilisation de la batterie |
| Appareil | 4x2 | Console : durée depuis le démarrage, Android et correctif, modèle, puce, mémoire | Ouvre « À propos du téléphone » |
| Écouteurs et montre | 4x1 | Appareils Bluetooth connectés et leur batterie | Ouvre les réglages Bluetooth |
| Temps d'écran | 4x2 | Cadran de 24 h, total, déverrouillages, trois applis | Ouvre Bien-être numérique |
| Données mobiles | 4x2 | Consommation de la période face au forfait | Ouvre la consommation des données |
| Fuseaux horaires | 4x1 | L'heure de trois villes au choix | Ouvre l'horloge |
| Compte à rebours | 3x1 | Les jours jusqu'à une date | Ouvre Halo |
| Contrôles | 4x1 | Lampe torche, Wi-Fi, Bluetooth, son, appareil photo | Bascule la lampe, ouvre le reste |
| Mois | 4x3 | Le mois en cours, jours avec événements marqués | Ouvre le jour dans l'agenda |
| Météo | 4x3 | Température et jauge du jour, relevés, barres des 24 h | Ouvre Halo |
| Soleil et Lune | 4x1 | Lever, coucher, durée du jour, phase de la lune | Ouvre Halo |
| Pluie | 4x2 | Pluie en cours ou à venir, probabilités sur 12 h | Ouvre Halo |
| Allergies | 4x2 | Pollens par famille sur trois jours, qualité de l'air | Ouvre Halo |
| Prévisions 5 jours | 4x2 | Icône, maximum, minimum, et une capsule qui place chaque jour sur l'échelle de la semaine | Ouvre Halo |
| Horloge analogique | 2x2 | Cadran néon, aiguilles lumineuses, date | Ouvre les alarmes |
| Éphéméride | 4x2 | Fête du jour, semaine et jour de l'année, l'année en douze mois, prochain jour férié | Ouvre l'agenda |
| Chronomètre et minuteur | 4x1 | Chronomètre, minuteurs de 1, 5, 10, 25 min qui sonnent à la fin | Boutons sur le widget |
| Lecture en cours | 4x1 | Pochette néon, titre, artiste, avancement, commandes | Ouvre l'application qui joue |

Les tailles sont celles de la pose ; tous se redimensionnent, et adaptent leur
contenu à la place.

## Agenda

- Agendas affichés au choix ; « aujourd'hui » ou « aujourd'hui et demain ».
- Événements « toute la journée » affichés ou masqués (réglage par widget) ;
  un événement sur plusieurs jours compte pour « toute la journée » les jours
  qu'il couvre entièrement.
- L'événement en cours est mis en avant (titre en gras, détail plus clair),
  les suivants du même jour légèrement estompés.
- À partir de 18 h, quand il ne reste plus rien dans la journée, l'agenda
  passe au lendemain (`AgendaBuilder.END_OF_DAY_HOUR`).
- Sans défilement : les lignes qui ne tiennent pas sont signalées par « ••• ».

## Météo, pluie, allergies, soleil et lune

- Données [Open-Meteo](https://open-meteo.com) : prévisions heure par heure et
  au quart d'heure, qualité de l'air et pollens (Europe, modèle CAMS).
- Le lieu se choisit par la position approximative ou par une recherche.
- Pollens : le maximum de la journée, par famille (graminées, arbres,
  herbacées), aujourd'hui et les deux jours suivants.
- La phase de la lune est calculée sur le téléphone.

## Système détaillé

- **Batterie détaillée** : un relevé toutes les 15 minutes environ (et à
  chaque changement de niveau quand le fond Circuit est affiché), gardé
  26 heures. L'estimation vient d'Android pendant la charge, sinon du rythme
  des dernières heures. Les cycles demandent Android 14.
- **Appareil** : la durée depuis le démarrage est un chronomètre qui défile à
  la seconde.
- **Écouteurs et montre** : la batterie n'est connue que des appareils qui la
  communiquent ; les autres affichent « — ».
- **Temps d'écran** : calculé à partir des événements d'utilisation du jour ;
  il peut différer un peu de Bien-être numérique.
- **Données mobiles** : forfait (5 à 300 Go, ou aucun) et jour de reprise
  réglables ; la projection suit le rythme moyen de la période.

Ces deux derniers demandent l'accès aux données d'utilisation. Installé hors
du Play Store, Halo doit d'abord être autorisé aux « paramètres restreints » :
*Paramètres → Applis → Halo → ⋮ → Autoriser les paramètres restreints*.

## Temps, musique

- **Chronomètre et minuteur** : le temps défile dans le lanceur sans réveiller
  Halo. Toucher un nombre lance un minuteur ; la remise à zéro d'un minuteur
  déjà à zéro repasse en chronomètre. À la fin, une notification sonne
  (autorisation des notifications, demandée dans l'écran du widget).
- **Lecture en cours** : Android ne dit ce qui joue qu'aux applications qui
  ont l'accès aux notifications ; Halo n'en lit aucune. Installé hors du Play
  Store, il faut d'abord autoriser les paramètres restreints (voir plus haut).
- **Éphéméride** : fêtes du calendrier français des prénoms, jours fériés en
  France (Pâques calculée).
