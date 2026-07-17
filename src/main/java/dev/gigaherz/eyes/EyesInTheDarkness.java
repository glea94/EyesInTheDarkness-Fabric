package dev.gigaherz.eyes;

import dev.gigaherz.eyes.config.ConfigData;
import dev.gigaherz.eyes.entity.EyesEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;

import static net.minecraft.world.entity.SpawnPlacements.*;

public class EyesInTheDarkness implements ModInitializer
{
    // Needed to keep a dedicated spawn cap for the eyes. See MobCategoryMixin / eyesinthedarkness.classtweaker.
    public static final MobCategory CLASSIFICATION = MobCategory.valueOf("EYESINTHEDARKNESS_EYES");

    public static final String MODID = "eyesinthedarkness";

    public static final SoundEvent EYES_LAUGH =
            Registry.register(BuiltInRegistries.SOUND_EVENT, location("eyes_laugh"), SoundEvent.createVariableRangeEvent(location("mob.eyes.laugh")));
    public static final SoundEvent EYES_DISAPPEAR =
            Registry.register(BuiltInRegistries.SOUND_EVENT, location("eyes_disappear"), SoundEvent.createVariableRangeEvent(location("mob.eyes.disappear")));
    public static final SoundEvent EYES_JUMPSCARE =
            Registry.register(BuiltInRegistries.SOUND_EVENT, location("eyes_jumpscare"), SoundEvent.createVariableRangeEvent(location("mob.eyes.jumpscare")));

    public static final EntityType<EyesEntity> EYES = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            location("eyes"),
            EntityType.Builder.of(EyesEntity::new, CLASSIFICATION)
                    .clientTrackingRange(5)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, location("eyes"))));

    public static final SpawnEggItem EYES_EGG = registerSpawnEgg();

    private static SpawnEggItem registerSpawnEgg()
    {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, location("eyes_spawn_egg"));
        SpawnEggItem item = new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(EYES));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    @Override
    public void onInitialize()
    {
        ConfigData.load();

        FabricDefaultAttributeRegistry.register(EYES, EyesEntity.prepareAttributes());

        SpawnPlacements.register(
                EYES,
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ConfigData::canEyesSpawnAt
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> entries.accept(EYES_EGG));

        // Version officielle pour Fabric 26.2 (Minecraft 1.21.4+)
        PayloadTypeRegistry.clientboundPlay().register(InitiateJumpscarePacket.TYPE, InitiateJumpscarePacket.STREAM_CODEC);

        EyesSpawningManager.register();
    }

    public static Identifier location(String location)
    {
        return Identifier.fromNamespaceAndPath(MODID, location);
    }
}
