package dev.gigaherz.eyes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier; // Utilisez Identifier (et NON ResourceLocation)

public record InitiateJumpscarePacket(double px, double py, double pz)
        implements CustomPacketPayload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, InitiateJumpscarePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, InitiateJumpscarePacket::px,
            ByteBufCodecs.DOUBLE, InitiateJumpscarePacket::py,
            ByteBufCodecs.DOUBLE, InitiateJumpscarePacket::pz,
            InitiateJumpscarePacket::new
    );

    // En 26.2, la création se fait généralement via Identifier.of() ou via votre méthode locale
    public static final Identifier ID = EyesInTheDarkness.location("server_hello");

    // L'identifiant est encapsulé dans le Type exigé par Mojang
    public static final CustomPacketPayload.Type<InitiateJumpscarePacket> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
