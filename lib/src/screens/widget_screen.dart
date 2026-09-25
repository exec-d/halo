import 'package:flutter/material.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'access_screen.dart';
import 'agenda_screen.dart';
import 'countdown_screen.dart';
import 'simple_screen.dart';
import 'weather_screen.dart';
import 'world_clock_screen.dart';

/// Écran de réglage d'un widget, ouvert depuis le catalogue ou depuis un
/// appui long sur l'écran d'accueil.
class WidgetScreen extends StatelessWidget {
  const WidgetScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.configuring = false,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;

  /// Ouvert par le lanceur : le widget est déjà posé, donc pas de bouton
  /// d'épinglage, et revenir en arrière rend la main au lanceur.
  final bool configuring;

  @override
  Widget build(BuildContext context) {
    final screen = switch (homeWidget.kind) {
      WuxWidgetKind.simple => SimpleScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
      WuxWidgetKind.agenda => AgendaScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
      WuxWidgetKind.worldClock => WorldClockScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
      WuxWidgetKind.countdown => CountdownScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
      WuxWidgetKind.weather => WeatherScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
      WuxWidgetKind.bluetooth ||
      WuxWidgetKind.usage ||
      WuxWidgetKind.mobileData => AccessScreen(
        homeWidget: homeWidget,
        platform: platform,
        allowPin: !configuring,
      ),
    };
    if (!configuring) return screen;
    // Les réglages sont déjà enregistrés à chaque changement : il ne reste
    // qu'à signaler au lanceur que la configuration est terminée, sans quoi
    // un lanceur antérieur à Android 12 retirerait le widget.
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop) platform.finishConfiguring();
      },
      child: screen,
    );
  }
}
