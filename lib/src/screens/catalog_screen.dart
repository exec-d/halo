import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'screen_frame.dart';
import 'widget_screen.dart';

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
        builder: (context) =>
            WidgetScreen(homeWidget: homeWidget, platform: platform),
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
