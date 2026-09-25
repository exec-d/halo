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
  bool get _bluetooth => widget.homeWidget.kind == WuxWidgetKind.bluetooth;
  bool get _mobileData => widget.homeWidget.kind == WuxWidgetKind.mobileData;

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
    final granted = _bluetooth
        ? await _platform.hasBluetoothPermission()
        : await _platform.hasUsageAccess();
    if (!mounted) return;
    setState(() {
      if (granted != _granted) _revision++;
      _granted = granted;
    });
  }

  Future<void> _request() async {
    if (_bluetooth) {
      await _platform.requestBluetoothPermission();
    } else {
      await _platform.openUsageAccess();
    }
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
          bluetooth: _bluetooth,
          onRequest: _request,
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

class _AccessSection extends StatelessWidget {
  const _AccessSection({
    required this.granted,
    required this.bluetooth,
    required this.onRequest,
  });

  final bool? granted;
  final bool bluetooth;
  final VoidCallback onRequest;

  @override
  Widget build(BuildContext context) {
    final label = bluetooth
        ? 'Autoriser « Appareils à proximité »'
        : "Ouvrir l'accès aux données d'utilisation";
    return IuxSection(
      title: 'Accès',
      description: bluetooth
          ? 'Halo lit le nom, le type et la batterie des appareils '
                'Bluetooth connectés. Rien ne quitte le téléphone.'
          : "Android réserve la durée d'utilisation des applis et la "
                'consommation de données aux applis autorisées dans ses '
                'réglages : activez Halo dans la liste. Rien ne quitte le '
                'téléphone.',
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
        ],
      ],
    );
  }
}
