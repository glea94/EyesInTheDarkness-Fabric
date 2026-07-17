package dev.gigaherz.eyes.mixin;

import dev.gigaherz.eyes.entity.EyesEntity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.ai.goal.GoalSelector; // Ajout de l'import requis

/**
 * On NeoForge this was handled by subscribing to {@code FinalizeSpawnEvent} on the
 * global event bus. Fabric API has no equivalent "any mob finished spawning" event,
 * so we mixin directly into {@link Mob#finalizeSpawn} instead: wolves get a goal to
 * hunt the eyes, and cats/ocelots get a goal to flee from them.
 */
@Mixin(Mob.class)
public abstract class MobFinalizeSpawnMixin
{
    // L'utilisation de @Shadow permet d'exposer proprement les champs protected de Mob à notre Mixin
    @Shadow protected GoalSelector goalSelector;
    @Shadow protected GoalSelector targetSelector;

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void eyesinthedarkness$addAvoidanceGoals(
            ServerLevelAccessor level,
            DifficultyInstance difficultyInstance,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir)
    {
        Mob self = (Mob) (Object) this;

        // On utilise les sélecteurs via @Shadow pour contourner l'accès 'protected' bloquant sur les instances locales
        if (self instanceof Wolf wolf)
        {
            this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(wolf, EyesEntity.class, false));
        }
        if (self instanceof Ocelot ocelot)
        {
            this.goalSelector.addGoal(3, new AvoidEntityGoal<>(ocelot, EyesEntity.class, 6.0F, 1.0D, 1.2D));
        }
        if (self instanceof Cat cat)
        {
            this.goalSelector.addGoal(3, new AvoidEntityGoal<>(cat, EyesEntity.class, 6.0F, 1.0D, 1.2D));
        }
    }
}
