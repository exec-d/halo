import 'dart:ui';

/// Un agenda du téléphone.
class CalendarInfo {
  const CalendarInfo({
    required this.id,
    required this.name,
    required this.account,
    required this.color,
  });

  factory CalendarInfo.fromMap(Map<Object?, Object?> map) => CalendarInfo(
    id: (map['id']! as num).toInt(),
    name: map['name']! as String,
    account: map['account']! as String,
    color: Color((map['color']! as num).toInt()),
  );

  final int id;
  final String name;
  final String account;
  final Color color;
}
