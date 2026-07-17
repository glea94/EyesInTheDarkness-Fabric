# Conversion NeoForge -> Fabric (Minecraft 26.2 / Fabric Loader 0.19.3)

Ce projet est une conversion source-à-source du mod NeoForge "Eyes in the Darkness"
(déjà en 26.2) vers Fabric Loom / Fabric API, en gardant la même version de Minecraft.
Comme 26.2 utilise les mappings officiels Mojang non-obfusqués pour les deux loaders,
le code vanilla (entité, rendu, packets bas niveau) est resté quasiment identique ;
ce qui a changé, ce sont les points d'intégration avec le loader.

## Ce qui a changé

- **build.gradle / settings.gradle / gradle.properties** : remplacés par l'équivalent
  Fabric Loom (plugin `net.fabricmc.fabric-loom`, dépendances `fabric-loader` /
  `fabric-api`, Gradle 9.5.1, Java 25). Le jar est unobfusqué donc plus de `mappings`
  ni de `remapJar`.
- **fabric.mod.json** remplace `neoforge.mods.toml`. Entrypoints `main` (commun) et
  `client` (rendu/réseau).
- **`EyesInTheDarkness`** (`ModInitializer`) : les `DeferredRegister` NeoForge sont
  remplacés par de simples `Registry.register(...)`, comme le fait Mojang lui-même.
- **`EyesInTheDarknessClient`** (`ClientModInitializer`) remplace `ClientEvents` :
  enregistrement du renderer, de l'overlay jumpscare et du receiver réseau client.
- **Réseau** : `RegisterPayloadHandlersEvent`/`IPayloadContext` (NeoForge) →
  `PayloadTypeRegistry` + `ServerPlayNetworking.send` / `ClientPlayNetworking.registerGlobalReceiver`.
- **Spawn placement** : appel direct à `SpawnPlacements.register(...)`, une méthode
  vanilla publique (plus besoin de `RegisterSpawnPlacementsEvent`).
- **Creative tab** : `BuildCreativeModeTabContentsEvent` → `CreativeModeTabEvents.modifyEntriesEvent(...)`.
- **HUD jumpscare** (`JumpscareOverlay`) : `RegisterGuiLayersEvent`/`GuiLayer` (NeoForge)
  → `HudElementRegistry`/`HudElement` (Fabric API). Le rendu bas niveau (PoseStack,
  `GuiElementRenderState`, `RenderPipelines`, etc.) est resté identique car ce sont des
  classes vanilla partagées par les deux loaders en 26.2.
- **`FinalizeSpawnEvent`** (ajout de goals anti-Eyes sur Loup/Chat/Ocelot) : remplacé
  par un mixin `@Inject` dans `Mob#finalizeSpawn` (`MobFinalizeSpawnMixin`), Fabric API
  n'ayant pas d'événement générique "n'importe quelle entité vient de spawn".
- **Accès à `ServerLevel#customSpawners`** (private) : remplacé le
  `accesstransformer.cfg` de NeoForge par un mixin *accessor*
  (`ServerLevelCustomSpawnersAccessor`) + une entrée `accessible`/`mutable` dans le
  class tweaker `eyesinthedarkness.classtweaker`.
- **`MobCategory.EYESINTHEDARKNESS_EYES`** (catégorie de spawn dédiée) :
  `enumextensions.json` de NeoForge → mixin d'extension d'énum
  (`MobCategoryMixin`) + directive `extend-enum` dans le class tweaker.
- **Config** : NeoForge `ModConfigSpec` (TOML, avec commentaires/ranges intégrés) n'a
  pas d'équivalent Fabric standard. Remplacé par une petite classe `ConfigData` qui
  lit/écrit un JSON simple dans `config/eyesinthedarkness.json` au démarrage. Les
  champs statiques publics exposés sont identiques à avant, donc le reste du code
  (`EyesEntity`, `EyesSpawningManager`, ...) n'a pas eu à changer. **Les commentaires
  et la validation de plage (min/max) du TOML d'origine ont été perdus** dans cette
  conversion simplifiée.
- **`BiomeRules`** : la règle automatique finale basée sur le tag `forge:is_void`
  (spécifique à NeoForge) a été retirée ; il n'y a plus de tag Fabric équivalent connu.
  Ajoutez vos propres règles `!*`/`!#...` en fin de liste dans la config si besoin.

## À vérifier / points d'incertitude (au premier build)

Je n'ai pas pu compiler ce projet (pas d'accès réseau dans mon environnement pour
télécharger Minecraft/Fabric Loom/Fabric API), donc certains noms d'API doivent être
vérifiés à la compilation, en particulier :

1. **`HudElementRegistry.attachElementAfter(VanillaHudElements.CAMERA_OVERLAYS, ...)`**
   dans `JumpscareOverlay.register()` — le nom exact de la constante
   `VanillaHudElements.CAMERA_OVERLAYS` n'a pas pu être confirmé à 100 %. Si elle
   n'existe pas sous ce nom, remplacez par la constante `VanillaHudElements`
   correspondante (autocomplétion IDE) ou par `HudElementRegistry.addLast(...)`.
2. **`eyesinthedarkness.classtweaker`** : la syntaxe exacte des directives
   `accessible`/`mutable` (ordre des mots-clés type/field) est à valider avec la tâche
   Gradle `validateAccessWidener`.
3. **Constructeur de `MobCategory`** (`MobCategoryMixin`) : les types de paramètres
   proviennent du descripteur NeoForge d'origine (`String, String, int, boolean,
   boolean, int`) ; les noms des paramètres n'ont pas d'importance mais un ordre ou
   type différent dans la vraie classe ferait échouer le mixin au chargement.
4. Le fichier `logo.png` référencé par `fabric.mod.json` doit être à la racine des
   resources (`src/main/resources/logo.png`) — c'est déjà le cas ici.

## Pour builder

```
./gradlew build
```

(après avoir vérifié que Java 25 est disponible pour Gradle/le toolchain).
