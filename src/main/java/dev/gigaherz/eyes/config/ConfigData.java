package dev.gigaherz.eyes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.gigaherz.eyes.entity.EyesEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NeoForge used {@code ModConfigSpec} (TOML, with comments, ranges, and separate
 * client/server files handled by FML). Fabric has no equivalent built-in config
 * framework, so this is a small hand-rolled replacement: one JSON file in the
 * standard Fabric config directory ({@code config/eyesinthedarkness.json}),
 * loaded once during {@code onInitialize()}.
 * <p>
 * The publicly-read static fields below are unchanged from the NeoForge version,
 * so {@link dev.gigaherz.eyes.entity.EyesEntity}, {@link dev.gigaherz.eyes.EyesSpawningManager}
 * etc. did not need to change how they read config values.
 */
public class ConfigData
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Server / common
    public static boolean jumpscare = true;
    public static int jumpscareHurtLevel = 1;
    public static boolean eyesCanAttackWhileLit = true;
    public static boolean enableEyeAggressionEscalation = true;
    public static boolean eyeAggressionDependsOnLocalDifficulty = true;
    public static boolean eyeAggressionDependsOnLightLevel = true;
    public static double eyeIdleVolume = 1.0;
    public static double eyeDisappearVolume = 1.0;
    public static double eyeJumpscareVolume = 1.0;

    public static boolean enableNaturalSpawn = true;
    public static int maxEyesSpawnDistance = 64;

    public static int spawnCycleIntervalNormal = 150;
    public static int maxEyesAroundPlayerNormal = 2;
    public static int maxTotalEyesPerDimensionNormal = 15;
    public static int spawnCycleIntervalMidnight = 50;
    public static int maxEyesAroundPlayerMidnight = 3;
    public static int maxTotalEyesPerDimensionMidnight = 15;
    public static int spawnCycleIntervalHalloween = 50;
    public static int maxEyesAroundPlayerHalloween = 5;
    public static int maxTotalEyesPerDimensionHalloween = 25;

    public static double speedNoAggro = 0.1;
    public static double speedFullAggro = 0.5;

    public static long longSpawnCycleWarning = 50000L;

    public static List<String> biomeRules = new ArrayList<>();
    public static List<String> dimensionRules = new ArrayList<>();

    // Client
    public static boolean jumpscareClient = true;

    public static void load()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("eyesinthedarkness.json");

        Model model;
        if (Files.exists(path))
        {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
            {
                model = GSON.fromJson(reader, Model.class);
                if (model == null)
                {
                    model = new Model();
                }
            }
            catch (IOException e)
            {
                LOGGER.warn("Could not read {}, using defaults.", path, e);
                model = new Model();
            }
        }
        else
        {
            model = new Model();
        }

        apply(model);

        // Write back out so new/missing fields (and comments-as-keys) get persisted.
        try
        {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
            {
                GSON.toJson(model, writer);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Could not write {}.", path, e);
        }
    }

    private static void apply(Model model)
    {
        jumpscare = model.general.jumpscare;
        jumpscareHurtLevel = model.general.jumpscareHurtLevel;
        eyesCanAttackWhileLit = model.general.eyesCanAttackWhileLit;
        speedNoAggro = model.general.speedNoAggro;
        speedFullAggro = model.general.speedFullAggro;
        longSpawnCycleWarning = model.general.longSpawnCycleWarning;

        enableEyeAggressionEscalation = model.eyeAggression.enableEscalation;
        eyeAggressionDependsOnLocalDifficulty = model.eyeAggression.localDifficulty;
        eyeAggressionDependsOnLightLevel = model.eyeAggression.lightLevel;

        eyeIdleVolume = model.soundVolumes.idleNoiseVolume;
        eyeDisappearVolume = model.soundVolumes.disappearNoiseVolume;
        eyeJumpscareVolume = model.soundVolumes.jumpscareVolume;

        enableNaturalSpawn = model.spawning.enableNaturalSpawn;
        maxEyesSpawnDistance = model.spawning.maxEyesSpawnDistance;
        biomeRules = orDefault(model.spawning.biomeRules, ArrayList::new);
        dimensionRules = orDefault(model.spawning.dimensionRules, ArrayList::new);

        spawnCycleIntervalNormal = model.spawningNormal.spawnCycleInterval;
        maxEyesAroundPlayerNormal = model.spawningNormal.maxEyesAroundPlayer;
        maxTotalEyesPerDimensionNormal = model.spawningNormal.maxTotalEyesPerDimension;

        spawnCycleIntervalMidnight = model.spawningMidnight.spawnCycleInterval;
        maxEyesAroundPlayerMidnight = model.spawningMidnight.maxEyesAroundPlayer;
        maxTotalEyesPerDimensionMidnight = model.spawningMidnight.maxTotalEyesPerDimension;

        spawnCycleIntervalHalloween = model.spawningHalloween.spawnCycleInterval;
        maxEyesAroundPlayerHalloween = model.spawningHalloween.maxEyesAroundPlayer;
        maxTotalEyesPerDimensionHalloween = model.spawningHalloween.maxTotalEyesPerDimension;

        jumpscareClient = model.client.jumpscare;

        BiomeRules.parseRules(biomeRules);
        DimensionRules.parseRules(dimensionRules);
    }

    private static <T> T orDefault(T value, java.util.function.Supplier<T> defaultSupplier)
    {
        return value != null ? value : defaultSupplier.get();
    }

    public static boolean canEyesSpawnAt(EntityType<EyesEntity> entityType, ServerLevelAccessor world, EntitySpawnReason reason, BlockPos pos, RandomSource random)
    {
        return Monster.checkMonsterSpawnRules(entityType, world, reason, pos, random);
    }

    // --- Plain-data model for (de)serialization. Field names double as the JSON keys. ---

    private static class Model
    {
        General general = new General();
        EyeAggression eyeAggression = new EyeAggression();
        SoundVolumes soundVolumes = new SoundVolumes();
        Spawning spawning = new Spawning();
        SpawningTier spawningNormal = new SpawningTier(150, 2, 15);
        SpawningTier spawningMidnight = new SpawningTier(50, 3, 15);
        SpawningTier spawningHalloween = new SpawningTier(50, 5, 25);
        Client client = new Client();
    }

    private static class General
    {
        boolean jumpscare = true;
        int jumpscareHurtLevel = 1;
        boolean eyesCanAttackWhileLit = true;
        double speedNoAggro = 0.1;
        double speedFullAggro = 0.5;
        long longSpawnCycleWarning = 50000L;
    }

    private static class EyeAggression
    {
        boolean enableEscalation = true;
        boolean localDifficulty = true;
        boolean lightLevel = true;
    }

    private static class SoundVolumes
    {
        double idleNoiseVolume = 1.0;
        double disappearNoiseVolume = 1.0;
        double jumpscareVolume = 1.0;
    }

    private static class Spawning
    {
        boolean enableNaturalSpawn = true;
        int maxEyesSpawnDistance = 64;
        List<String> biomeRules = new ArrayList<>();
        List<String> dimensionRules = new ArrayList<>();
    }

    private static class SpawningTier
    {
        int spawnCycleInterval;
        int maxEyesAroundPlayer;
        int maxTotalEyesPerDimension;

        SpawningTier(int spawnCycleInterval, int maxEyesAroundPlayer, int maxTotalEyesPerDimension)
        {
            this.spawnCycleInterval = spawnCycleInterval;
            this.maxEyesAroundPlayer = maxEyesAroundPlayer;
            this.maxTotalEyesPerDimension = maxTotalEyesPerDimension;
        }
    }

    private static class Client
    {
        boolean jumpscare = true;
    }
}
