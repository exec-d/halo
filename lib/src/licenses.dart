import 'package:flutter/foundation.dart';

/// Licences à afficher en plus de celles des paquets Dart, que Flutter
/// collecte seul : les données et les icônes utilisées par les widgets.
void registerLicenses() {
  LicenseRegistry.addLicense(() async* {
    yield const LicenseEntryWithLineBreaks(['Halo'], _mit);
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
    yield const LicenseEntryWithLineBreaks(
      ['Chakra Petch', 'IBM Plex Sans', 'JetBrains Mono'],
      'Polices de la galerie. Chakra Petch, Copyright 2018 The Chakra Petch '
      'Project Authors ; IBM Plex Sans, Copyright 2017 IBM Corp. ; JetBrains '
      'Mono, Copyright 2020 The JetBrains Mono Project Authors. Sous licence '
      'SIL Open Font License, Version 1.1 : https://openfontlicense.org.',
    );
  });
}

/// La licence de Halo lui-même (fichier LICENSE du dépôt).
const _mit = '''MIT License

Copyright (c) 2026 Kevin Delfour

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.''';
