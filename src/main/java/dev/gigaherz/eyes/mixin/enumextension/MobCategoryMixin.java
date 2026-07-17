package dev.gigaherz.eyes.mixin.enumextension;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds the dedicated "EYESINTHEDARKNESS_EYES" mob spawn category, so eyes entities
 * get their own spawn cap instead of sharing the vanilla MONSTER cap.
 * <p>
 * Equivalent of the NeoForge {@code META-INF/enumextensions.json} entry. See also
 * the {@code extend-enum} line in {@code eyesinthedarkness.classtweaker}, which makes
 * this new constant visible in the decompiled source.
 */
@Mixin(MobCategory.class)
enum MobCategoryMixin
{
    EYESINTHEDARKNESS_EYES("eyesinthedarkness:eyes", "EY", 15, false, false, 64);

    @Shadow
    MobCategoryMixin(String id, String name, int maxInstancesPerChunk, boolean isFriendly, boolean isPersistent, int noDespawnDistance)
    {
    }
}
