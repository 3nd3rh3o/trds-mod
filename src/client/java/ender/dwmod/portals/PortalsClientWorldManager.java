package ender.dwmod.portals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

// Manage over-loaded chunk that are NOT the current clientworld.
@Environment(EnvType.CLIENT)
public final class PortalsClientWorldManager {
    private final Minecraft client;

    private ClientLevel mainLevel;
    private final Map<ResourceKey<Level>, ClientLevel> extraWorlds = new HashMap<>();

    public PortalsClientWorldManager(Minecraft client)
    {
        this.client = client;
    }
    
    public void setMainWorld(ClientLevel level)
    {
        mainLevel = level;
        extraWorlds.clear();
    }

    public ClientLevel getMainWorld()
    {
        return mainLevel;
    }

    public ClientLevel getOrCreatePortalWorld(
        ResourceKey<Level> dimensionKey,
        Holder<DimensionType> dimensionType,
        long seed,
        int seaLevel
    ) {
        ClientLevel existing = extraWorlds.get(dimensionKey);
        if (existing != null) return existing;

        Minecraft mc = client;
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            throw new IllegalStateException("No connection yet");
        }

        // 1) Data du monde client
        // Regarde comment ClientLevel.ClientLevelData est instancié dans ClientPacketListener/handleLogin.
        // Ici, le plus simple : cloner la data du monde principal.
        ClientLevel main = mc.level;
        ClientLevel.ClientLevelData data;
        if (main != null && main.getLevelData() instanceof ClientLevel.ClientLevelData mainData) {
            // petite "copie" – tu peux aussi réutiliser directement mainData si tu t'en fiches
            data = new ClientLevel.ClientLevelData(
                mainData.getDifficulty(),    // ou champs équivalents
                mainData.isHardcore(),
                mainData.voidDarknessOnsetRange() == 1.0F // UGLY BUT WORKS!!!
            );
        } else {
            // fallback minimal si on n'a pas encore de monde principal
            data = new ClientLevel.ClientLevelData(
                Difficulty.NORMAL,
                false,  // hardcore
                false   // flat
            );
        }

        int viewDistance = mc.options.getEffectiveRenderDistance();
        // Tu peux récupérer la simulation distance sur le client si tu la stockes, ou juste mettre viewDistance
        int simulationDistance = viewDistance;

        LevelRenderer levelRenderer = mc.levelRenderer;
        boolean isDebug = false; // à moins que ce soit un monde debug

        ClientLevel portalWorld = new ClientLevel(
            connection,
            data,
            dimensionKey,
            dimensionType,
            viewDistance,
            simulationDistance,
            levelRenderer,
            isDebug,
            seed,
            seaLevel
        );

        extraWorlds.put(dimensionKey, portalWorld);
        return portalWorld;
    }


    public ClientLevel getExtraWorld(ResourceKey<Level> dim)
    {
        return extraWorlds.get(dim);
    }

    public void unloadExtraWorld(ResourceKey<Level> dim)
    {
        ClientLevel level = extraWorlds.remove(dim);
        if (level != null)
        {
            // optional : cleanup
        }
    }

    public Collection<ClientLevel> getAllExtraWorlds()
    {
        return extraWorlds.values();
    }

    public void tickExtraWorlds()
    {
        for (ClientLevel l : extraWorlds.values())
        {
            l.tick(() -> true);
        }
    }
}
