import 'wux_home_widget.dart';

const _androidPackage = 'dev.levilainpetit.wux.widgets';

/// Widget de démonstration : il affiche un message choisi dans l'application.
/// Il sert à valider la chaîne Flutter → stockage partagé → widget natif.
const helloWidget = WuxHomeWidget(
  id: 'hello',
  title: 'Message',
  description: "Affiche un court message sur l'écran d'accueil.",
  androidProvider: '$_androidPackage.HelloWidgetProvider',
);

/// Tous les widgets proposés par l'application.
const wuxHomeWidgets = <WuxHomeWidget>[helloWidget];
