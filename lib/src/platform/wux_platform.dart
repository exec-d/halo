import 'package:flutter/services.dart';
import 'package:home_widget/home_widget.dart';

import '../home_widgets/catalog.dart';
import '../home_widgets/wux_home_widget.dart';
import 'models.dart';

export 'models.dart';

/// Frontière entre l'application et Android.
///
/// Les écrans ne parlent qu'à cette interface, ce qui permet de les tester
/// sans canal de plateforme.
abstract interface class WuxPlatform {
  /// Réglage partagé avec le widget natif.
  Future<String?> read(WuxHomeWidget widget, String name);

  /// Enregistre un réglage, puis redessine les widgets concernés.
  Future<void> write(WuxHomeWidget widget, String name, String? value);

  Future<bool> canPin();

  /// Propose à l'utilisateur d'ajouter [widget] à son écran d'accueil.
  Future<void> pin(WuxHomeWidget widget);

  /// Le widget tel qu'Android le dessine, à la taille [size] (dp), en PNG ;
  /// [sample] : avec des données d'exemple plutôt que les vraies.
  Future<Uint8List?> render(
    WuxHomeWidget widget,
    Size size, {
    bool sample = false,
  });

  /// Le fond d'écran animé en image fixe, à [size] pixels, en PNG.
  Future<Uint8List?> renderWallpaper(Size size);

  /// Intensité du fond d'écran : `discreet`, `normal` ou `vivid`.
  Future<String> wallpaperIntensity();

  Future<void> setWallpaperIntensity(String value);

  /// Vrai si le fond d'écran Halo est celui du téléphone.
  Future<bool> isWallpaperActive();

  /// Ouvre l'écran système qui applique le fond d'écran ; faux s'il manque.
  Future<bool> applyWallpaper();

  /// Vrai jusqu'à ce que [markLaunched] soit appelé une fois.
  Future<bool> isFirstLaunch();

  Future<void> markLaunched();

  Future<bool> hasCalendarPermission();

  /// Affiche la demande système ; renvoie la réponse.
  Future<bool> requestCalendarPermission();

  /// Ouvre la fiche de l'application, quand la demande n'est plus proposée.
  Future<void> openAppSettings();

  Future<List<CalendarInfo>> calendars();

  /// Le lieu de la météo, ou `null` s'il n'est pas encore choisi.
  Future<String?> weatherPlace();

  Future<List<WeatherPlace>> searchPlaces(String query);

  /// Choisit [place] et télécharge ses prévisions ; renvoie son nom.
  Future<String?> setWeatherPlace(WeatherPlace place);

  /// Utilise la position du téléphone (demande l'autorisation au besoin) ;
  /// renvoie le nom du lieu, ou `null` si elle est refusée.
  Future<String?> locateWeatherPlace();

  Future<AppInfo> appInfo();

  Future<AppStatus> status();

  /// Demande la position approximative ; renvoie la réponse.
  Future<bool> requestLocationPermission();

  /// Vrai si l'accès aux données d'utilisation est accordé (temps d'écran,
  /// données mobiles).
  Future<bool> hasUsageAccess();

  /// Ouvre le réglage d'Android qui accorde cet accès.
  Future<void> openUsageAccess();

  /// Vrai si Halo peut voir les appareils Bluetooth connectés.
  Future<bool> hasBluetoothPermission();

  /// Affiche la demande « Appareils à proximité » ; renvoie la réponse.
  Future<bool> requestBluetoothPermission();

  /// Ouvre la liste des applications exclues de l'optimisation de batterie.
  Future<void> openBatterySettings();

  /// Retélécharge la météo ; faux si le réseau ou le service a échoué.
  Future<bool> refreshWeather();

  /// L'écran demandé par un raccourci de l'icône (`wallpaper`, `widgets`,
  /// `settings`) au lancement, ou `null`.
  Future<String?> launchTarget();

  /// [handler] reçoit l'écran demandé par un raccourci touché pendant que
  /// Halo est ouvert.
  void onOpen(void Function(String target) handler);

  /// Widget dont le lanceur a ouvert les réglages, ou `null`.
  Future<WuxHomeWidget?> configuringWidget();

  /// Rend la main au lanceur après les réglages.
  Future<void> finishConfiguring();
}

/// Implémentation réelle : `home_widget` pour le stockage et l'épinglage, le
/// canal de `MainActivity.kt` pour le reste.
class AndroidWuxPlatform implements WuxPlatform {
  static const _channel = MethodChannel('dev.levilainpetit.wux/native');

  @override
  Future<String?> read(WuxHomeWidget widget, String name) =>
      HomeWidget.getWidgetData<String>(widget.key(name));

  @override
  Future<void> write(WuxHomeWidget widget, String name, String? value) async {
    await HomeWidget.saveWidgetData<String>(widget.key(name), value);
    await _channel.invokeMethod<void>('refreshWidgets');
    await HomeWidget.updateWidget(qualifiedAndroidName: widget.androidProvider);
  }

  @override
  Future<bool> canPin() async =>
      await HomeWidget.isRequestPinWidgetSupported() ?? false;

  @override
  Future<void> pin(WuxHomeWidget widget) =>
      HomeWidget.requestPinWidget(qualifiedAndroidName: widget.androidProvider);

  @override
  Future<Uint8List?> render(
    WuxHomeWidget widget,
    Size size, {
    bool sample = false,
  }) => _channel.invokeMethod<Uint8List>('renderWidget', {
    'id': widget.id,
    'width': size.width,
    'height': size.height,
    'sample': sample,
  });

