import 'wux_home_widget.dart';

const _androidPackage = 'dev.levilainpetit.wux.widgets';

const clockWidget = WuxHomeWidget(
  id: 'clock',
  kind: WuxWidgetKind.clock,
  title: 'Horloge',
  description: "L'heure et la date. Touchez-le pour ouvrir l'horloge.",
  androidProvider: '$_androidPackage.ClockWidgetProvider',
);

const oneColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_one_column',
  kind: WuxWidgetKind.agenda,
  title: 'Agenda',
  description: 'Les événements du jour, en une colonne.',
  androidProvider: '$_androidPackage.OneColumnAgendaWidget',
);

const twoColumnAgendaWidget = WuxHomeWidget(
  id: 'agenda_two_columns',
  kind: WuxWidgetKind.agenda,
  title: 'Agenda 2 colonnes',
  description: 'Les événements du jour, en deux colonnes.',
  androidProvider: '$_androidPackage.TwoColumnAgendaWidget',
);

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[
  clockWidget,
  oneColumnAgendaWidget,
  twoColumnAgendaWidget,
];
