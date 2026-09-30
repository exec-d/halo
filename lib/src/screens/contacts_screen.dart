import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../../l10n/app_localizations.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Un contact favori, tel que le widget le garde.
class FavoriteContact {
  const FavoriteContact({required this.name, required this.number, this.photo});

  factory FavoriteContact.fromJson(Map<String, dynamic> json) =>
      FavoriteContact(
        name: json['name'] as String? ?? '',
        number: json['number'] as String? ?? '',
        photo: json['photo'] as String?,
      );

  final String name;
  final String number;

  /// La copie de la photo gardée par Halo, s'il y en a une.
  final String? photo;

  Map<String, String> toJson() => {
    'name': name,
    'number': number,
    'photo': ?photo,
  };
}

/// Contacts favoris : jusqu'à six contacts choisis avec le sélecteur
/// d'Android (Halo ne voit que le contact touché), et ce que fait un toucher
/// sur le widget : appeler ou écrire un SMS.
class ContactsScreen extends StatefulWidget {
  const ContactsScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  @override
  State<ContactsScreen> createState() => _ContactsScreenState();
}

class _ContactsScreenState extends State<ContactsScreen> {
  static const _listKey = 'list';
  static const _actionKey = 'action';
  static const _max = 6;

  List<FavoriteContact> _contacts = const [];
  bool _sms = false;
  bool? _canCall;
  int _revision = 0;

  WuxPlatform get _platform => widget.platform;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final json = await _platform.read(widget.homeWidget, _listKey);
    final action = await _platform.read(widget.homeWidget, _actionKey);
    final canCall = await _platform.hasCallPermission();
    if (!mounted) return;
    setState(() {
      _contacts = _decode(json);
      _sms = action == 'sms';
      _canCall = canCall;
    });
  }

  static List<FavoriteContact> _decode(String? json) {
    if (json == null || json.isEmpty) return const [];
    try {
      return [
        for (final item in jsonDecode(json) as List<dynamic>)
          FavoriteContact.fromJson(item as Map<String, dynamic>),
      ];
    } on FormatException {
      return const [];
    }
  }

  Future<void> _save(List<FavoriteContact> contacts) async {
    setState(() => _contacts = contacts);
    await _platform.write(
      widget.homeWidget,
      _listKey,
      jsonEncode([for (final c in contacts) c.toJson()]),
    );
    if (mounted) setState(() => _revision++);
  }

  Future<void> _add() async {
    final picked = await _platform.pickContact();
    if (picked == null || !mounted) return;
    await _save([..._contacts, FavoriteContact.fromJson(picked)]);
  }

  Future<void> _remove(FavoriteContact contact) =>
      _save([..._contacts]..remove(contact));

  Future<void> _setSms(bool sms) async {
    setState(() => _sms = sms);
    await _platform.write(widget.homeWidget, _actionKey, sms ? 'sms' : 'call');
    if (mounted) setState(() => _revision++);
  }

  Future<void> _requestCall() async {
    final granted = await _platform.requestCallPermission();
    if (!mounted) return;
    setState(() {
      _canCall = granted;
      _revision++;
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final add = l10n.contactsAdd;
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: _platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        IuxSection(
          title: l10n.contactsTitle,
          description: l10n.contactsDescription,
          children: [
            for (final contact in _contacts) ...[
              IuxButton(
                label: l10n.contactsRemove(contact.name),
                action: IuxActionDescriptor(
                  semantics: IuxActionSemantics(
                    label: l10n.contactsRemove(contact.name),
                  ),
                ),
                expand: true,
                onActivate: () => _remove(contact),
              ),
              const IuxGap.standard(),
            ],
            if (_contacts.length < _max)
              IuxButton(
                label: add,
                action: IuxActionDescriptor.primary(
                  semantics: IuxActionSemantics(label: add),
                  role: IuxActionRole.custom,
                ),
                expand: true,
                onActivate: _add,
              )
            else
              Text(
                l10n.contactsFull,
                style: IuxTypographyTheme.of(context).body,
              ),
          ],
        ),
        IuxSection(
          title: l10n.contactsActionTitle,
          children: [
            IuxSwitch(
              label: l10n.contactsSms,
              input: IuxInputDescriptor(
                semantics: IuxInputSemantics(label: l10n.contactsSms),
                helpText: l10n.contactsSmsHelp,
              ),
              value: IuxSelectionState.fromSelected(_sms),
              onChanged: _setSms,
            ),
            if (!_sms && _canCall == false) ...[
              const IuxGap.standard(),
              Text(
                l10n.contactsCallHelp,
                style: IuxTypographyTheme.of(context).body,
              ),
              const IuxGap.standard(),
              IuxButton(
                label: l10n.contactsCallButton,
                action: IuxActionDescriptor(
                  semantics: IuxActionSemantics(label: l10n.contactsCallButton),
                ),
                expand: true,
                onActivate: _requestCall,
              ),
            ],
          ],
        ),
      ],
    );
  }
}
