import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

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

  Future<void> _run(
    Future<String?> Function() action,
    String Function(AppLocalizations l10n) failure,
  ) async {
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
        _message = failure(AppLocalizations.of(context));
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
      _message = results.isEmpty
          ? AppLocalizations.of(context).weatherNoResult(query)
          : null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final message = _message;
    final l10n = AppLocalizations.of(context);
    final locate = l10n.weatherUseLocation;
    final search = l10n.weatherSearch;
    final busyReason = _busy ? l10n.weatherSearching : null;
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: widget.platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        IuxSection(
          title: l10n.weatherPlaceTitle,
          description: _place == null
              ? l10n.weatherNoPlace
              : l10n.weatherForecastFor(_place!),
          children: [
            IuxButton(
              label: locate,
              action: IuxActionDescriptor.primary(
                semantics: IuxActionSemantics(
                  label: locate,
                  unavailabilityReason: busyReason,
                ),
                role: IuxActionRole.custom,
                availability: _busy
                    ? IuxActionAvailability.disabled
                    : IuxActionAvailability.enabled,
              ),
              expand: true,
              onActivate: () => _run(
                widget.platform.locateWeatherPlace,
                (l10n) => l10n.weatherLocationFailed,
              ),
            ),
            const IuxGap.standard(),
            IuxTextField(
              input: IuxInputDescriptor(
                semantics: IuxInputSemantics(label: l10n.weatherCityLabel),
                helpText: l10n.weatherCityHelp,
              ),
              controller: _query,
              onChanged: (_) {},
              onSubmitted: (_) => _searchPlaces(),
            ),
            const IuxGap.standard(),
            IuxButton(
              label: search,
              action: IuxActionDescriptor(
                semantics: IuxActionSemantics(
                  label: search,
                  unavailabilityReason: busyReason,
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
                        (l10n) => l10n.weatherPlaceUnavailable,
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
