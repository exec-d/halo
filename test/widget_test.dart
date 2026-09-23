import 'package:flutter/widgets.dart';
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
  await tester.ensureVisible(find.text(title).first);
  await tester.tap(find.text(title).first);
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('le catalogue montre chaque widget avec son aperçu', (
    tester,
  ) async {
    final platform = FakePlatform();
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();

    for (final widget in wuxHomeWidgets) {
      expect(find.text(widget.title), findsOneWidget);
      expect(platform.renders, contains(widget.id));
    }
    // Les aperçus se redessinent seuls : on arrête leur minuterie.
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("un widget sans réglage s'épingle", (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Horloge');

    await tester.tap(find.text("Ajouter à l'écran d'accueil"));
    expect(platform.pinned, ['clock']);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('masquer un agenda enregistre la sélection et redessine', (
    tester,
  ) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Agenda');
    final before = platform.renders.length;

    await tester.ensureVisible(find.text('Personnel'));
    await tester.tap(find.text('Personnel'));
    await tester.pumpAndSettle();

    expect(platform.data['agenda_one_column.calendars'], '2');
    expect(platform.renders.length, greaterThan(before));

    // Tout réactiver revient à « tous les agendas », y compris les futurs.
    await tester.tap(find.text('Personnel'));
    await tester.pumpAndSettle();
    expect(platform.data.containsKey('agenda_one_column.calendars'), isFalse);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("choisir « Aujourd'hui » enregistre un seul jour", (
    tester,
  ) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Agenda 2 colonnes');

    await tester.ensureVisible(find.text("Aujourd'hui"));
    await tester.tap(find.text("Aujourd'hui"));
    await tester.pumpAndSettle();

    expect(platform.data['agenda_two_columns.days'], '1');
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("sans autorisation, l'écran la demande", (tester) async {
    final platform = FakePlatform(permission: false);
    await _open(tester, platform, 'Agenda');

    expect(find.text('Personnel'), findsNothing);
    await tester.tap(find.text("Autoriser l'accès à l'agenda"));
    await tester.pumpAndSettle();

    expect(platform.permissionRequests, 1);
    expect(find.text('Personnel'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('un refus propose les réglages', (tester) async {
    final platform = FakePlatform(permission: false, grantOnRequest: false);
    await _open(tester, platform, 'Agenda');

    await tester.tap(find.text("Autoriser l'accès à l'agenda"));
    await tester.pumpAndSettle();

    expect(find.text('Ouvrir les réglages'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("ouvert depuis un widget, l'écran rend la main au lanceur", (
    tester,
  ) async {
    final platform = FakePlatform();
    await tester.pumpWidget(
      WuxApp(platform: platform, configuring: oneColumnAgendaWidget),
    );
    await tester.pumpAndSettle();

    expect(find.text('Agendas'), findsOneWidget);
    expect(find.text("Ajouter à l'écran d'accueil"), findsNothing);

    await tester.tap(find.bySemanticsLabel('Retour'));
    await tester.pumpAndSettle();
    expect(platform.finishedConfiguring, 1);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('le compte à rebours enregistre titre et date', (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Compte à rebours');

    final fields = find.byType(EditableText);
    await tester.enterText(fields.at(0), 'Vacances');
    await tester.enterText(fields.at(1), '4');
    await tester.enterText(fields.at(2), '11');
    await tester.enterText(fields.at(3), '2026');
    await tester.pumpAndSettle();

    expect(platform.data['countdown.title'], 'Vacances');
    expect(platform.data['countdown.date'], '2026-11-04');
    await tester.pumpWidget(const SizedBox());
  });
}
