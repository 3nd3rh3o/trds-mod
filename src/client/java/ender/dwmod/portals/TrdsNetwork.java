package ender.dwmod.portals;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class TrdsNetwork {
    public static final String MODID = "dwmod";

    // IDs de payload
    public static final CustomPacketPayload.Type<PortalWorldInitPayload> PORTAL_WORLD_INIT_ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "portal_world_init"));

    public static final CustomPacketPayload.Type<PortalChunkPayload> PORTAL_CHUNK_DATA_ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "portal_chunk_data"));

    public static final CustomPacketPayload.Type<PortalChunkUnloadPayload> PORTAL_CHUNK_UNLOAD_ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "portal_chunk_unload"));

    private TrdsNetwork() {}

    public static void initCommon() {
        // Enregistrer les codecs sur les deux côtés AVANT les handlers
        PayloadTypeRegistry.playS2C().register(PortalWorldInitPayload.ID, PortalWorldInitPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalChunkPayload.ID, PortalChunkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalChunkUnloadPayload.ID, PortalChunkUnloadPayload.CODEC);
    }

    public static void initClient() {
        // Handlers côté client
        ClientPlayNetworking.registerGlobalReceiver(
                PortalWorldInitPayload.ID,
                (payload, context) -> PortalClientHandlers.handleWorldInit(payload, context)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                PortalChunkPayload.ID,
                (payload, context) -> PortalClientHandlers.handlePortalChunk(payload, context)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                PortalChunkUnloadPayload.ID,
                (payload, context) -> PortalClientHandlers.handleChunkUnload(payload, context)
        );
    }
}
