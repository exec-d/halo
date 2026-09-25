import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Réglages d'un widget qui a besoin d'un accès : appareils Bluetooth, ou
/// données d'utilisation (temps d'écran, données mobiles). Pour les données
/// mobiles, aussi le forfait et son jour de reprise.
///
/// L'accès aux données d'utilisation ne s'accorde que dans les réglages
/// d'Android : l'état est relu au retour dans Halo.
class AccessScreen extends StatefulWidget {
  const AccessScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  @override
  State<AccessScreen> createState() => _AccessScreenState();
}

class _AccessScreenState extends State<AccessScreen>
    with WidgetsBindingObserver {
  static const _quotaKey = 'quota';
  static const _cycleKey = 'cycleDay';

  /// Forfaits proposés, en Go ; 0 : pas de forfait.
  static const _quotas = [0, 5, 10, 20, 30, 50, 80, 100, 150, 200, 300];

  bool? _granted;
  int _revision = 0;
  int _quota = 0;
  int _cycleDay = 1;

  WuxPlatform get _platform => widget.platform;
  bool get _mobileData => widget.homeWidget.kind == WuxWidgetKind.mobileData;
  late final _Access _access = _Access.of(widget.homeWidget.kind, _platform);

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
    if (state == AppLifecycleState.resumed) _refreshAccess();
  }

  Future<void> _load() async {
    if (_mobileData) {
      final quota = await _platform.read(widget.homeWidget, _quotaKey);
      final cycle = await _platform.read(widget.homeWidget, _cycleKey);
      _quota = int.tryParse(quota ?? '') ?? 0;
      _cycleDay = int.tryParse(cycle ?? '') ?? 1;
    }
    await _refreshAccess();
  }

  Future<void> _refreshAccess() async {
    final granted = await _access.has();
    if (!mounted) return;
    setState(() {
      if (granted != _granted) _revision++;
      _granted = granted;
    });
  }

  Future<void> _request() async {
    await _access.request();
    await _refreshAccess();
  }

  Future<void> _setQuota(int quota) async {
    setState(() => _quota = quota);
    await _platform.write(widget.homeWidget, _quotaKey, '$quota');
    if (mounted) setState(() => _revision++);
  }

  Future<void> _setCycleDay(int day) async {
    setState(() => _cycleDay = day);
    await _platform.write(widget.homeWidget, _cycleKey, '$day');
    if (mounted) setState(() => _revision++);
  }

  @override
  Widget build(BuildContext context) {
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: _platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        _AccessSection(
          granted: _granted,
          access: _access,
          onRequest: _request,
          onOpenAppSettings: _platform.openAppSettings,
        ),
        if (_mobileData)
          IuxSection(
            title: 'Forfait',
            children: [
              IuxSelectField<int>(
                label: 'Données incluses',
                input: const IuxInputDescriptor(
                  semantics: IuxInputSemantics(label: 'Données incluses'),
                  helpText:
                      'Le widget trace le rythme qui mène pile au forfait.',
                ),
                value: _quota,
                options: [
                  for (final gb in _quotas)
                    IuxRadioOption<int>(
                      value: gb,
                      label: gb == 0 ? 'Pas de forfait' : '$gb Go',
                    ),
                ],
                onChanged: _setQuota,
              ),
              const IuxGap.standard(),
              IuxSelectField<int>(
                label: 'Jour de reprise',
                input: const IuxInputDescriptor(
                  semantics: IuxInputSemantics(label: 'Jour de reprise'),
                  helpText: 'Le jour du mois où le forfait repart à zéro.',
                ),
                value: _cycleDay,
                options: [
                  for (var day = 1; day <= 28; day++)
                    IuxRadioOption<int>(
                      value: day,
                      label: day == 1 ? '1er du mois' : 'Le $day',
                    ),
                ],
                onChanged: _setCycleDay,
              ),
            ],
          ),
      ],
    );
  }
}

