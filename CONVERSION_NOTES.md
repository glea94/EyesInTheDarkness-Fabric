# NeoForge to Fabric Conversion (Minecraft 26.2 / Fabric Loader 0.19.3)

This project is a source-to-source conversion of the NeoForge mod "Eyes in the Darkness"
(already on 26.2) to Fabric Loom / Fabric API, while maintaining the same Minecraft version.
Since 26.2 uses official, de-obfuscated Mojang mappings for both loaders,
the vanilla code (entities, rendering, low-level packets) has remained virtually identical;
what has changed are the integration points with the loader.

## What has changed

- **build.gradle / settings.gradle / gradle.properties**: replaced by their Fabric Loom
equivalents (`net.fabricmc.fabric-loom` plugin, `fabric-loader` / `fabric-api`
dependencies, Gradle 9.5.1, Java 25). The JAR is de-obfuscated, so no more `mappings`
or `remapJar` tasks are needed.
- **fabric.mod.json** replaces `neoforge.mods.toml`. Includes `main` (common) and
`client` (rendering/networking) entrypoints.
- **`EyesInTheDarkness`** (`ModInitializer`): NeoForge `DeferredRegister`s are
replaced by simple `Registry.register(...)` calls, just as Mojang does.
- **`EyesInTheDarknessClient`** (`ClientModInitializer`) replaces `ClientEvents`:
handles registration of the renderer, jumpscare overlay, and client network receiver.
- **Networking**: `RegisterPayloadHandlersEvent`/`IPayloadContext` (NeoForge) →
`PayloadTypeRegistry` + `ServerPlayNetworking.send` / `ClientPlayNetworking.registerGlobalReceiver`.
- **Spawn placement**: direct call to `SpawnPlacements.register(...)`, a public
vanilla method (no longer need `RegisterSpawnPlacementsEvent`).
- **Creative tab**: `BuildCreativeModeTabContentsEvent` → `CreativeModeTabEvents.modifyEntriesEvent(...)`. - **HUD jumpscare** (`JumpscareOverlay`): `RegisterGuiLayersEvent`/`GuiLayer` (NeoForge)
→ `HudElementRegistry`/`HudElement` (Fabric API). Low-level rendering (PoseStack,
`GuiElementRenderState`, `RenderPipelines`, etc.) remained identical as these are
vanilla classes shared by both loaders in version 26.2.
- **`FinalizeSpawnEvent`** (adding anti-Eyes goals to Wolf/Cat/Ocelot): replaced
by a `@Inject` mixin in `Mob#finalizeSpawn` (`MobFinalizeSpawnMixin`), since Fabric API
lacks a generic "any entity just spawned" event.
- **Access to `ServerLevel#customSpawners`** (private): replaced NeoForge's
`accesstransformer.cfg` with an accessor mixin
(`ServerLevelCustomSpawnersAccessor`) + an `accessible`/`mutable` entry in the
`eyesinthedarkness.classtweaker` class tweaker.
- **`MobCategory.EYESINTHEDARKNESS_EYES`** (dedicated spawn category):
NeoForge's `enumextensions.json` → enum extension mixin
(`MobCategoryMixin`) + `extend-enum` directive in the class tweaker.
- **Config**: NeoForge's `ModConfigSpec` (TOML, with built-in comments/ranges) has
no standard Fabric equivalent. Replaced by a small `ConfigData` class that
reads/writes a simple JSON file at `config/eyesinthedarkness.json` on startup. The
exposed public static fields remain identical to the original, so the rest of the code
(`EyesEntity`, `EyesSpawningManager`, etc.) did not need to change. **Comments
and range validation (min/max) from the original TOML were lost** during this
simplified conversion. - **`BiomeRules`**: the final automatic rule based on the `forge:is_void` tag
(specific to NeoForge) has been removed; there is no longer a known equivalent Fabric tag. 
Add your own `!*`/`!#...` rules to the end of the list in the config if needed.

## Things to check / points of uncertainty (on the first build)

I was unable to compile this project (no network access in my environment to
download Minecraft/Fabric Loom/Fabric API), so some API names need to be
verified during compilation, specifically:

1. **`HudElementRegistry.attachElementAfter(VanillaHudElements.CAMERA_OVERLAYS, ...)`**
in `JumpscareOverlay.register()` — the exact name of the constant
`VanillaHudElements.CAMERA_OVERLAYS` could not be 100% confirmed. If it
does not exist under this name, replace it with the corresponding
`VanillaHudElements` constant (using IDE autocompletion) or with `HudElementRegistry.addLast(...)`.
2. **`eyesinthedarkness.classtweaker`**: the exact syntax for the
`accessible`/`mutable` directives (keyword order: type/field) needs to be validated
using the `validateAccessWidener` Gradle task.
3. **`MobCategory` constructor** (`MobCategoryMixin`): the parameter types
come from the original NeoForge descriptor (`String, String, int, boolean,
boolean, int`); parameter names do not matter, but a different order or
type in the actual class would cause the mixin to fail at load time.
4. The `logo.png` file referenced by `fabric.mod.json` must be at the root of the
resources (`src/main/resources/logo.png`) — this is already the case here. ## To build

```
./gradlew build
```

(after verifying that Java 25 is available for Gradle/the toolchain).
