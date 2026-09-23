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

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[
  clockWidget,
  oneColumnAgendaWidget,
  twoColumnAgendaWidget,
  systemWidget,
  advancedSystemWidget,
];
