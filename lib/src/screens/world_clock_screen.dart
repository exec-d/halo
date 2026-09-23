import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/cities.dart';
import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Fuseaux horaires : trois villes au choix.
class WorldClockScreen extends StatefulWidget {
  const WorldClockScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  @override
  State<WorldClockScreen> createState() => _WorldClockScreenState();
}

class _WorldClockScreenState extends State<WorldClockScreen> {
  static const _key = 'zones';
  static const _defaults = ['Europe/Paris', 'America/New_York', 'Asia/Tokyo'];

  List<String> _zones = List.of(_defaults);
  int _revision = 0;

  @override
  void initState() {
    super.initState();
    widget.platform.read(widget.homeWidget, _key).then((stored) {
      if (!mounted || stored == null) return;
      final zones = [
        for (final entry in stored.split(','))
          if (entry.split('|') case [_, final zone]
              when worldCities.any((c) => c.$2 == zone))
            zone,
      ];
      if (zones.length == 3) setState(() => _zones = zones);
    });
  }

  Future<void> _choose(int index, String zone) async {
    setState(() => _zones[index] = zone);
    final value = [
      for (final zone in _zones)
        '${worldCities.firstWhere((c) => c.$2 == zone).$1}|$zone',
    ].join(',');
    await widget.platform.write(widget.homeWidget, _key, value);
    setState(() => _revision++);
  }

  @override
  Widget build(BuildContext context) {
    final options = [
      for (final (name, zone) in worldCities)
        IuxRadioOption<String>(value: zone, label: name),
    ];
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: widget.platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        IuxSection(
          title: 'Villes',
          children: [
            for (var i = 0; i < 3; i++) ...[
              if (i > 0) const IuxGap.standard(),
              IuxSelectField<String>(
                label: 'Ville ${i + 1}',
                input: IuxInputDescriptor(
                  semantics: IuxInputSemantics(label: 'Ville ${i + 1}'),
                ),
                value: _zones[i],
                options: options,
                onChanged: (zone) => _choose(i, zone),
              ),
            ],
          ],
        ),
      ],
    );
  }
}
