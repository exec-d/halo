import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../platform/wux_platform.dart';
import 'about_screen.dart';
import 'screen_frame.dart';

/// Réglages de l'application : ce dont dépendent les widgets, et le moyen
/// de le corriger. Relu à chaque retour à l'écran (après un passage par les
/// réglages d'Android).
class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key, required this.platform});

  final WuxPlatform platform;

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen>
    with WidgetsBindingObserver {
  AppStatus? _status;
  bool _refreshing = false;
  String Function(AppLocalizations l10n)? _refreshMessage;

  WuxPlatform get _platform => widget.platform;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _load();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _load();
  }

  Future<void> _load() async {
    final status = await _platform.status();
    if (mounted) setState(() => _status = status);
  }

  Future<void> _calendar() async {
    final status = _status;
    if (status == null || status.calendar) {
      await _platform.openAppSettings();
      return;
    }
    if (!await _platform.requestCalendarPermission()) {
      await _platform.openAppSettings();
    }
    await _load();
  }

  Future<void> _location() async {
    final status = _status;
    if (status == null || status.location) {
      await _platform.openAppSettings();
      return;
    }
    if (!await _platform.requestLocationPermission()) {
      await _platform.openAppSettings();
    }
    await _load();
  }

  Future<void> _refresh() async {
    setState(() {
      _refreshing = true;
      _refreshMessage = null;
    });
    final ok = await _platform.refreshWeather();
    if (!mounted) return;
    setState(() {
      _refreshing = false;
      _refreshMessage = ok
          ? (l10n) => l10n.settingsWeatherUpToDate
          : (l10n) => l10n.settingsWeatherFailed;
    });
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    final status = _status;
    final l10n = AppLocalizations.of(context);
    final batteryLabel = l10n.settingsBatteryButton;
    final refreshLabel = l10n.settingsWeatherRefresh;
    return Scaffold(
      body: ScreenFrame(
        title: l10n.catalogSettings,
        canGoBack: true,
        child: status == null
            ? const SizedBox.shrink()
            : Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  IuxSection(
                    title: l10n.settingsPermissionsTitle,
                    description: l10n.settingsPermissionsDescription,
                    children: [
                      _Permission(
                        title: l10n.settingsCalendarTitle,
                        use: l10n.settingsCalendarUse,
                        granted: status.calendar,
                        onActivate: _calendar,
                      ),
                      const IuxGap.standard(),
                      _Permission(
                        title: l10n.settingsLocationTitle,
                        use: l10n.settingsLocationUse,
                        granted: status.location,
                        onActivate: _location,
                      ),
                    ],
                  ),
                  const IuxGap.between(),
                  IuxSection(
                    title: l10n.settingsBatteryTitle,
                    description: status.batteryUnrestricted
                        ? l10n.settingsBatteryUnrestricted
                        : l10n.settingsBatteryRestricted,
                    children: [
                      IuxButton(
                        label: batteryLabel,
                        action: IuxActionDescriptor(
                          semantics: IuxActionSemantics(label: batteryLabel),
                        ),
                        expand: true,
                        onActivate: _platform.openBatterySettings,
                      ),
                    ],
                  ),
                  const IuxGap.between(),
                  IuxSection(
                    title: l10n.settingsWeatherTitle,
                    description: _weatherDescription(l10n, status),
                    children: [
                      IuxButton(
                        label: refreshLabel,
                        action: IuxActionDescriptor(
                          semantics: IuxActionSemantics(
                            label: refreshLabel,
                            unavailabilityReason: status.weatherPlace == null
                                ? l10n.settingsWeatherNoPlaceReason
                                : _refreshing
                                ? l10n.settingsWeatherDownloading
                                : null,
                          ),
                          availability:
                              status.weatherPlace == null || _refreshing
                              ? IuxActionAvailability.disabled
                              : IuxActionAvailability.enabled,
                        ),
                        expand: true,
                        onActivate: _refresh,
                      ),
                      if (_refreshMessage?.call(l10n) case final message?) ...[
                        const IuxGap.standard(),
                        Text(
                          message,
                          style: IuxTypographyTheme.of(context).body,
                        ),
                      ],
                    ],
                  ),
                  const IuxGap.between(),
                  IuxListGroup(
                    children: [
                      IuxListItem.tappable(
                        title: l10n.settingsAboutTitle,
                        subtitle: l10n.settingsAboutSubtitle,
                        disclosure: IuxListItemDisclosure.opensScreen,
                        onActivate: () => Navigator.of(context).push<void>(
                          MaterialPageRoute<void>(
                            builder: (_) => AboutScreen(platform: _platform),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
      ),
    );
  }

  String _weatherDescription(AppLocalizations l10n, AppStatus status) {
    final place = status.weatherPlace;
    if (place == null) return l10n.settingsWeatherNoPlace;
    final updated = status.weatherUpdatedAt;
    if (updated == null) return l10n.settingsWeatherNoForecast(place);
    final time = TimeOfDay.fromDateTime(updated).format(context);
    return DateUtils.isSameDay(updated, DateTime.now())
        ? l10n.settingsWeatherUpdatedToday(place, time)
        : l10n.settingsWeatherUpdatedOn(
            place,
            updated.day,
            updated.month,
            time,
          );
  }
}

/// Une autorisation : ce qu'elle permet, où elle en est, et le bouton pour
/// l'accorder ou la gérer.
class _Permission extends StatelessWidget {
  const _Permission({
    required this.title,
    required this.use,
    required this.granted,
    required this.onActivate,
  });

  final String title;
  final String use;
  final bool granted;
  final VoidCallback onActivate;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final label = granted ? l10n.permissionManage : l10n.permissionAllow;
    return IuxCard(
      actions: [
        IuxButton(
          label: label,
          action: IuxActionDescriptor(
            semantics: IuxActionSemantics(
              label: l10n.permissionActionSemantics(label, title),
            ),
          ),
          onActivate: onActivate,
        ),
      ],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: IuxTypographyTheme.of(context).title),
          const SizedBox(height: 4),
          IuxStatusIndicator(
            status: granted
                ? IuxStatus.success(l10n.permissionGranted)
                : IuxStatus.neutral(l10n.permissionNotGranted),
          ),
          const SizedBox(height: 4),
          Text(use, style: IuxTypographyTheme.of(context).body),
        ],
      ),
    );
  }
}
