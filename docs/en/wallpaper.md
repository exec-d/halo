# Wallpapers, screen saver, tiles and shortcuts

*[Français](../wallpaper.md)*

Halo offers six animated wallpapers (Circuit, Grid, Megacity, Code, Neon,
Sentinel), a screen saver,
three quick settings tiles and shortcuts on its icon. The intensity (Subtle, Normal,
Vivid) is shared by the six wallpapers.

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

## The science-fiction wallpapers

Five scenes inspired by science-fiction and AI films. Unlike Circuit, they
keep their own colors (cyan and orange, green, neon…) rather than the
phone's. All of them follow the tilt, nearby layers moving more than distant
ones, and stop as soon as the wallpaper is no longer visible.

| Wallpaper | What it shows | Code |
| --- | --- | --- |
| Grid | An endless scrolling grid; two light cycles, cyan and orange, draw their walls, turning at right angles | `GridScene.kt` |
| Megacity | Three layers of towers in the rain, a pyramid, flare stacks, flying cars, two searchlights, a glowing billboard | `MegacityScene.kt` |
| Code | A rain of green characters on two layers, slanting with the phone | `CodeScene.kt` |
| Neon | A street lined with buzzing signs reflected on the wet ground; now and then, the image glitches | `NeonScene.kt` |
| Sentinel | The red eye of an AI: its core breathes, follows the tilt, and brightens on unlock | `SentinelScene.kt` |

`SceneWallpaperService.kt` runs these scenes (frame rate, sensors,
intensity); `SceneWallpapers.kt` declares one service per wallpaper.

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
