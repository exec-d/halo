import 'package:flutter/material.dart';

import 'src/app.dart';
import 'src/platform/wux_platform.dart';

void main() {
  runApp(WuxApp(platform: AndroidWuxPlatform()));
}
