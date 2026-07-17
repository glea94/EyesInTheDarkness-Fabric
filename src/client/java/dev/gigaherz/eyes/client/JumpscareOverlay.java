package dev.gigaherz.eyes.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import dev.gigaherz.eyes.EyesInTheDarkness;
import dev.gigaherz.eyes.config.ConfigData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import org.joml.Matrix3x2f;

import org.jetbrains.annotations.Nullable;

public class JumpscareOverlay implements HudElement
{
    private static final Identifier TEXTURE_EYES = EyesInTheDarkness.location("textures/entity/eyes2.png");
    private static final Identifier TEXTURE_FLASH = EyesInTheDarkness.location("textures/creepy.png");

    public static JumpscareOverlay INSTANCE = new JumpscareOverlay();

    private static final Rect2i[] FRAMES = {
            new Rect2i(0, 0, 13, 6),
            new Rect2i(0, 7, 13, 6),
            new Rect2i(0, 14, 13, 6),
            new Rect2i(0, 21, 13, 6),
            new Rect2i(15, 1, 15, 8),
            new Rect2i(15, 16, 15, 12),
    };
    private static final int ANIMATION_APPEAR = 10;
    private static final int ANIMATION_LINGER = 90;
    private static final int ANIMATION_BLINK = 60;
    private static final int ANIMATION_SCARE1 = 20;
    private static final int ANIMATION_FADE = 20;
    private static final int ANIMATION_BLINK_START = ANIMATION_APPEAR + ANIMATION_LINGER;
    private static final int ANIMATION_SCARE_START = ANIMATION_BLINK_START + ANIMATION_BLINK;
    private static final int ANIMATION_FADE_START = ANIMATION_SCARE_START + ANIMATION_SCARE1;
    private static final int ANIMATION_TOTAL = ANIMATION_APPEAR + ANIMATION_LINGER + ANIMATION_BLINK
            + ANIMATION_SCARE1 + ANIMATION_FADE;

    public static void register()
    {
        // Remplacement par VanillaHudElements.CHAT (Présent de manière stable dans l'API) pour ancrer notre overlay
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, EyesInTheDarkness.location("jumpscare"), INSTANCE);
        ClientTickEvents.START_CLIENT_TICK.register(client -> INSTANCE.clientTick());
    }

    private boolean visible = false;
    private float progress = 0;

    private JumpscareOverlay()
    {
    }

    public void show(double ex, double ey, double ez)
    {
        if (ConfigData.jumpscareClient)
        {
            visible = true;
            Minecraft.getInstance().level.playLocalSound(ex, ey, ez, EyesInTheDarkness.EYES_JUMPSCARE, SoundSource.HOSTILE, getJumpscareVolume(), 1, false);
        }
    }

    protected float getJumpscareVolume()
    {
        return (float)ConfigData.eyeIdleVolume;
    }

    public void clientTick()
    {
        if (visible)
        {
            progress++;
            if (progress >= ANIMATION_TOTAL)
            {
                visible = false;
                progress = 0;
            }
        }
    }

    // Correction 1 : Remplacement obligatoire de @Override render par extractRenderState pour l'interface HudElement
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker partialTicks)
    {
        if (!visible) return;

        var mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float time = progress + partialTicks.getGameTimeDeltaPartialTick(true);
        if (time >= ANIMATION_TOTAL)
        {
            visible = false;
            progress = 0;
            return;
        }

        // Correction 2 : Gestion de la matrice via l'état de l'allocateur au lieu de push/pop manquants
        Matrix3x2f originalPose = new Matrix3x2f(graphics.pose());

        float darkening = Mth.clamp(
                Math.min(
                        time / ANIMATION_APPEAR,
                        (ANIMATION_TOTAL - time) / ANIMATION_FADE
                ), 0, 1
        );

        boolean showCreep = false;
        int blinkstate = 0;
        if (time >= ANIMATION_BLINK_START)
        {
            if (time >= ANIMATION_SCARE_START)
            {
                blinkstate = 1;
                showCreep = (time - ANIMATION_SCARE_START) > ANIMATION_SCARE1;
            }
            else
            {
                float fade = Math.max(0, (time - ANIMATION_BLINK_START) / ANIMATION_BLINK);
                float blinkspeed = (float) (1 + Math.pow(fade, 3));
                blinkstate = Mth.floor(20 * blinkspeed) & 1;
                showCreep = blinkstate == 1;
            }
        }

        int alpha = Mth.floor(darkening * 255);

        if (alpha > 0)
        {
            graphics.fill(0, 0, screenWidth, screenHeight, alpha << 24);
            if (showCreep)
            {
                int texW = 2048;
                int texH = 1024;

                float scale1 = screenHeight / (float) texH;
                int drawY = 0;
                int drawH = screenHeight;
                int drawW = Mth.floor(texW * scale1);
                int drawX = (screenWidth - drawW) / 2;

                // Correction 3 : Forçage du cast (int) pour drawX et drawY afin de supprimer les erreurs de perte de précision (lossy conversion)
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_FLASH, (int)drawX, (int)drawY, 0, 0, drawW, drawH, texW, texH, (alpha << 24) | 0xFFFFFF);
            }
        }

        if (blinkstate != 1)
        {
            float scale = Float.MAX_VALUE;
            for (Rect2i r : FRAMES)
            {
                float s = Math.min(
                        Mth.floor(screenWidth * 0.8 / (float) r.getWidth()),
                        Mth.floor(screenHeight * 0.8 / (float) r.getHeight()));
                scale = Math.min(scale, s);
            }

            scale = Math.min(1, (1 + time) / (1 + ANIMATION_APPEAR)) * scale;

            int currentFrame = Math.min(FRAMES.length - 1, Mth.floor(FRAMES.length * time / ANIMATION_APPEAR));

            Rect2i rect = FRAMES[currentFrame];
            int tx = rect.getX();
            int ty = rect.getY();
            int tw = rect.getWidth();
            int th = rect.getHeight();

            int drawW = Mth.floor(tw * scale);
            int drawH = Mth.floor(th * scale);
            int drawX = Mth.floor((screenWidth - drawW) / 2.0f);
            int drawY = Mth.floor((screenHeight - drawH) / 2.0f);

            int texW = 32;
            int texH = 32;

            // Correction 4 : drawX et drawY passés en int stricts
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_EYES, drawX, drawY, tx, ty, drawW, drawH, texW, texH, 0xFFFFFFFF);
        }

        // Restauration de la matrice d'origine
        graphics.pose().set(originalPose);
    }
}
