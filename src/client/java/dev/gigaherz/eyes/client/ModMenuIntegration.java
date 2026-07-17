package dev.gigaherz.eyes.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.gigaherz.eyes.config.ConfigData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ConfigScreen(parentScreen);
    }

    private static class ConfigScreen extends Screen {
        private final Screen parent;

        protected ConfigScreen(Screen parent) {
            super(Component.literal("Eyes in the Darkness"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            // Bouton Jumpscare Toggle
            this.addRenderableWidget(Button.builder(
                            Component.literal("Jumpscare Client: " + (ConfigData.jumpscareClient ? "§aON" : "§cOFF")),
                            button -> {
                                ConfigData.jumpscareClient = !ConfigData.jumpscareClient;
                                button.setMessage(Component.literal("Jumpscare Client: " + (ConfigData.jumpscareClient ? "§aON" : "§cOFF")));
                            })
                    .bounds(this.width / 2 - 100, this.height / 2 - 25, 200, 20).build()
            );

            // Bouton Done
            this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                    .bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20).build()
            );
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            // Rendu de l'arrière-plan de l'écran et du titre natif géré par la super-classe Screen
            super.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void onClose() {
            super.onClose();
        }
    }
}
