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
  testWidgets("le catalogue montre chaque widget avec un aperçu d'exemple", (
    tester,
  ) async {
    final platform = FakePlatform();
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();

    for (final widget in wuxHomeWidgets) {
      expect(find.text(widget.title), findsOneWidget);
      expect(platform.sampleRenders, contains(widget.id));
    }
    expect(platform.renders, isEmpty);
    // Les aperçus se redessinent seuls : on arrête leur minuterie.
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("le fond d'écran Circuit s'applique depuis son écran", (
    tester,
  ) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Circuit');
    expect(find.text('Pas encore appliqué'), findsOneWidget);

    await tester.ensureVisible(find.text("Appliquer le fond d'écran"));
    await tester.tap(find.text("Appliquer le fond d'écran"));
    await tester.pumpAndSettle();

    expect(platform.wallpaperApplications, 1);
    expect(find.text("Fond d'écran actuel"), findsOneWidget);

    await tester.ensureVisible(find.text('Vif'));
    await tester.tap(find.text('Vif'));
    await tester.pumpAndSettle();
    expect(platform.intensity, 'vivid');
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('données mobiles : accès aux données et forfait', (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Données mobiles');
    expect(find.text('Non accordé'), findsOneWidget);

    await tester.ensureVisible(
      find.text("Ouvrir l'accès aux données d'utilisation"),
    );
    await tester.tap(find.text("Ouvrir l'accès aux données d'utilisation"));
    await tester.pumpAndSettle();
    expect(platform.usageSettingsOpened, 1);

    // Accordé dans les réglages, puis retour dans Halo.
    platform.usageAccess = true;
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.paused);
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
    await tester.pumpAndSettle();
    expect(find.text('Accordé'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('écouteurs et montre : autorisation Bluetooth', (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Écouteurs et montre');
    await tester.ensureVisible(
      find.text('Autoriser « Appareils à proximité »'),
    );
    await tester.tap(find.text('Autoriser « Appareils à proximité »'));
    await tester.pumpAndSettle();
    expect(platform.bluetooth, isTrue);
    expect(find.text('Accordé'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("le raccourci « Fond d'écran » ouvre l'écran Circuit", (
    tester,
  ) async {
    final platform = FakePlatform()..target = 'wallpaper';
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();
    expect(find.text("Appliquer le fond d'écran"), findsOneWidget);

    // Touché alors que Halo est ouvert : les réglages.
    platform.openHandler!('settings');
    await tester.pumpAndSettle();
    expect(find.text('Réglages'), findsWidgets);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets("au premier lancement, l'accès à l'agenda est demandé", (
    tester,
  ) async {
    final platform = FakePlatform(permission: false)..firstLaunch = true;
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();
    expect(platform.permissionRequests, 1);

    // Pas une seconde fois.
    await tester.pumpWidget(const SizedBox());
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();
    expect(platform.permissionRequests, 1);
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

  testWidgets('masquer les événements « toute la journée » est enregistré', (
    tester,
  ) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Agenda');

    await tester.ensureVisible(find.text('Toute la journée'));
    await tester.tap(find.text('Toute la journée'));
    await tester.pumpAndSettle();
    expect(platform.data['agenda_one_column.allDay'], '0');

    // Les réafficher revient au réglage par défaut.
    await tester.tap(find.text('Toute la journée'));
    await tester.pumpAndSettle();
    expect(platform.data.containsKey('agenda_one_column.allDay'), isFalse);
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

  testWidgets('la météo se règle par une recherche de ville', (tester) async {
    final platform = FakePlatform();
    await _open(tester, platform, 'Météo');

    await tester.enterText(find.byType(EditableText), 'Villars');
    await tester.ensureVisible(find.text('Rechercher'));
    await tester.tap(find.text('Rechercher'));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Villars, Ain, France'));
    await tester.tap(find.text('Villars, Ain, France'));
    await tester.pumpAndSettle();

    expect(platform.place, 'Villars, Ain, France');
    expect(find.text('Prévisions pour Villars, Ain, France.'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('à propos montre la version et les crédits', (tester) async {
    await tester.pumpWidget(WuxApp(platform: FakePlatform()));
    await tester.pumpAndSettle();
    await tester.tap(find.bySemanticsLabel('À propos'));
    await tester.pumpAndSettle();

    expect(find.text('Version 1.0.0 (build 42)'), findsOneWidget);
    expect(find.text('Confidentialité'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });

  testWidgets('les réglages actualisent la météo', (tester) async {
    final platform = FakePlatform()..place = 'Lyon';
    await tester.pumpWidget(WuxApp(platform: platform));
    await tester.pumpAndSettle();
    await tester.tap(find.bySemanticsLabel('Réglages'));
    await tester.pumpAndSettle();

    expect(find.text('Agenda'), findsOneWidget);
    await tester.ensureVisible(find.text('Actualiser maintenant'));
    await tester.tap(find.text('Actualiser maintenant'));
    await tester.pumpAndSettle();
    expect(platform.weatherRefreshes, 1);
    expect(find.text('Prévisions à jour.'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });
}
