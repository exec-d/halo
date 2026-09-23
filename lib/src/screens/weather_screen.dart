import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Météo : le lieu des prévisions, par la position ou par une recherche.
/// Le widget Soleil et Lune utilise le même lieu.
class WeatherScreen extends StatefulWidget {
  const WeatherScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  @override
  State<WeatherScreen> createState() => _WeatherScreenState();
}

class _WeatherScreenState extends State<WeatherScreen> {
  final _query = TextEditingController();
  String? _place;
  List<WeatherPlace> _results = const [];
  bool _busy = false;
  String? _message;
  int _revision = 0;

  static const _locate = 'Utiliser ma position';
  static const _search = 'Rechercher';
  static const _busyReason = 'Recherche en cours';

  @override
  void initState() {
    super.initState();
    widget.platform.weatherPlace().then((place) {
      if (mounted) setState(() => _place = place);
    });
  }

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  Future<void> _run(Future<String?> Function() action, String failure) async {
    setState(() {
      _busy = true;
      _message = null;
    });
    String? place;
    try {
      place = await action();
    } on Exception {
      place = null;
    }
    if (!mounted) return;
    setState(() {
      _busy = false;
      if (place != null) {
        _place = place;
        _results = const [];
        _revision++;
      } else {
        _message = failure;
      }
    });
  }

  Future<void> _searchPlaces() async {
    final query = _query.text.trim();
    if (query.isEmpty) return;
    setState(() => _busy = true);
    List<WeatherPlace> results;
    try {
      results = await widget.platform.searchPlaces(query);
    } on Exception {
      results = const [];
    }
    if (!mounted) return;
    setState(() {
      _busy = false;
      _results = results;
      _message = results.isEmpty ? 'Aucun lieu trouvé pour « $query ».' : null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final message = _message;
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: widget.platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        IuxSection(
          title: 'Lieu',
          description: _place == null
              ? 'Aucun lieu choisi pour le moment.'
              : 'Prévisions pour $_place.',
          children: [
            IuxButton(
              label: _locate,
              action: IuxActionDescriptor.primary(
                semantics: IuxActionSemantics(
                  label: _locate,
                  unavailabilityReason: _busy ? _busyReason : null,
                ),
                role: IuxActionRole.custom,
                availability: _busy
                    ? IuxActionAvailability.disabled
                    : IuxActionAvailability.enabled,
              ),
              expand: true,
              onActivate: () => _run(
                widget.platform.locateWeatherPlace,
                "La position n'a pas pu être obtenue. Vérifiez que la "
                'localisation est activée et autorisée pour WUX.',
              ),
            ),
            const IuxGap.standard(),
            IuxTextField(
              input: const IuxInputDescriptor(
                semantics: IuxInputSemantics(label: 'Ville'),
                helpText: 'Ou cherchez une ville par son nom.',
              ),
              controller: _query,
              onChanged: (_) {},
              onSubmitted: (_) => _searchPlaces(),
            ),
            const IuxGap.standard(),
            IuxButton(
              label: _search,
              action: IuxActionDescriptor(
                semantics: IuxActionSemantics(
                  label: _search,
                  unavailabilityReason: _busy ? _busyReason : null,
                ),
                availability: _busy
                    ? IuxActionAvailability.disabled
                    : IuxActionAvailability.enabled,
              ),
              expand: true,
              onActivate: _searchPlaces,
            ),
            if (message != null) ...[
              const IuxGap.standard(),
              Text(message, style: IuxTypographyTheme.of(context).body),
            ],
            if (_results.isNotEmpty) ...[
              const IuxGap.standard(),
              IuxListGroup(
                children: [
                  for (final place in _results)
                    IuxListItem.tappable(
                      title: place.name,
                      onActivate: () => _run(
                        () => widget.platform.setWeatherPlace(place),
                        'Les prévisions de ce lieu ne sont pas disponibles.',
                      ),
                    ),
                ],
              ),
            ],
          ],
        ),
      ],
    );
  }
}
