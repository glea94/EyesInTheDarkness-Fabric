# Eyes in the Darkness (Fabric)

Those eyes you think you saw when walking through the forest at night. No, you did
not imagine them.

This is a Fabric port of [gigaherz's "Eyes in the Darkness"](https://github.com/gigaherz/EyesInTheDarkness)
mod, targeting **Minecraft 26.2** and **Fabric Loader 0.19.3+**.

## What it does

A pair of glowing eyes occasionally appears watching you from the darkness at
night. They vanish if you get too close or look away for too long — but not
always. Sometimes they don't vanish, and jumpscare you instead.

- Natural spawning at night, scaled by:
  - time of day (gets more frequent as midnight approaches)
  - time of year (gets more frequent as Halloween approaches)
- Configurable spawn caps per dimension and around each player
- Configurable biome/dimension allow/deny rules
- Wolves will hunt eyes down; cats and ocelots will flee from them
- A screen-flash jumpscare with sound, with a configurable "hurt" intensity
- Client and server side config

## Requirements

- Minecraft 26.2
- [Fabric Loader](https://fabricmc.net/use/) 0.19.3 or later
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and drop it
   in your `mods` folder.
3. Download the latest `EyesInTheDarkness-26.2-*.jar` from the
   [Releases](../../releases) page and drop it in your `mods` folder too.
4. This mod must be installed on both the client and the server (or on the client
   only, for singleplayer).

## Configuration

On first launch, a config file is generated at `config/eyesinthedarkness.json`.
It lets you tweak, among other things:

- whether natural spawning and/or the jumpscare are enabled
- spawn cycle interval, max eyes per dimension, and max eyes around a player,
  each with separate values for "normal", "near midnight" and "near Halloween"
- sound volumes
- biome and dimension allow/deny rules (see the comments generated in the file)
- Add a config option on/off in ModMenu to toggle jumpscares.
## Building from source

```
git clone <this repo>
cd EyesInTheDarkness
./gradlew build
```

The compiled mod jar will be in `build/libs/`.

## Credits

- Original mod concept and NeoForge implementation: [gigaherz](https://github.com/gigaherz)
- Fabric port: converted from the NeoForge codebase for Minecraft 26.2 / Fabric
  Loader 0.19.3

## License

BSD-3-Clause — see [LICENSE.txt](LICENSE.txt).
