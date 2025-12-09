package ender.dwmod.portals;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record PortalChunkUnloadPayload(
        ResourceKey<Level> dimension,
        ChunkPos pos
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PortalChunkUnloadPayload> ID =
            TrdsNetwork.PORTAL_CHUNK_UNLOAD_ID;

    public static final StreamCodec<FriendlyByteBuf, PortalChunkUnloadPayload> CODEC =
            StreamCodec.of(PortalChunkUnloadPayload::write, PortalChunkUnloadPayload::read);

    public static PortalChunkUnloadPayload read(FriendlyByteBuf buf) {
        ResourceKey<Level> dim = ResourceKey.create(
                Registries.DIMENSION,
                buf.readResourceLocation()
        );
        ChunkPos pos = new ChunkPos(buf.readInt(), buf.readInt());
        return new PortalChunkUnloadPayload(dim, pos);
    }

    public static void write(FriendlyByteBuf buf, PortalChunkUnloadPayload payload) {
        buf.writeResourceLocation(payload.dimension.location());
        buf.writeInt(payload.pos.x);
        buf.writeInt(payload.pos.z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
