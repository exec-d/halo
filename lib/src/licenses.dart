import 'package:flutter/foundation.dart';

/// Licences à afficher en plus de celles des paquets Dart, que Flutter
/// collecte seul : les données et les icônes utilisées par les widgets.
void registerLicenses() {
  LicenseRegistry.addLicense(() async* {
    yield const LicenseEntryWithLineBreaks(
      ['Open-Meteo'],
      'Données météo, pluie, pollens et qualité de l\'air : Open-Meteo.com, '
      'sous licence Creative Commons Attribution 4.0 International '
      '(CC BY 4.0), https://open-meteo.com. Pollens et qualité de l\'air '
      'issus du Copernicus Atmosphere Monitoring Service (CAMS). Géocodage : '
      'GeoNames, CC BY 4.0.',
    );
    yield const LicenseEntryWithLineBreaks(
      ['Material Icons'],
      'Material Icons, Copyright Google LLC. Licensed under the Apache '
      'License, Version 2.0: https://www.apache.org/licenses/LICENSE-2.0. '
      'Utilisées par les icônes lumineuses des widgets.',
    );
  });
}
