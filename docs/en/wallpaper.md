# Wallpapers, screen saver, tiles and shortcuts

*[Français](../wallpaper.md)*

Halo offers an animated wallpaper (Circuit), a screen saver,
three quick settings tiles and shortcuts on its icon. The intensity can be set to
Subtle, Normal or Vivid.

## Circuit

The inside of a Pixel 7, seen through the screen, as a neon schematic in the
phone's colours. It is chosen in Halo (*Wallpaper* section), for the home
screen, the lock screen or both.

### What is drawn

Four layers, from the back to the glass:

1. **Chassis**: frame, screws, camera bar across the back, power and volume
   buttons on the right, SIM tray on the left, wireless charging coil.
2. **Battery**, with its real level.
3. **Boards**: main board (processor, memory, modem, power management, rear
   lenses side by side in their pill, flash), bottom board (USB-C, speaker,
   vibration motor, microphone), flex cable, traces and small components.
4. **Glass**: front camera in the punch hole, earpiece, light sensor,
   fingerprint reader.

The scene contains no text, so as not to mix with that of the widgets.

### What moves

- **Tilt**: layers slide more the deeper they are (gravity sensor), and a
  reflection crosses the glass. The resting position slowly follows the hand:
  it is movement that makes things move, not posture.
- **Battery**: the real level; while charging, it breathes and pulses rise
  from the USB-C port.
- **Network**: pulses run from the antenna to the processor when data flows
  (`TrafficStats`); the antennas glow according to signal strength.
- **Power-on**: each time the screen turns on, the scene is there right away,
  then components and traces light up one by one from the processor.
- **Lock screen**: the top is darkened for the clock.

### Settings

**Intensity**: Subtle (default), Normal or Vivid. Subtle keeps the widgets
and icons on top readable.

### Phone battery

- Nothing runs when the wallpaper is not visible.
- The animation only runs continuously during movement, a pulse, power-on or
  charging; otherwise, the wallpaper is a still image.
- Fixed parts are drawn once, as `ALPHA_8` masks tinted when drawn (glow at
  half resolution).

### Code

`android/app/src/main/kotlin/dev/levilainpetit/wux/wallpaper/`:

| File | Role |
| --- | --- |
| `CircuitScene.kt` | The geometry (seen from the back, flipped), the masks, the pulse paths |
| `CircuitPainter.kt` | One frame: offset layers, battery, antennas, pulses, power-on, reflection |
| `HaloWallpaperService.kt` | Sensor, battery, traffic, signal, frame rate |
| `WallpaperPreview.kt` | The in-app preview and opening the system screen |
| `WallpaperSettings.kt` | The intensity |

## Screen saver

While charging or docked, Android can show a screen saver
(*Settings → Display → Screen saver*, choose "Halo"): a large neon clock on
the Circuit scene, with the date, the next alarm, the next event and the
weather. The text shifts slightly every minute to avoid burn-in. Code:
`dream/`.

## Quick settings tiles

To add from the quick settings panel (pencil):

| Tile | Shows | Tap |
| --- | --- | --- |
| Weather | Temperature and current conditions | Downloads the forecast again |
| Halo wallpaper | The intensity of the applied wallpaper | Subtle → Normal → Vivid |
| Battery | Level and estimate | Opens battery usage |

Code: `tiles/QuickTiles.kt`.

## Icon shortcuts

A long press on the Halo icon: **Wallpaper**, **Widgets**, **Settings**.
Each opens the app on the matching screen (`halo://…`,
`res/xml/shortcuts.xml`).
