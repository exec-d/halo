import 'package:home_widget/home_widget.dart';

import 'wux_home_widget.dart';

/// Frontière entre l'application et les widgets natifs.
///
/// Les écrans ne parlent qu'à cette interface, ce qui permet de les tester
/// sans canal de plateforme.
abstract interface class HomeWidgetBridge {
  Future<String?> read(WuxHomeWidget widget, String name);

  /// Enregistre [value] puis demande au widget de se redessiner.
  Future<void> write(WuxHomeWidget widget, String name, String value);

  Future<bool> canPin();

  /// Propose à l'utilisateur d'ajouter [widget] à son écran d'accueil.
  Future<void> pin(WuxHomeWidget widget);
}

/// Implémentation réelle, adossée au plugin `home_widget`.
class PluginHomeWidgetBridge implements HomeWidgetBridge {
  @override
  Future<String?> read(WuxHomeWidget widget, String name) =>
      HomeWidget.getWidgetData<String>(widget.key(name));

  @override
  Future<void> write(WuxHomeWidget widget, String name, String value) async {
    await HomeWidget.saveWidgetData<String>(widget.key(name), value);
    await HomeWidget.updateWidget(qualifiedAndroidName: widget.androidProvider);
  }

  @override
  Future<bool> canPin() async =>
      await HomeWidget.isRequestPinWidgetSupported() ?? false;

  @override
  Future<void> pin(WuxHomeWidget widget) =>
      HomeWidget.requestPinWidget(qualifiedAndroidName: widget.androidProvider);
}
