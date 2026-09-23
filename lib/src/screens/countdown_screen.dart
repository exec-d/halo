import 'package:flutter/material.dart';
import 'package:iux_flutter/iux_flutter.dart';

import '../home_widgets/wux_home_widget.dart';
import '../platform/wux_platform.dart';
import 'settings_scaffold.dart';

/// Compte à rebours : un titre et une date, relus par `CountdownWidget.kt`
/// (`countdown.title`, `countdown.date` au format AAAA-MM-JJ).
class CountdownScreen extends StatefulWidget {
  const CountdownScreen({
    super.key,
    required this.homeWidget,
    required this.platform,
    this.allowPin = true,
  });

  final WuxHomeWidget homeWidget;
  final WuxPlatform platform;
  final bool allowPin;

  @override
  State<CountdownScreen> createState() => _CountdownScreenState();
}

class _CountdownScreenState extends State<CountdownScreen> {
  final _title = TextEditingController();
  IuxDateParts _date = const IuxDateParts.empty();
  int _revision = 0;

  static const _labels = IuxDateFieldLabels(
    day: 'Jour',
    month: 'Mois',
    year: 'Année',
  );

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final title = await widget.platform.read(widget.homeWidget, 'title');
    final date = DateTime.tryParse(
      await widget.platform.read(widget.homeWidget, 'date') ?? '',
    );
    if (!mounted) return;
    _title.text = title ?? '';
    if (date != null) {
      setState(
        () => _date = IuxDateParts(
          day: date.day,
          month: date.month,
          year: date.year,
        ),
      );
    }
  }

  @override
  void dispose() {
    _title.dispose();
    super.dispose();
  }

  Future<void> _saveTitle(String value) async {
    await widget.platform.write(widget.homeWidget, 'title', value.trim());
    setState(() => _revision++);
  }

  Future<void> _saveDate(IuxDateParts parts) async {
    setState(() => _date = parts);
    final date = parts.date;
    if (date == null) return;
    final iso =
        '${date.year.toString().padLeft(4, '0')}-'
        '${date.month.toString().padLeft(2, '0')}-'
        '${date.day.toString().padLeft(2, '0')}';
    await widget.platform.write(widget.homeWidget, 'date', iso);
    setState(() => _revision++);
  }

  @override
  Widget build(BuildContext context) {
    final invalid = _date.isComplete && _date.date == null;
    return SettingsScaffold(
      homeWidget: widget.homeWidget,
      platform: widget.platform,
      allowPin: widget.allowPin,
      revision: _revision,
      sections: [
        IuxSection(
          title: 'Évènement',
          children: [
            IuxTextField(
              input: const IuxInputDescriptor(
                semantics: IuxInputSemantics(label: 'Titre'),
                helpText: 'Par exemple « Vacances ».',
              ),
              controller: _title,
              onChanged: _saveTitle,
            ),
            const IuxGap.standard(),
            IuxDateField(
              input: IuxInputDescriptor(
                semantics: const IuxInputSemantics(label: 'Date'),
                helpText: 'Jour, mois et année.',
                validation: invalid
                    ? const IuxInputValidation.invalid(
                        "Cette date n'existe pas.",
                      )
                    : const IuxInputValidation.notValidated(),
              ),
              labels: _labels,
              value: _date,
              onChanged: _saveDate,
            ),
          ],
        ),
      ],
    );
  }
}
