package dev.gigaherz.eyes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Fabric equivalent of the NeoForge access-transformer entry:
 * {@code public-f net.minecraft.server.level.ServerLevel customSpawners}
 * <p>
 * Lets {@link dev.gigaherz.eyes.EyesSpawningManager} register itself into a
 * level's custom spawner list, the same way it did on NeoForge.
 */
@Mixin(ServerLevel.class)
public interface ServerLevelCustomSpawnersAccessor
{
    @Accessor("customSpawners")
    List<CustomSpawner> eyesinthedarkness$getCustomSpawners();

    @Accessor("customSpawners")
    void eyesinthedarkness$setCustomSpawners(List<CustomSpawner> customSpawners);
}
