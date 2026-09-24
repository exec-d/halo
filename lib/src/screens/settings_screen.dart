import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

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
  String? _refreshMessage;

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
          ? 'Prévisions à jour.'
          : 'Échec du téléchargement : vérifiez la connexion.';
    });
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    final status = _status;
    return Scaffold(
      body: ScreenFrame(
        title: 'Réglages',
        canGoBack: true,
        child: status == null
            ? const SizedBox.shrink()
            : Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  IuxSection(
                    title: 'Autorisations',
                    description:
                        'Halo ne demande que ce dont ses widgets ont besoin.',
                    children: [
                      _Permission(
                        title: 'Agenda',
                        use: 'Agendas et widget Mois, en lecture seule.',
                        granted: status.calendar,
                        onActivate: _calendar,
                      ),
                      const IuxGap.standard(),
                      _Permission(
                        title: 'Position approximative',
                        use:
                            'Seulement pour « Utiliser ma position » dans '
                            'Météo. Une ville choisie à la main suffit.',
                        granted: status.location,
                        onActivate: _location,
                      ),
                    ],
                  ),
                  const IuxGap.between(),
                  IuxSection(
                    title: 'Batterie',
                    description: status.batteryUnrestricted
                        ? "Halo n'est pas limité par l'optimisation de la "
                              'batterie : ses widgets se mettent à jour à '
                              "l'heure."
                        : 'Android peut retarder les mises à jour des '
                              'widgets pour économiser la batterie. Les '
                              'exclure de cette optimisation les garde à '
                              "l'heure, pour un coût minime.",
                    children: [
                      IuxButton(
                        label: _batteryLabel,
                        action: const IuxActionDescriptor(
                          semantics: IuxActionSemantics(label: _batteryLabel),
                        ),
                        expand: true,
                        onActivate: _platform.openBatterySettings,
                      ),
                    ],
                  ),
                  const IuxGap.between(),
                  IuxSection(
                    title: 'Données météo',
                    description: _weatherDescription(status),
                    children: [
                      IuxButton(
                        label: _refreshLabel,
                        action: IuxActionDescriptor(
                          semantics: IuxActionSemantics(
                            label: _refreshLabel,
                            unavailabilityReason: status.weatherPlace == null
                                ? 'Aucun lieu choisi dans Météo'
                                : _refreshing
                                ? 'Téléchargement en cours'
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
                      if (_refreshMessage case final message?) ...[
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
                        title: 'À propos de Halo',
                        subtitle: 'Version, confidentialité, crédits, licences',
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

  static const _batteryLabel = "Gérer l'optimisation de la batterie";
  static const _refreshLabel = 'Actualiser maintenant';

  String _weatherDescription(AppStatus status) {
    final place = status.weatherPlace;
    if (place == null) {
      return 'Aucun lieu choisi : ouvrez le widget Météo pour en choisir un.';
    }
    final updated = status.weatherUpdatedAt;
    if (updated == null) return 'Lieu : $place. Pas encore de prévisions.';
    final time = TimeOfDay.fromDateTime(updated).format(context);
    final today = DateUtils.isSameDay(updated, DateTime.now());
    return 'Lieu : $place. Mises à jour ${today ? "à $time" : "le "
                  "${updated.day}/${updated.month} à $time"}, puis toutes les '
        '30 minutes environ.';
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
    final label = granted ? 'Gérer' : 'Autoriser';
    return IuxCard(
      actions: [
        IuxButton(
          label: label,
          action: IuxActionDescriptor(
            semantics: IuxActionSemantics(label: '$label : $title'),
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
                ? const IuxStatus.success('Accordée')
                : const IuxStatus.neutral('Non accordée'),
          ),
          const SizedBox(height: 4),
          Text(use, style: IuxTypographyTheme.of(context).body),
        ],
      ),
    );
  }
}
