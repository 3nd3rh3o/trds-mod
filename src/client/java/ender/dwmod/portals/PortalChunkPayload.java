package ender.dwmod.portals;

import ender.dwmod.DwMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public record PortalChunkPayload(
    ResourceKey<Level> dimension,
    int chunkX,
    int chunkZ,
    long seed,
    int seaLevel,
    ClientboundLevelChunkPacketData chunkData,
    ClientboundLightUpdatePacketData lightData
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PortalChunkPayload> ID =
            TrdsNetwork.PORTAL_CHUNK_DATA_ID;

    public static final CustomPacketPayload.Type<PortalChunkPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DwMod.MOD_ID, "portal_chunk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PortalChunkPayload> CODEC =
        CustomPacketPayload.codec(PortalChunkPayload::write, PortalChunkPayload::read);

    private static PortalChunkPayload read(RegistryFriendlyByteBuf buf) {
        ResourceKey<Level> dim = buf.readResourceKey(Registries.DIMENSION);
        int x = buf.readVarInt();
        int z = buf.readVarInt();
        long seed = buf.readLong();
        int seaLevel = buf.readInt();

        // On se repose sur les ctors "réseau" vanilla
        ClientboundLevelChunkPacketData chunkData = new ClientboundLevelChunkPacketData(buf, x, z);
        ClientboundLightUpdatePacketData lightData = new ClientboundLightUpdatePacketData(buf, x, z);

        return new PortalChunkPayload(dim, x, z, seed, seaLevel, chunkData, lightData);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(this.dimension);
        buf.writeVarInt(this.chunkX);
        buf.writeVarInt(this.chunkZ);
        buf.writeLong(this.seed);
        buf.writeInt(this.seaLevel);
        
        // Utilise les méthodes vanilla
        this.chunkData.write(buf);
        this.lightData.write(buf);
    }

    @Override
    public Type<PortalChunkPayload> type() {
        return TYPE;
    }

    public static PortalChunkPayload fromChunk(ResourceKey<Level> dim, LevelChunk chunk) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromChunk'");
    }
}
