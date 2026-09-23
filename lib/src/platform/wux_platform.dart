import 'package:flutter/services.dart';
import 'package:home_widget/home_widget.dart';

import '../home_widgets/catalog.dart';
import '../home_widgets/wux_home_widget.dart';
import 'models.dart';

export 'models.dart';

/// Frontière entre l'application et Android.
///
/// Les écrans ne parlent qu'à cette interface, ce qui permet de les tester
/// sans canal de plateforme.
abstract interface class WuxPlatform {
  /// Réglage partagé avec le widget natif.
  Future<String?> read(WuxHomeWidget widget, String name);

  /// Enregistre un réglage, puis redessine les widgets concernés.
  Future<void> write(WuxHomeWidget widget, String name, String? value);

  Future<bool> canPin();

  /// Propose à l'utilisateur d'ajouter [widget] à son écran d'accueil.
  Future<void> pin(WuxHomeWidget widget);

  /// Le widget tel qu'Android le dessine, à la taille [size] (dp), en PNG.
  Future<Uint8List?> render(WuxHomeWidget widget, Size size);

  Future<bool> hasCalendarPermission();

  /// Affiche la demande système ; renvoie la réponse.
  Future<bool> requestCalendarPermission();

  /// Ouvre la fiche de l'application, quand la demande n'est plus proposée.
  Future<void> openAppSettings();

  Future<List<CalendarInfo>> calendars();

  /// Widget dont le lanceur a ouvert les réglages, ou `null`.
  Future<WuxHomeWidget?> configuringWidget();

  /// Rend la main au lanceur après les réglages.
  Future<void> finishConfiguring();
}

/// Implémentation réelle : `home_widget` pour le stockage et l'épinglage, le
/// canal de `MainActivity.kt` pour le reste.
class AndroidWuxPlatform implements WuxPlatform {
  static const _channel = MethodChannel('dev.levilainpetit.wux/native');

  @override
  Future<String?> read(WuxHomeWidget widget, String name) =>
      HomeWidget.getWidgetData<String>(widget.key(name));

  @override
  Future<void> write(WuxHomeWidget widget, String name, String? value) async {
    await HomeWidget.saveWidgetData<String>(widget.key(name), value);
    await _channel.invokeMethod<void>('refreshWidgets');
    await HomeWidget.updateWidget(qualifiedAndroidName: widget.androidProvider);
  }

  @override
  Future<bool> canPin() async =>
      await HomeWidget.isRequestPinWidgetSupported() ?? false;

  @override
  Future<void> pin(WuxHomeWidget widget) =>
      HomeWidget.requestPinWidget(qualifiedAndroidName: widget.androidProvider);

  @override
  Future<Uint8List?> render(WuxHomeWidget widget, Size size) =>
      _channel.invokeMethod<Uint8List>('renderWidget', {
        'id': widget.id,
        'width': size.width,
        'height': size.height,
      });

  @override
  Future<bool> hasCalendarPermission() async =>
      await _channel.invokeMethod<bool>('hasCalendarPermission') ?? false;

  @override
  Future<bool> requestCalendarPermission() async =>
      await _channel.invokeMethod<bool>('requestCalendarPermission') ?? false;

  @override
  Future<void> openAppSettings() =>
      _channel.invokeMethod<void>('openAppSettings');

  @override
  Future<List<CalendarInfo>> calendars() async {
    final list = await _channel.invokeListMethod<Object?>('calendars') ?? [];
    return [
      for (final item in list)
        CalendarInfo.fromMap(item! as Map<Object?, Object?>),
    ];
  }

  @override
  Future<WuxHomeWidget?> configuringWidget() async {
    final id = await HomeWidget.initiallyLaunchedFromHomeWidgetConfigure();
    if (id == null) return null;
    final provider = await _channel.invokeMethod<String>('widgetProvider', {
      'id': int.parse(id),
    });
    for (final widget in wuxHomeWidgets) {
      if (widget.androidProvider == provider) return widget;
    }
    return null;
  }

  @override
  Future<void> finishConfiguring() => HomeWidget.finishHomeWidgetConfigure();
}