  static const _launched = 'app.launched';

  @override
  Future<Uint8List?> renderWallpaper(Size size) =>
      _channel.invokeMethod<Uint8List>('renderWallpaper', {
        'width': size.width.round(),
        'height': size.height.round(),
      });

  @override
  Future<String> wallpaperIntensity() async =>
      await _channel.invokeMethod<String>('wallpaperIntensity') ?? 'discreet';

  @override
  Future<void> setWallpaperIntensity(String value) =>
      _channel.invokeMethod<void>('setWallpaperIntensity', {'value': value});

  @override
  Future<bool> isWallpaperActive() async =>
      await _channel.invokeMethod<bool>('wallpaperActive') ?? false;

  @override
  Future<bool> applyWallpaper() async =>
      await _channel.invokeMethod<bool>('applyWallpaper') ?? false;

  @override
  Future<String?> launchTarget() =>
      _channel.invokeMethod<String>('launchTarget');

  @override
  void onOpen(void Function(String target) handler) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'open' && call.arguments is String) {
        handler(call.arguments as String);
      }
    });
  }

  @override
  Future<bool> hasUsageAccess() async =>
      await _channel.invokeMethod<bool>('hasUsageAccess') ?? false;

  @override
  Future<void> openUsageAccess() =>
      _channel.invokeMethod<void>('openUsageAccess');

  @override
  Future<bool> hasBluetoothPermission() async =>
      await _channel.invokeMethod<bool>('hasBluetoothPermission') ?? false;

  @override
  Future<bool> requestBluetoothPermission() async =>
      await _channel.invokeMethod<bool>('requestBluetoothPermission') ?? false;

  @override
  Future<bool> isFirstLaunch() async =>
      await HomeWidget.getWidgetData<String>(_launched) == null;

  @override
  Future<void> markLaunched() =>
      HomeWidget.saveWidgetData<String>(_launched, '1');

  @override
  Future<bool> hasCalendarPermission() async =>
      await _channel.invokeMethod<bool>('hasCalendarPermission') ?? false;

  @override
  Future<bool> requestCalendarPermission() async =>
      await _channel.invokeMethod<bool>('requestCalendarPermission') ?? false;

  @override
  Future<void> openAppSettings() =>
      _channel.invokeMethod<void>('openAppSettings');

  @override
  Future<List<CalendarInfo>> calendars() async {
    final list = await _channel.invokeListMethod<Object?>('calendars') ?? [];
    return [
      for (final item in list)
        CalendarInfo.fromMap(item! as Map<Object?, Object?>),
    ];
  }

  @override
  Future<String?> weatherPlace() =>
      _channel.invokeMethod<String>('weatherPlace');

  @override
  Future<List<WeatherPlace>> searchPlaces(String query) async {
    final list =
        await _channel.invokeListMethod<Object?>('weatherSearch', {
          'query': query,
        }) ??
        [];
    return [
      for (final item in list)
        WeatherPlace.fromMap(item! as Map<Object?, Object?>),
    ];
  }

  @override
  Future<String?> setWeatherPlace(WeatherPlace place) async {
    final result = await _channel.invokeMapMethod<String, Object?>(
      'weatherSetPlace',
      {
        'name': place.name,
        'latitude': place.latitude,
        'longitude': place.longitude,
      },
    );
    return result?['name'] as String?;
  }

  @override
  Future<String?> locateWeatherPlace() async {
    final result = await _channel.invokeMapMethod<String, Object?>(
      'weatherLocate',
    );
    return result?['name'] as String?;
  }

  @override
  Future<AppInfo> appInfo() async {
    final map = await _channel.invokeMapMethod<String, Object?>('appInfo');
    return AppInfo(
      version: map?['version'] as String? ?? '?',
      build: (map?['build'] as num?)?.toInt() ?? 0,
    );
  }

  @override
  Future<AppStatus> status() async {
    final map =
        await _channel.invokeMapMethod<String, Object?>('status') ?? const {};
    final updated = (map['weatherUpdatedAt'] as num?)?.toInt();
    return AppStatus(
      calendar: map['calendar'] == true,
      location: map['location'] == true,
      batteryUnrestricted: map['batteryUnrestricted'] == true,
      weatherPlace: map['weatherPlace'] as String?,
      weatherUpdatedAt: updated == null
          ? null
          : DateTime.fromMillisecondsSinceEpoch(updated),
    );
  }

  @override
  Future<bool> requestLocationPermission() async =>
      await _channel.invokeMethod<bool>('requestLocationPermission') ?? false;

  @override
  Future<void> openBatterySettings() =>
      _channel.invokeMethod<void>('openBatterySettings');

  @override
  Future<bool> refreshWeather() async =>
      await _channel.invokeMethod<bool>('refreshWeather') ?? false;

  @override
  Future<WuxHomeWidget?> configuringWidget() async {
    final id = await HomeWidget.initiallyLaunchedFromHomeWidgetConfigure();
    if (id == null) return null;
    final provider = await _channel.invokeMethod<String>('widgetProvider', {
      'id': int.parse(id),
    });
    for (final widget in wuxHomeWidgets) {
      if (widget.androidProvider == provider) return widget;
    }
    return null;
  }

  @override
  Future<void> finishConfiguring() => HomeWidget.finishHomeWidgetConfigure();
}
