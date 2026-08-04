package dev.gigaherz.eyes.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.gigaherz.eyes.config.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> {
            // Création du menu Cloth Config sécurisé pour l'API moderne
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Eyes in the Darkness - Configuration"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // === CATÉGORIE : GÉNÉRAL ===
            ConfigCategory general = builder.getOrCreateCategory(Component.literal("Général"));

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Jumpscare (Serveur)"), ConfigData.jumpscare)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> ConfigData.jumpscare = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Jumpscare (Client)"), ConfigData.jumpscareClient)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> ConfigData.jumpscareClient = val)
                    .build());

            general.addEntry(entryBuilder.startIntField(Component.literal("Dégâts Jumpscare"), ConfigData.jumpscareHurtLevel)
                    .setDefaultValue(1)
                    .setSaveConsumer(val -> ConfigData.jumpscareHurtLevel = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Attaque éclairée"), ConfigData.eyesCanAttackWhileLit)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> ConfigData.eyesCanAttackWhileLit = val)
                    .build());

            // === CATÉGORIE : MÉCANIQUES & SONS ===
            ConfigCategory mechanics = builder.getOrCreateCategory(Component.literal("Mécaniques & Sons"));

            mechanics.addEntry(entryBuilder.startDoubleField(Component.literal("Vitesse sans Aggro"), ConfigData.speedNoAggro)
                    .setDefaultValue(0.1)
                    .setSaveConsumer(val -> ConfigData.speedNoAggro = val)
                    .build());

            mechanics.addEntry(entryBuilder.startDoubleField(Component.literal("Vitesse Aggro Max"), ConfigData.speedFullAggro)
                    .setDefaultValue(0.5)
                    .setSaveConsumer(val -> ConfigData.speedFullAggro = val)
                    .build());

            // Contrôle du volume sous forme de curseur (Slider) de 0 à 100%
            mechanics.addEntry(entryBuilder.startIntSlider(Component.literal("Volume Ambiant (%)"), (int) (ConfigData.eyeIdleVolume * 100), 0, 100)
                    .setDefaultValue(100)
                    .setSaveConsumer(val -> ConfigData.eyeIdleVolume = val / 100.0)
                    .build());

            // === CATÉGORIE : APPARITIONS ===
            ConfigCategory spawning = builder.getOrCreateCategory(Component.literal("Apparitions"));

            spawning.addEntry(entryBuilder.startBooleanToggle(Component.literal("Spawn Naturel"), ConfigData.enableNaturalSpawn)
                    .setDefaultValue(true)
                    .setSaveConsumer(val -> ConfigData.enableNaturalSpawn = val)
                    .build());

            spawning.addEntry(entryBuilder.startIntField(Component.literal("Distance Max Spawn"), ConfigData.maxEyesSpawnDistance)
                    .setDefaultValue(64)
                    .setSaveConsumer(val -> ConfigData.maxEyesSpawnDistance = val)
                    .build());

            // Clic sur "Done" : Écrit physiquement les modifications dans le fichier JSON du disque dur
            builder.setSavingRunnable(ConfigData::save);

            return builder.build();
        };
    }
}
