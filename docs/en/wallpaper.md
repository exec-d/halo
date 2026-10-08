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
- **A much denser motherboard**: a hatched copper pour cleared around the
  chips and traces, stitching vias, test points, many surface-mount parts,
  an audio codec and its crystal, a gyroscope, an inductor, a flex
  connector, a charge controller and meandered traces.
- **The rest is Circuit's**: tilt, network, wake-up.
- **Battery**: on screen, the flux capacitor animates at about 22 frames per
  second for 30 s after the screen turns on or a gesture on the home screen
  (touch, page change), then slows to 5 frames per second until the next
  gesture; while charging, always at full rate. Nothing runs when the wallpaper is not visible.

## Iron Man

The phone wears the armour (`Core.ARC`, `ArcCore`, `ArcWallpaperService`): it
redraws the whole phone, like the themes' cores (see below).

- **The frame**: a belt of bolted plates, armoured pistons for the buttons,
  an iris around the front camera, a reinforced housing for the USB port,
  conduits along the sides.
- **The plates**: the helmet and its status lights, the collarbones, the
  chest around the reactor housing, the rib slats, the abs, the vertebral
  sternum and two pistons; between them, a hexagonal mesh and energy
  conduits. Each plate hides the ones behind it.
- **The reactor**: ten-sided housing, capacitors, bolted casing, graduated
  ring, ten wound coils and their leads, clamps, core.
- **It beats** (two beats, then a pause): the edges facing it light up, a
  wave runs through the mesh and along the conduits. One coil per tenth of
  battery. While charging, the current rises from the port to the reactor,
  the rings turn and the pistons stretch; with network traffic, pulses come
  from the corners and the status lights blink fast.
- Tilt and power-on are Circuit's; same frame rate as the others.

To look at the wallpapers without a phone, `tool/scenes/render.sh` draws them
as PNG files on the computer (see the script's header); `render.sh --thumbs`
redraws the wallpapers' thumbnails in Android's picker.

## Themes

Pixel theme packs (Wallpaper & style › Theme pack) are reserved for Google:
no API lets an app appear there. Halo makes its own, in its Themes screen
(from the gallery and the Wallpapers tab). A theme carries the name and id of
its wallpaper:

| Theme | Palette (core, lines, glow) | Sounds: ringtone, notification, alarm |
| --- | --- | --- |
| Circuit | bluish white, sky blue, electric blue | Data bus, Pulse, Boot |
| Back to the Future | cream, amber, orange | 88 mph, Flux, Departure time |
| Iron Man | ivory, gold, red | Repulsor, Interface, Reactor |
| Quantum physics | lavender, light violet, violet | Superposition, Entanglement, Collapse |
| Artificial intelligence | bluish white, sky blue, electric blue | Inference, Token, Awakening |
| Atomic energy | cream, yellow, golden yellow | Chain reaction, Neutron, Criticality |
| Fallout | green white, apple green, green | Vault door, Terminal, Geiger counter |
| Ghost in the Shell | water white, turquoise, sea green | Dive, Ghost, Synchronisation |

Iron Man and the last five wallpapers (`CoreArt` and its subclasses:
`ArcCore`, `QuantumCore`, `NeuralCore`, `AtomCore`, `VaultCore`, `GhostCore`) redraw the whole phone
(`wholePhone`): the back, the middle and the front, each with its own
animation (battery, charging, power-on), power-on outlines and pulse
routes.

- Quantum physics: the forest of cables at the back, the plate, columns and
  chip holder in the middle, the cables in front, where the network pulses
  run.
- Artificial intelligence: the accelerator and its brain in the middle, the
  detailed motherboard and bottom board in front; the network runs along the
  edge bundle.
- Atomic energy: the vessel and its mechanisms in front, the internals and
  the core in the middle; the network follows the water loop.
- Ghost in the Shell: the tunnel's rays and the cascading glyphs at the
  back, the network's layers and their nodes in the middle, the reticle in
  front; the network dives from the corners to the vanishing point.
- Fallout: the Pip-Boy's casing in front, the phosphor glow at the back; the
  screen cycles through the game's five tabs (`VaultBoy` holds the Vault Boy
  mask); the network follows the casing's seams.

All take the phone's colours, or their theme's. Like the flux capacitor and the
reactor, they run at full rate for 30 s after power-on or a gesture, then
slow down. Film and game names name the themes, and drawings and sounds are original,
except for Fallout: its screen uses the Pip-Boy's texts and the Vault Boy,
taken from a screenshot. Halo is for personal use and is not distributed.

Applying a theme:

1. **Wallpaper**: Halo remembers the theme, then Android shows its live
   wallpaper apply screen, even if it is already set: System UI only picks
   up a live wallpaper's colours when it is applied; otherwise it waits for
   the next time the screen sleeps.
2. **Colours**: the wallpaper draws in the theme's palette and announces it to
   Android (`onComputeColors`). If Colours is set to "Wallpaper", the system
   derives its Material You scheme from it; the widgets, which take the system
   colours (`values-v31/colors.xml`), follow. On a basic colour, the system
   ignores the wallpaper: the theme screen says so.
3. **Sounds**: copied into the phone's sounds (Ringtones/Halo,
   Notifications/Halo, Alarms/Halo), then set as defaults. This needs Android
   10 and the "Modify system settings" permission, which Halo asks for when
   you come back from the wallpaper screen. They are original sounds,
   synthesised by `tool/theme_sounds.py` (the same recipes as the mock-up), as
   16-bit WAV.

With no theme chosen for the wallpaper in place, the wallpaper takes the
phone's colours again. The code: `theme/HaloThemes.kt`, `theme/ThemeSounds.kt`,
`lib/src/screens/themes_screen.dart`.

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
