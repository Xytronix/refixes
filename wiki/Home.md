[**Support Discord Server**](https://discord.gg/y5kTgtQtgX)

### ⚠️ **Warning:** Refixes requires the server to be launched via [**Hyinit**](https://www.curseforge.com/hytale/bootstrap/hyinit)! See Hyinit's project page for more information.

# ♻️ Refixes

Refixes is a Hytale server mod that backports and maintains community bug fixes and performance patches for current Hytale releases.

This project is derived from patches in Hyfixes / Hyzen Kernel, both of which are unfortunately no longer maintained at this time, and we aim to keep important patches updated to newer Hytale releases.

Refixes ships as a single jar containing both the runtime plugin and the Mixin-based early patches. Hyinit auto-discovers both from the one jar.

# ✅ Installation

To install:

1. Set up [Hyinit](https://www.curseforge.com/hytale/bootstrap/hyinit) to launch your server
2. Place `refixes-X.X.X.jar` inside the `earlyplugins` folder

Config lives in `mods/IroriPowered_Refixes/`, split across two files: runtime settings in `config.json` (see [Configuration](Configuration)) and Mixin patch toggles in `Refixes.json` (see [Mixins](Mixins)).
