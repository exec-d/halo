import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

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
    final l10n = AppLocalizations.of(context);
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
            title: l10n.mobileDataPlanTitle,
            children: [
              IuxSelectField<int>(
                label: l10n.mobileDataIncluded,
                input: IuxInputDescriptor(
                  semantics: IuxInputSemantics(label: l10n.mobileDataIncluded),
                  helpText: l10n.mobileDataIncludedHelp,
                ),
                value: _quota,
                options: [
                  for (final gb in _quotas)
                    IuxRadioOption<int>(
                      value: gb,
                      label: gb == 0
                          ? l10n.mobileDataNoPlan
                          : l10n.mobileDataGigabytes(gb),
                    ),
                ],
                onChanged: _setQuota,
              ),
              const IuxGap.standard(),
              IuxSelectField<int>(
                label: l10n.mobileDataCycleDay,
                input: IuxInputDescriptor(
                  semantics: IuxInputSemantics(label: l10n.mobileDataCycleDay),
                  helpText: l10n.mobileDataCycleDayHelp,
                ),
                value: _cycleDay,
                options: [
                  for (var day = 1; day <= 28; day++)
                    IuxRadioOption<int>(
                      value: day,
                      label: day == 1
                          ? l10n.mobileDataFirstOfMonth
                          : l10n.mobileDataDayOfMonth(day),
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
  final String Function(AppLocalizations l10n) button;
  final String Function(AppLocalizations l10n) description;

  /// Accordé dans les réglages d'Android, que les « paramètres restreints »
  /// bloquent d'abord pour une appli installée hors du Play Store.
  final bool restricted;

  static _Access of(WuxWidgetKind kind, WuxPlatform platform) => switch (kind) {
    WuxWidgetKind.bluetooth => _Access(
      has: platform.hasBluetoothPermission,
      request: platform.requestBluetoothPermission,
      button: (l10n) => l10n.accessBluetoothButton,
      description: (l10n) => l10n.accessBluetoothDescription,
    ),
    WuxWidgetKind.media => _Access(
      has: platform.hasMediaAccess,
      request: platform.openMediaAccess,
      button: (l10n) => l10n.accessMediaButton,
      description: (l10n) => l10n.accessMediaDescription,
      restricted: true,
    ),
    WuxWidgetKind.timer => _Access(
      has: platform.hasNotificationPermission,
      request: platform.requestNotificationPermission,
      button: (l10n) => l10n.accessTimerButton,
      description: (l10n) => l10n.accessTimerDescription,
    ),
    _ => _Access(
      has: platform.hasUsageAccess,
      request: platform.openUsageAccess,
      button: (l10n) => l10n.accessUsageButton,
      description: (l10n) => l10n.accessUsageDescription,
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

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final label = access.button(l10n);
    final appSettings = l10n.accessOpenAppInfo;
    return IuxSection(
      title: l10n.accessTitle,
      description: access.description(l10n),
      children: [
        if (granted != null)
          IuxStatusIndicator(
            status: granted!
                ? IuxStatus.success(l10n.accessGranted)
                : IuxStatus.neutral(l10n.accessNotGranted),
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
              l10n.accessRestrictedHelp,
              style: IuxTypographyTheme.of(context).body,
            ),
            const IuxGap.standard(),
            IuxButton(
              label: appSettings,
              action: IuxActionDescriptor(
                semantics: IuxActionSemantics(label: appSettings),
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
