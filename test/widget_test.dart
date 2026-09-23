import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wux/src/app.dart';
import 'package:wux/src/home_widgets/catalog.dart';

import 'fake_platform.dart';

Future<void> _open(
  WidgetTester tester,
  FakePlatform platform,
  String title,
) async {
  await tester.pumpWidget(WuxApp(platform: platform));
  await tester.pumpAndSettle();
  await tester.tap(find.text(title));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('le catalogue liste les trois widgets', (tester) async {
    await tester.pumpWidget(WuxApp(platform: FakePlatform()));
    await tester.pumpAndSettle();

    expect(find.text('Horloge'), findsOneWidget);
    expect(find.text('Agenda'), findsOneWidget);
    expect(find.text('Agenda 2 colonnes'), findsOneWidget);
  });

  testWidgets("l'horloge montre l'heure et la date natives", (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Horloge');

    // Grande et compacte.
    expect(find.text('09:41'), findsNWidgets(2));
    expect(find.text('MERCREDI\n23 SEPTEMBRE'), findsOneWidget);
    expect(find.text('MERCREDI 23 SEPTEMBRE'), findsOneWidget);

    await tester.tap(find.text("Ajouter à l'écran d'accueil"));
    expect(platform.pinned, ['clock']);
    expect(find.byIcon(Icons.alarm), findsNothing);
  });

  testWidgets("l'horloge montre la prochaine alarme", (tester) async {
    await _open(tester, FakePlatform(nextAlarm: 'jeu. 07:00'), 'Horloge');

    expect(find.text('JEU. 07:00'), findsNWidgets(2));
    expect(find.byIcon(Icons.alarm), findsNWidgets(2));
  });

  testWidgets("par défaut, l'agenda affiche aujourd'hui et demain", (
    tester,
  ) async {
    await _open(tester, FakePlatform(), 'Agenda 2 colonnes');

    expect(find.text("AUJOURD'HUI"), findsOneWidget);
    expect(find.text('DEMAIN'), findsOneWidget);
    expect(find.text('Dîner'), findsOneWidget);
    expect(find.text('18:30 – 22:30 · Turin'), findsOneWidget);
    expect(find.text('Aucun événement'), findsOneWidget);
  });

  testWidgets("choisir « Aujourd'hui » n'affiche plus demain", (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Agenda 2 colonnes');

    await tester.ensureVisible(find.text("Aujourd'hui"));
    await tester.tap(find.text("Aujourd'hui"));
    await tester.pumpAndSettle();

    expect(platform.data['agenda_two_columns.days'], '1');
    expect(find.text('DEMAIN'), findsNothing);
  });

  testWidgets('masquer un agenda enregistre la sélection', (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Agenda');

    await tester.ensureVisible(find.text('Personnel'));
    await tester.tap(find.text('Personnel'));
    await tester.pumpAndSettle();

    expect(platform.data['agenda_one_column.calendars'], '2');
    expect(platform.lastPreviewIds, {2});
    expect(find.text('Dîner'), findsNothing);

    // Tout réactiver revient à « tous les agendas », y compris les futurs.
    await tester.tap(find.text('Personnel'));
    await tester.pumpAndSettle();
    expect(platform.data.containsKey('agenda_one_column.calendars'), isFalse);
  });

  testWidgets("sans autorisation, l'écran la demande", (tester) async {
    final platform = FakePlatform(permission: false);
    await _open(tester, platform, 'Agenda');

    expect(find.text('Personnel'), findsNothing);
    await tester.tap(find.text("Autoriser l'accès à l'agenda"));
    await tester.pumpAndSettle();

    expect(platform.permissionRequests, 1);
    expect(find.text('Personnel'), findsOneWidget);
    expect(find.text("Autoriser l'accès à l'agenda"), findsNothing);
  });

  testWidgets('un refus propose les réglages', (tester) async {
    final platform = FakePlatform(permission: false, grantOnRequest: false);
    await _open(tester, platform, 'Agenda');

    await tester.tap(find.text("Autoriser l'accès à l'agenda"));
    await tester.pumpAndSettle();

    expect(find.text('Ouvrir les réglages'), findsOneWidget);
  });

  testWidgets("ouvert depuis un widget, l'écran rend la main au lanceur", (
    tester,
  ) async {
    final platform = FakePlatform();
    await tester.pumpWidget(
      WuxApp(platform: platform, configuring: oneColumnAgendaWidget),
    );
    await tester.pumpAndSettle();

    // Directement sur les réglages, sans bouton d'épinglage.
    expect(find.text('Agendas'), findsOneWidget);
    expect(find.text("Ajouter à l'écran d'accueil"), findsNothing);

    await tester.tap(find.bySemanticsLabel('Retour'));
    await tester.pumpAndSettle();
    expect(platform.finishedConfiguring, 1);
  });
}
