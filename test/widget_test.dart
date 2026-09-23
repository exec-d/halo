import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wux/src/app.dart';
import 'package:wux/src/home_widgets/home_widget_bridge.dart';
import 'package:wux/src/home_widgets/wux_home_widget.dart';

class _FakeBridge implements HomeWidgetBridge {
  _FakeBridge({this.pinSupported = true});

  final bool pinSupported;
  final data = <String, String>{};
  final pinned = <String>[];

  @override
  Future<String?> read(WuxHomeWidget widget, String name) async =>
      data[widget.key(name)];

  @override
  Future<void> write(WuxHomeWidget widget, String name, String value) async =>
      data[widget.key(name)] = value;

  @override
  Future<bool> canPin() async => pinSupported;

  @override
  Future<void> pin(WuxHomeWidget widget) async => pinned.add(widget.id);
}

void main() {
  testWidgets('le message saisi est transmis au widget', (tester) async {
    final bridge = _FakeBridge();
    await tester.pumpWidget(WuxApp(bridge: bridge));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), 'Bonjour');
    await tester.tap(find.text('Appliquer'));
    await tester.pumpAndSettle();

    expect(bridge.data['hello.message'], 'Bonjour');
    expect(find.text('Widget mis à jour'), findsOneWidget);
  });

  testWidgets("le bouton d'épinglage suit le support du lanceur", (
    tester,
  ) async {
    final bridge = _FakeBridge(pinSupported: false);
    await tester.pumpWidget(WuxApp(bridge: bridge));
    await tester.pumpAndSettle();

    expect(find.text("Ajouter à l'accueil"), findsNothing);
  });

  testWidgets("l'épinglage vise le bon widget", (tester) async {
    final bridge = _FakeBridge();
    await tester.pumpWidget(WuxApp(bridge: bridge));
    await tester.pumpAndSettle();

    await tester.tap(find.text("Ajouter à l'accueil"));
    expect(bridge.pinned, ['hello']);
  });
}
