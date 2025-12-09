package ender.dwmod.portals;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ender.dwmod.DwModClient;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class PortalClientHandlers {
    private static PortalClientWorldManager manager() {
        return DwModClient.WORLD_MANAGER; // singleton à toi
    }

    public static void handleWorldInit(PortalWorldInitPayload payload, ClientPlayNetworking.Context ctx) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            ClientPacketListener conn = mc.getConnection();
            if (conn == null) return;

            RegistryAccess.Frozen registries = conn.registryAccess();
            Registry<DimensionType> dimReg = registries.lookupOrThrow(Registries.DIMENSION_TYPE);
            Holder<DimensionType> dimType = dimReg.getOrThrow(payload.dimensionType());

            manager().getOrCreate(payload.dimension(), dimType, payload.seed(), payload.seaLevel());
        });
    }

    public static void handlePortalChunk(PortalChunkPayload payload, ClientPlayNetworking.Context context) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.execute(() -> {
            // 1. Récupérer / créer le ClientLevel "virtuel"
            ClientLevel portalLevel = DwModClient.WORLD_MANAGER.getOrCreate(
                payload.dimension(),
                null,
                payload.seed(),
                payload.seaLevel()
            );

            int x = payload.chunkX();
            int z = payload.chunkZ();

            ClientboundLevelChunkPacketData chunkData = payload.chunkData();

            // 2. Injection propre dans le ClientChunkCache
            ClientChunkCache chunkCache = portalLevel.getChunkSource();

            FriendlyByteBuf readBuf = chunkData.getReadBuffer();
            Map<Heightmap.Types, long[]> heightmaps = chunkData.getHeightmaps();
            Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> beConsumer =
                chunkData.getBlockEntitiesTagsConsumer(x, z);

            LevelChunk chunk = chunkCache.replaceWithPacketData(
                x,
                z,
                readBuf,
                heightmaps,
                beConsumer
            );

            // 3. Lumière : recopier ce que fait ClientPacketListener.queueLightUpdate(...)
            ClientboundLightUpdatePacketData lightData = payload.lightData();

            // Le pattern vanilla (1.19+) c’est : queue un runnable sur le level,
            // qui pousse les DataLayer dans le LevelLightEngine.
            portalLevel.queueLightUpdate(() -> {
                LevelLightEngine lightEngine = portalLevel.getLightEngine();
                // Ici tu recopies la logique exacte de queueLightUpdate(...) des sources vanilla
                // (pour 1.21.x, c’est toujours la même idée : pour chaque section,
                //  appeler lightEngine.queueSectionData(...)).
            });

            // Éventuellement : marquer le chunk comme "ready" pour ton renderer de portail.
        });
    }
    
    public static void handleChunkUnload(PortalChunkUnloadPayload payload, ClientPlayNetworking.Context ctx) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            ClientLevel level = manager().get(payload.dimension());
            if (level == null) return;
            ClientChunkCache chunkSource = level.getChunkSource();
            chunkSource.drop(payload.pos());
        });
    }
}
