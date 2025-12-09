package ender.dwmod.portals;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record PortalWorldInitPayload(
        ResourceKey<Level> dimension,
        ResourceKey<DimensionType> dimensionType,
        long seed,
        int seaLevel
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PortalWorldInitPayload> ID =
            TrdsNetwork.PORTAL_WORLD_INIT_ID;

    public static final StreamCodec<FriendlyByteBuf, PortalWorldInitPayload> CODEC =
            StreamCodec.of(PortalWorldInitPayload::write, PortalWorldInitPayload::read);

    public static PortalWorldInitPayload read(FriendlyByteBuf buf) {
        ResourceKey<Level> dim = ResourceKey.create(
                Registries.DIMENSION,
                buf.readResourceLocation()
        );
        ResourceKey<DimensionType> dimType = ResourceKey.create(
                Registries.DIMENSION_TYPE,
                buf.readResourceLocation()
        );
        long seed = buf.readLong();
        int seaLevel = buf.readVarInt();
        return new PortalWorldInitPayload(dim, dimType, seed, seaLevel);
    }

    public static void write(FriendlyByteBuf buf, PortalWorldInitPayload payload) {
        buf.writeResourceLocation(payload.dimension.location());
        buf.writeResourceLocation(payload.dimensionType.location());
        buf.writeLong(payload.seed);
        buf.writeVarInt(payload.seaLevel);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

