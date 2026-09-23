import 'dart:typed_data';
import 'dart:ui';

import 'package:wux/src/home_widgets/wux_home_widget.dart';
import 'package:wux/src/platform/wux_platform.dart';

/// Plateforme simulée : un stockage en mémoire, deux agendas, et un journal
/// des aperçus demandés.
class FakePlatform implements WuxPlatform {
  FakePlatform({this.permission = true, this.grantOnRequest = true});

  bool permission;
  final bool grantOnRequest;
  final data = <String, String>{};
  final pinned = <String>[];
  final renders = <String>[];
  var permissionRequests = 0;
  var finishedConfiguring = 0;

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
  Future<Uint8List?> render(WuxHomeWidget widget, Size size) async {
    renders.add(widget.id);
    return null;
  }

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
  Future<WuxHomeWidget?> configuringWidget() async => null;

  @override
  Future<void> finishConfiguring() async => finishedConfiguring++;
}
