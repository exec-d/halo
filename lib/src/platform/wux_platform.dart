import 'package:flutter/services.dart';
import 'package:home_widget/home_widget.dart';

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

  /// Enregistre un réglage, puis redessine le widget.
  Future<void> write(WuxHomeWidget widget, String name, String? value);

  Future<bool> canPin();

  /// Propose à l'utilisateur d'ajouter [widget] à son écran d'accueil.
  Future<void> pin(WuxHomeWidget widget);

  Future<bool> hasCalendarPermission();

  /// Affiche la demande système ; renvoie la réponse.
  Future<bool> requestCalendarPermission();

  /// Ouvre la fiche de l'application, quand la demande n'est plus proposée.
  Future<void> openAppSettings();

  Future<List<CalendarInfo>> calendars();

  /// Ce que le widget agenda afficherait avec ces réglages.
  Future<List<AgendaDayPreview>> agendaPreview({
    required int days,
    required Set<int>? calendarIds,
  });

  Future<ClockPreview> clockPreview();

  Future<WidgetPalette> palette();
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
    switch (widget.kind) {
      case WuxWidgetKind.agenda:
        // Les widgets Glance ont besoin d'un rafraîchissement explicite pour
        // relire leurs réglages quand leur session est encore ouverte.
        await _channel.invokeMethod<void>('refreshWidgets');
      case WuxWidgetKind.clock:
        await HomeWidget.updateWidget(
          qualifiedAndroidName: widget.androidProvider,
        );
    }
  }

  @override
  Future<bool> canPin() async =>
      await HomeWidget.isRequestPinWidgetSupported() ?? false;

  @override
  Future<void> pin(WuxHomeWidget widget) =>
      HomeWidget.requestPinWidget(qualifiedAndroidName: widget.androidProvider);

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
  Future<List<AgendaDayPreview>> agendaPreview({
    required int days,
    required Set<int>? calendarIds,
  }) async {
    final list =
        await _channel.invokeListMethod<Object?>('agendaPreview', {
          'days': days,
          'calendarIds': calendarIds?.toList(),
        }) ??
        [];
    return [
      for (final item in list)
        AgendaDayPreview.fromMap(item! as Map<Object?, Object?>),
    ];
  }

  @override
  Future<ClockPreview> clockPreview() async {
    final map = await _channel.invokeMapMethod<String, String>('clockPreview');
    return ClockPreview(time: map!['time']!, date: map['date']!);
  }

  @override
  Future<WidgetPalette> palette() async {
    final map = await _channel.invokeMapMethod<Object?, Object?>('palette');
    return map == null ? WidgetPalette.fallback : WidgetPalette.fromMap(map);
  }
}
