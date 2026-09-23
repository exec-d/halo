import 'package:flutter/material.dart';

import 'src/app.dart';
import 'src/home_widgets/home_widget_bridge.dart';

void main() {
  runApp(WuxApp(bridge: PluginHomeWidgetBridge()));
}
