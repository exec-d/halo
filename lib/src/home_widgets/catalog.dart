import 'dart:ui';

import 'wux_home_widget.dart';

const _androidPackage = 'dev.levilainpetit.wux.widgets';

const clockWidget = WuxHomeWidget(
  id: 'clock',
  kind: WuxWidgetKind.simple,
  title: 'Horloge',
  description: "L'heure et la date. Touchez-le pour ouvrir l'horloge.",
  androidProvider: '$_androidPackage.ClockWidgetProvider',
  previewSize: Size(340, 120),
);

const oneColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_one_column',
  kind: WuxWidgetKind.agenda,
  title: 'Agenda',
  description: 'Les événements du jour, en une colonne.',
  androidProvider: '$_androidPackage.OneColumnAgendaWidget',
  previewSize: Size(200, 280),
);

const twoColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_two_columns',
  kind: WuxWidgetKind.agenda,
  title: 'Agenda 2 colonnes',
  description: 'Les événements du jour, en deux colonnes.',
  androidProvider: '$_androidPackage.TwoColumnAgendaWidget',
  previewSize: Size(340, 280),
);

const systemWidget = WuxHomeWidget(
  id: 'system',
  kind: WuxWidgetKind.simple,
  title: 'Système',
  description: 'Batterie, réseau et stockage, sur une rangée.',
  androidProvider: '$_androidPackage.SystemWidget',
  previewSize: Size(340, 72),
);

const advancedSystemWidget = WuxHomeWidget(
  id: 'system_advanced',
  kind: WuxWidgetKind.simple,
  title: 'Système avancé',
  description:
      'Signal, Wi-Fi, Bluetooth, batterie, mémoire, stockage, localisation '
      'et son, sur deux rangées.',
  androidProvider: '$_androidPackage.AdvancedSystemWidget',
  previewSize: Size(340, 150),
);

const batteryWidget = WuxHomeWidget(
  id: 'battery',
  kind: WuxWidgetKind.simple,
  title: 'Batterie détaillée',
  description:
      'La courbe des 24 dernières heures et la suite prévue, la '
      'température, la tension et les cycles.',
  androidProvider: '$_androidPackage.BatteryWidget',
  previewSize: Size(340, 150),
);

const deviceWidget = WuxHomeWidget(
  id: 'device',
  kind: WuxWidgetKind.simple,
  title: 'Appareil',
  description:
      'La fiche du téléphone façon console, et depuis combien de temps il '
      'tourne, à la seconde.',
  androidProvider: '$_androidPackage.DeviceWidget',
  previewSize: Size(340, 150),
);

const bluetoothDevicesWidget = WuxHomeWidget(
  id: 'bt_devices',
  kind: WuxWidgetKind.bluetooth,
  title: 'Écouteurs et montre',
  description: 'La batterie des appareils Bluetooth connectés.',
  androidProvider: '$_androidPackage.BluetoothDevicesWidget',
  previewSize: Size(340, 72),
);

const screenTimeWidget = WuxHomeWidget(
  id: 'screen_time',
  kind: WuxWidgetKind.usage,
  title: "Temps d'écran",
  description:
      "La journée en cadran de 24 heures : quand l'écran était allumé, les "
      'déverrouillages et les applis les plus utilisées.',
  androidProvider: '$_androidPackage.ScreenTimeWidget',
  previewSize: Size(340, 150),
);

const mobileDataWidget = WuxHomeWidget(
  id: 'mobile_data',
  kind: WuxWidgetKind.mobileData,
  title: 'Données mobiles',
  description:
      'La consommation de la période face au forfait, avec la projection '
      'en fin de période.',
  androidProvider: '$_androidPackage.MobileDataWidget',
  previewSize: Size(340, 150),
);

const worldClockWidget = WuxHomeWidget(
  id: 'world_clock',
  kind: WuxWidgetKind.worldClock,
  title: 'Fuseaux horaires',
  description: "L'heure de trois villes, sur une rangée.",
  androidProvider: '$_androidPackage.WorldClockWidget',
  previewSize: Size(340, 80),
);

const countdownWidget = WuxHomeWidget(
  id: 'countdown',
  kind: WuxWidgetKind.countdown,
  title: 'Compte à rebours',
  description: "Les jours jusqu'à une date qui compte.",
  androidProvider: '$_androidPackage.CountdownWidget',
  previewSize: Size(280, 72),
);

const controlsWidget = WuxHomeWidget(
  id: 'controls',
  kind: WuxWidgetKind.simple,
  title: 'Contrôles',
  description:
      'Lampe torche en un geste ; Wi-Fi, Bluetooth, son et appareil photo '
      'à portée de doigt.',
  androidProvider: '$_androidPackage.ControlsWidget',
  previewSize: Size(340, 72),
);

const monthWidget = WuxHomeWidget(
  id: 'month',
  kind: WuxWidgetKind.simple,
  title: 'Mois',
  description:
      'Le mois en cours ; un point sous chaque jour qui a un événement. '
      "Il lit l'agenda : autorisez-le depuis un widget Agenda.",
  androidProvider: '$_androidPackage.MonthWidget',
  previewSize: Size(340, 250),
);

const weatherWidget = WuxHomeWidget(
  id: 'weather',
  kind: WuxWidgetKind.weather,
  title: 'Météo',
  description:
      'Façon tableau de bord : température et jauge du jour, ressenti, vent, '
      'humidité, UV, pluie, et la courbe des 24 heures. Open-Meteo, chaque '
      'heure. Sur une rangée, une seule ligne.',
  androidProvider: '$_androidPackage.WeatherWidget',
  previewSize: Size(340, 230),
);

const sunMoonWidget = WuxHomeWidget(
  id: 'sun_moon',
  kind: WuxWidgetKind.simple,
  title: 'Soleil et Lune',
  description:
      'Lever et coucher du soleil au lieu choisi dans Météo, durée du jour '
      'et phase de la lune.',
  androidProvider: '$_androidPackage.SunMoonWidget',
  previewSize: Size(340, 72),
);

const rainWidget = WuxHomeWidget(
  id: 'rain',
  kind: WuxWidgetKind.simple,
  title: 'Pluie',
  description:
      'Si la pluie arrive et quand, puis les probabilités des 12 prochaines '
      'heures, au lieu choisi dans Météo.',
  androidProvider: '$_androidPackage.RainWidget',
  previewSize: Size(340, 150),
);

const allergyWidget = WuxHomeWidget(
  id: 'allergy',
  kind: WuxWidgetKind.simple,
  title: 'Allergies',
  description:
      'Graminées, bouleau, aulne, olivier, armoise, ambroisie et qualité de '
      "l'air, au lieu choisi dans Météo. Pollens : Europe seulement.",
  androidProvider: '$_androidPackage.AllergyWidget',
  previewSize: Size(340, 190),
);

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[
  clockWidget,
  oneColumnAgendaWidget,
  twoColumnAgendaWidget,
  systemWidget,
  advancedSystemWidget,
  batteryWidget,
  deviceWidget,
  bluetoothDevicesWidget,
  screenTimeWidget,
  mobileDataWidget,
  worldClockWidget,
  countdownWidget,
  controlsWidget,
  monthWidget,
  weatherWidget,
  sunMoonWidget,
  rainWidget,
  allergyWidget,
];
