import 'package:flutter/material.dart';

import '../platform/models.dart';

/// Aperçus des widgets, dessinés en Flutter avec les mêmes polices, couleurs
/// et textes que les widgets natifs. Seule la mise en page est dupliquée :
/// elle doit suivre `res/layout/widget_clock*.xml` et `agenda_*.xml`.

/// Fond sombre derrière un aperçu : le style néon est pensé pour un fond
/// d'écran foncé.
class WallpaperFrame extends StatelessWidget {
  const WallpaperFrame({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF05070D), Color(0xFF0B1224)],
        ),
      ),
      child: Padding(padding: const EdgeInsets.all(12), child: child),
    );
  }
}

/// La police mono du système, seule police « technique » qu'un widget accepte.
const _mono = 'monospace';

/// Le halo : l'ombre sans décalage des widgets natifs, doublée pour l'éclat.
List<Shadow> _glow(Color color, double radius) => [
  Shadow(color: color, blurRadius: radius),
  Shadow(color: color.withValues(alpha: 0.6), blurRadius: radius * 2),
];

List<BoxShadow> _boxGlow(Color color) => [
  BoxShadow(color: color, blurRadius: 6),
  BoxShadow(color: color.withValues(alpha: 0.5), blurRadius: 14),
];

class _NeonLine extends StatelessWidget {
  const _NeonLine({
    required this.color,
    required this.glow,
    this.vertical = false,
  });

  final Color color;
  final Color glow;
  final bool vertical;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: vertical ? 2 : double.infinity,
      height: vertical ? double.infinity : 2,
      margin: vertical
          ? const EdgeInsets.symmetric(horizontal: 8)
          : const EdgeInsets.symmetric(vertical: 6),
      decoration: BoxDecoration(color: color, boxShadow: _boxGlow(glow)),
    );
  }
}

class _Alarm extends StatelessWidget {
  const _Alarm({
    required this.label,
    required this.palette,
    required this.size,
  });

  final String label;
  final WidgetPalette palette;
  final double size;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(
          Icons.alarm,
          size: size * 1.4,
          color: palette.core,
          shadows: _glow(palette.glow, 5),
        ),
        const SizedBox(width: 6),
        Text(
          label.toUpperCase(),
          style: TextStyle(
            fontFamily: _mono,
            fontSize: size,
            color: palette.core,
            shadows: _glow(palette.glow, 5),
          ),
        ),
      ],
    );
  }
}

TextStyle _time(WidgetPalette palette) => TextStyle(
  fontFamily: 'WuxDots',
  fontSize: 96,
  height: 1,
  color: palette.core,
  shadows: _glow(palette.glow, 8),
);

/// Reproduction de `widget_clock.xml` (grande) et `widget_clock_compact.xml`.
class ClockWidgetPreview extends StatelessWidget {
  const ClockWidgetPreview({
    super.key,
    required this.clock,
    required this.palette,
    this.compact = false,
  });

  final ClockPreview clock;
  final WidgetPalette palette;
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final dateSize = compact ? 12.0 : 17.0;
    return SizedBox(
      height: compact ? 64 : 110,
      child: Row(
        children: [
          Flexible(
            child: FittedBox(child: Text(clock.time, style: _time(palette))),
          ),
          _NeonLine(color: palette.line, glow: palette.glow, vertical: true),
          IntrinsicWidth(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  (compact ? clock.date : clock.dateStacked).toUpperCase(),
                  style: TextStyle(
                    fontFamily: _mono,
                    fontSize: dateSize,
                    height: 1.1,
                    color: palette.core,
                    shadows: _glow(palette.glow, 5),
                  ),
                ),
                _NeonLine(color: palette.line, glow: palette.glow),
                if (clock.nextAlarm case final alarm?)
                  _Alarm(label: alarm, palette: palette, size: dateSize - 1),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Reproduction des éléments `agenda_heading`, `agenda_row` et `agenda_note`,
/// en une colonne : l'aperçu n'a pas la largeur de deux.
class AgendaWidgetPreview extends StatelessWidget {
  const AgendaWidgetPreview({
    super.key,
    required this.days,
    required this.palette,
  });

  final List<AgendaDayPreview> days;
  final WidgetPalette palette;

  @override
  Widget build(BuildContext context) {
    final note = TextStyle(
      fontFamily: _mono,
      fontSize: 13,
      color: palette.line,
      shadows: _glow(palette.glow, 4),
    );
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              for (final day in days) ...[
                Padding(
                  padding: const EdgeInsets.only(left: 2, top: 6, bottom: 4),
                  child: Text(
                    day.label,
                    style: TextStyle(
                      fontFamily: _mono,
                      fontSize: 17,
                      letterSpacing: 17 * 0.08,
                      color: palette.line,
                      shadows: _glow(palette.glow, 6),
                    ),
                  ),
                ),
                if (day.events.isEmpty)
                  Padding(
                    padding: const EdgeInsets.fromLTRB(17, 3, 0, 3),
                    child: Text('Aucun événement', style: note),
                  ),
                for (final event in day.events) _EventRow(event, palette),
              ],
            ],
          ),
        ),
      ],
    );
  }
}

class _EventRow extends StatelessWidget {
  const _EventRow(this.event, this.palette);

  final AgendaEventPreview event;
  final WidgetPalette palette;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: IntrinsicHeight(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              width: 3,
              margin: const EdgeInsets.symmetric(horizontal: 5),
              decoration: BoxDecoration(
                color: event.color,
                boxShadow: _boxGlow(event.color),
              ),
            ),
            const SizedBox(width: 6),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    event.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontFamily: _mono,
                      fontSize: 15,
                      color: palette.core,
                      shadows: _glow(palette.glow, 4),
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    event.detail,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontFamily: _mono,
                      fontSize: 13,
                      color: palette.line,
                      shadows: _glow(palette.glow, 3),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
