# Wallpapers, screen saver, tiles and shortcuts

*[Français](../wallpaper.md)*

Halo offers three animated wallpapers (Circuit, and its Back to the Future
and Iron Man variants), a screen saver,
three quick settings tiles and shortcuts on its icon. The intensity (Subtle, Normal,
Vivid) is shared by all wallpapers.

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

- **Tilt**: layers slide more the deeper they are (accelerometer), and a
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
  charging; otherwise, the wallpaper is a still image. Hand tremor does not
  restart drawing: only a real gesture does.
- Tilt comes from the accelerometer alone, filtered: the gravity sensor
  would also power the gyroscope, which uses far more.
- **Android Battery Saver** on: the wallpaper freezes (no sensor, no pulses,
  no animation, one frame per state change), for all three wallpapers.
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

## Back to the Future

The same phone interior as Circuit (`CircuitScene.build(…, Core.FLUX)`),
with the film's flux capacitor in place of the battery and the coil: a box
with a window, the Y of three electrodes, cables running up to the
motherboard. The set's dimensions (600 × 760 mm) are scaled to the battery
rectangle. The service is `FluxWallpaperService`, which extends
`HaloWallpaperService`.

- **The flux capacitor**: a pulse runs from lamp to lamp along the three
  arms to the core, which flashes; faster while charging.
- **The gauge**, above the window: ten cells for the battery level.
- **Busier boards**: on the motherboard, the three time circuit displays
  (destination 2015 · 16:29, present at the real time, last departed
  1985 · 01:21) and three power coils above the cables; on the bottom board,
  Mr. Fusion, which lights up while charging.
- **The rest is Circuit's**: tilt, network, wake-up.
- **Battery**: on screen, the flux capacitor animates continuously at about
  22 frames per second; nothing runs when the wallpaper is not visible.

## Iron Man

Same principle (`Core.ARC`, `ArcWallpaperService`): Tony Stark's arc reactor,
centred on the plate that replaces the battery, linked to the motherboard by
two cables. Ten coils around the core with the new element's triangle.

- **The casing**: knurled double ring, ten bolts, ten wound coils separated
  by spacers, inner collar, beaded core housing.
- **Start-up**: each time the screen turns on, the coils light up one by one,
  then the core flares.
- **The coils**: one lit per tenth of battery; each flickers, light shows
  between its windings, a glow runs around the ring.
- **Two energy tracks** turn in opposite directions, faster while charging.
- **The core beats** (two close beats) and its rays stretch; while charging,
  particles spiral into it; below 15 % battery, it flickers.
- Tilt, network and wake-up are Circuit's; same frame rate as the flux
  capacitor.

To look at the wallpapers without a phone, `tool/scenes/render.sh` draws them
as PNG files on the computer (see the script's header); `render.sh --thumbs`
redraws the flux capacitor and arc reactor thumbnails in Android's picker.

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