/// Un accès : comment le lire, comment le demander, et comment l'expliquer.
class _Access {
  const _Access({
    required this.has,
    required this.request,
    required this.button,
    required this.description,
    this.restricted = false,
  });

  final Future<bool> Function() has;
  final Future<void> Function() request;
  final String button;
  final String description;

  /// Accordé dans les réglages d'Android, que les « paramètres restreints »
  /// bloquent d'abord pour une appli installée hors du Play Store.
  final bool restricted;

  static _Access of(WuxWidgetKind kind, WuxPlatform platform) => switch (kind) {
    WuxWidgetKind.bluetooth => _Access(
      has: platform.hasBluetoothPermission,
      request: platform.requestBluetoothPermission,
      button: 'Autoriser « Appareils à proximité »',
      description:
          'Halo lit le nom, le type et la batterie des appareils '
          'Bluetooth connectés. Rien ne quitte le téléphone.',
    ),
    WuxWidgetKind.media => _Access(
      has: platform.hasMediaAccess,
      request: platform.openMediaAccess,
      button: "Ouvrir l'accès aux notifications",
      description:
          "Android ne dit ce qui joue, et ne laisse le piloter, qu'aux "
          'applis autorisées à « accéder aux notifications ». Halo n\'en lit '
          'aucune : il suit seulement la lecture. Rien ne quitte le '
          'téléphone.',
      restricted: true,
    ),
    WuxWidgetKind.timer => _Access(
      has: platform.hasNotificationPermission,
      request: platform.requestNotificationPermission,
      button: 'Autoriser les notifications',
      description:
          'À la fin d\'un minuteur, Halo sonne et affiche une '
          'notification. Sans cette autorisation, le widget indique '
          'seulement « Minuteur terminé ».',
    ),
    _ => _Access(
      has: platform.hasUsageAccess,
      request: platform.openUsageAccess,
      button: "Ouvrir l'accès aux données d'utilisation",
      description:
          "Android réserve la durée d'utilisation des applis et la "
          'consommation de données aux applis autorisées dans ses '
          'réglages : activez Halo dans la liste. Rien ne quitte le '
          'téléphone.',
      restricted: true,
    ),
  };
}

class _AccessSection extends StatelessWidget {
  const _AccessSection({
    required this.granted,
    required this.access,
    required this.onRequest,
    required this.onOpenAppSettings,
  });

  final bool? granted;
  final _Access access;
  final VoidCallback onRequest;
  final VoidCallback onOpenAppSettings;

  static const _appSettings = 'Ouvrir la fiche de Halo';

  @override
  Widget build(BuildContext context) {
    final label = access.button;
    return IuxSection(
      title: 'Accès',
      description: access.description,
      children: [
        if (granted != null)
          IuxStatusIndicator(
            status: granted!
                ? const IuxStatus.success('Accordé')
                : const IuxStatus.neutral('Non accordé'),
          ),
        if (granted == false) ...[
          const IuxGap.standard(),
          IuxButton(
            label: label,
            action: IuxActionDescriptor.primary(
              semantics: IuxActionSemantics(label: label),
              role: IuxActionRole.custom,
            ),
            expand: true,
            onActivate: onRequest,
          ),
          if (access.restricted) ...[
            const IuxGap.standard(),
            Text(
              "Si Android répond « L'accès a été refusé à cette appli » : "
              'Halo, installé hors du Play Store, relève des paramètres '
              'restreints. Dans sa fiche, touchez ⋮ puis « Autoriser les '
              'paramètres restreints », confirmez, et revenez activer '
              "l'accès.",
              style: IuxTypographyTheme.of(context).body,
            ),
            const IuxGap.standard(),
            IuxButton(
              label: _appSettings,
              action: const IuxActionDescriptor(
                semantics: IuxActionSemantics(label: _appSettings),
              ),
              expand: true,
              onActivate: onOpenAppSettings,
            ),
          ],
        ],
      ],
    );
  }
}
