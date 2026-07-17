package dev.gigaherz.eyes.client;

import dev.gigaherz.eyes.EyesInTheDarkness;
import dev.gigaherz.eyes.InitiateJumpscarePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EyesInTheDarknessClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        EntityRenderers.register(EyesInTheDarkness.EYES, EyesRenderer::new);

        JumpscareOverlay.register();

        ClientPlayNetworking.registerGlobalReceiver(InitiateJumpscarePacket.TYPE, (payload, context) ->
                context.client().execute(() -> ClientMessageHandler.handleInitiateJumpscare(payload)));
    }
}
