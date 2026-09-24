import 'package:flutter/material.dart';

import 'src/app.dart';
import 'src/licenses.dart';
import 'src/platform/wux_platform.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  registerLicenses();
  final platform = AndroidWuxPlatform();
  // Lancée par un appui long sur un widget : on ouvre directement ses réglages.
  final configuring = await platform.configuringWidget();
  runApp(WuxApp(platform: platform, configuring: configuring));
}
