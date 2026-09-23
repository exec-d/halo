import 'wux_home_widget.dart';

const _androidPackage = 'dev.levilainpetit.wux.widgets';

const clockWidget = WuxHomeWidget(
  id: 'clock',
  kind: WuxWidgetKind.clock,
  title: 'Horloge',
  description: "L'heure et la date. Touchez-le pour ouvrir l'horloge.",
  androidProvider: '$_androidPackage.ClockWidgetProvider',
);

const todayAgendaWidget = WuxHomeWidget(
  id: 'agenda_today',
  kind: WuxWidgetKind.agenda,
  title: 'Agenda du jour',
  description: "Les événements d'aujourd'hui.",
  androidProvider: '$_androidPackage.TodayAgendaWidgetReceiver',
  days: 1,
);

const twoDayAgendaWidget = WuxHomeWidget(
  id: 'agenda_two_days',
  kind: WuxWidgetKind.agenda,
  title: "Aujourd'hui et demain",
  description: "Les événements d'aujourd'hui et de demain.",
  androidProvider: '$_androidPackage.TwoDayAgendaWidgetReceiver',
  days: 2,
);

const clockAgendaWidget = WuxHomeWidget(
  id: 'clock_agenda',
  kind: WuxWidgetKind.agenda,
  title: 'Horloge et agenda',
  description: "L'heure, puis les événements d'aujourd'hui et de demain.",
  androidProvider: '$_androidPackage.ClockAgendaWidgetProvider',
  days: 2,
  withClock: true,
);

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[
  clockWidget,
  todayAgendaWidget,
  twoDayAgendaWidget,
  clockAgendaWidget,
];
