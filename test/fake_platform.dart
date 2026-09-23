import 'package:flutter/material.dart';
import 'package:wux/src/home_widgets/wux_home_widget.dart';
import 'package:wux/src/platform/wux_platform.dart';

/// Plateforme simulée : un stockage en mémoire et deux agendas.
class FakePlatform implements WuxPlatform {
  FakePlatform({this.permission = true, this.grantOnRequest = true});

  bool permission;
  final bool grantOnRequest;
  final data = <String, String>{};
  final pinned = <String>[];
  var permissionRequests = 0;
  Set<int>? lastPreviewIds;

  static const personal = CalendarInfo(
    id: 1,
    name: 'Personnel',
    account: 'moi@example.com',
    color: Color(0xFFE53935),
  );
  static const work = CalendarInfo(
    id: 2,
    name: 'Travail',
    account: 'moi@example.com',
    color: Color(0xFF1E88E5),
  );

  @override
  Future<String?> read(WuxHomeWidget widget, String name) async =>
      data[widget.key(name)];

  @override
  Future<void> write(WuxHomeWidget widget, String name, String? value) async {
    if (value == null) {
      data.remove(widget.key(name));
    } else {
      data[widget.key(name)] = value;
    }
  }

  @override
  Future<bool> canPin() async => true;

  @override
  Future<void> pin(WuxHomeWidget widget) async => pinned.add(widget.id);

  @override
  Future<bool> hasCalendarPermission() async => permission;

  @override
  Future<bool> requestCalendarPermission() async {
    permissionRequests++;
    permission = grantOnRequest;
    return permission;
  }

  @override
  Future<void> openAppSettings() async {}

  @override
  Future<List<CalendarInfo>> calendars() async => [personal, work];

  @override
  Future<List<AgendaDayPreview>> agendaPreview({
    required int days,
    required Set<int>? calendarIds,
  }) async {
    lastPreviewIds = calendarIds;
    return [
      AgendaDayPreview(
        label: "AUJOURD'HUI",
        events: [
          if (calendarIds?.contains(1) ?? true)
            const AgendaEventPreview(
              time: 'De 18:30 à 22:30',
              title: 'Dîner',
              location: 'Turin',
              color: Color(0xFFE53935),
            ),
        ],
      ),
      if (days > 1) const AgendaDayPreview(label: 'DEMAIN', events: []),
    ];
  }

  @override
  Future<ClockPreview> clockPreview() async =>
      const ClockPreview(time: '09:41', date: 'mercredi 23 septembre');

  @override
  Future<WidgetPalette> palette() async => WidgetPalette.fallback;
}
