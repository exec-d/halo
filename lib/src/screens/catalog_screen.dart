import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'agenda_screen.dart';
import 'clock_screen.dart';
import 'screen_frame.dart';

/// Liste des widgets disponibles ; chacun ouvre son écran de réglage.
class CatalogScreen extends StatelessWidget {
  const CatalogScreen({
    super.key,
    required this.widgets,
    required this.platform,
  });

  final List<WuxHomeWidget> widgets;
  final WuxPlatform platform;

  void _open(BuildContext context, WuxHomeWidget homeWidget) {
    Navigator.of(context).push<void>(
      MaterialPageRoute<void>(
        builder: (context) => switch (homeWidget.kind) {
          WuxWidgetKind.clock => ClockScreen(
            homeWidget: homeWidget,
            platform: platform,
          ),
          WuxWidgetKind.agenda => AgendaScreen(
            homeWidget: homeWidget,
            platform: platform,
          ),
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ScreenFrame(
        title: 'Mes widgets',
        child: IuxListGroup(
          children: [
            for (final homeWidget in widgets)
              IuxListItem.tappable(
                title: homeWidget.title,
                subtitle: homeWidget.description,
                disclosure: IuxListItemDisclosure.opensScreen,
                onActivate: () => _open(context, homeWidget),
              ),
          ],
        ),
      ),
    );
  }
}
