import 'package:flutter/material.dart';

import '../platform/models.dart';

/// Faux fond d'écran derrière un aperçu, proche de la maquette.
///
/// Le vrai fond d'écran n'est pas lisible sans permission de stockage ; ce
/// dégradé suffit à juger la lisibilité d'un widget transparent.
class WallpaperFrame extends StatelessWidget {
  const WallpaperFrame({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(24),
      child: DecoratedBox(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF4A5BD4), Color(0xFFB45BD0), Color(0xFFE0689A)],
          ),
        ),
        child: Padding(padding: const EdgeInsets.all(16), child: child),
      ),
    );
  }
}

const _shadow = [
  Shadow(color: Color(0x66000000), offset: Offset(0, 1), blurRadius: 4),
];

/// Reproduction de `res/layout/widget_clock.xml`.
class ClockWidgetPreview extends StatelessWidget {
  const ClockWidgetPreview({
    super.key,
    required this.clock,
    required this.palette,
  });

  final ClockPreview clock;
  final WidgetPalette palette;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 24),
      child: Column(
        children: [
          Text(
            clock.time,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 64,
              fontWeight: FontWeight.w300,
              height: 1,
              shadows: _shadow,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            clock.date,
            textAlign: TextAlign.center,
            style: TextStyle(
              color: palette.clockAccent,
              fontSize: 18,
              shadows: _shadow,
            ),
          ),
          if (clock.nextAlarm case final alarm?) ...[
            const SizedBox(height: 4),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.alarm, size: 16, color: palette.clockAccent),
                const SizedBox(width: 4),
                Text(
                  alarm,
                  style: TextStyle(
                    color: palette.clockAccent,
                    fontSize: 15,
                    shadows: _shadow,
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

/// Reproduction de `AgendaContent` (`AgendaWidget.kt`).
///
/// Les textes viennent du code natif, déjà mis en forme : seule la mise en
/// page est dupliquée ici, et elle doit suivre celle du widget.
class AgendaWidgetPreview extends StatelessWidget {
  const AgendaWidgetPreview({
    super.key,
    required this.days,
    required this.background,
    required this.palette,
  });

  final List<AgendaDayPreview> days;
  final AgendaBackground background;
  final WidgetPalette palette;

  @override
  Widget build(BuildContext context) {
    final surface = background == AgendaBackground.surface;
    final primary = surface ? palette.primary : Colors.white;
    final text = surface ? palette.onSurface : Colors.white;
    final secondary = surface
        ? palette.onSurfaceVariant
        : const Color(0xE6FFFFFF);

    final content = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        for (final day in days) ...[
          Padding(
            padding: const EdgeInsets.only(top: 6, bottom: 2),
            child: Text(
              day.label,
              style: TextStyle(
                color: primary,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          if (day.events.isEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 3),
              child: Text(
                'Aucun événement',
                style: TextStyle(color: secondary, fontSize: 13),
              ),
            ),
          for (final event in day.events)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 3),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          event.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            color: text,
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        Text(
                          event.detail,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(color: secondary, fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      color: event.color,
                      shape: BoxShape.circle,
                    ),
                  ),
                ],
              ),
            ),
        ],
      ],
    );

    return Container(
      padding: const EdgeInsets.fromLTRB(12, 8, 10, 8),
      color: surface ? palette.surface : null,
      child: content,
    );
  }
}
