package ender.dwmod.portals;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

@Environment(EnvType.CLIENT)
public final class PortalClientWorldManager {
    private final Minecraft minecraft;
    private final Map<ResourceKey<Level>, ClientLevel> worlds = new HashMap<>();

    public PortalClientWorldManager(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public ClientLevel get(ResourceKey<Level> key) {
        return worlds.get(key);
    }

    public ClientLevel getOrCreate(ResourceKey<Level> key,
                                   Holder<DimensionType> dimType,
                                   long seed,
                                   int seaLevel) {
        ClientLevel existing = worlds.get(key);
        if (existing != null) return existing;

        Minecraft mc = this.minecraft;
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) throw new IllegalStateException("No connection yet");

        // Réutiliser les infos du monde principal pour ClientLevelData
        ClientLevel main = mc.level;
        ClientLevel.ClientLevelData data;
        if (main != null && main.getLevelData() instanceof ClientLevel.ClientLevelData mainData) {
            data = new ClientLevel.ClientLevelData(
                    mainData.getDifficulty(),
                    mainData.isHardcore(),
                    mainData.voidDarknessOnsetRange() == 1.F
            );
        } else {
            data = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
        }

        int viewDistance = mc.options.getEffectiveRenderDistance();
        int simulationDistance = viewDistance;
        LevelRenderer renderer = mc.levelRenderer;
        boolean debug = false;

        ClientLevel portal = new ClientLevel(
                connection,
                data,
                key,
                dimType,
                viewDistance,
                simulationDistance,
                renderer,
                debug,
                seed,
                seaLevel
        );

        worlds.put(key, portal);
        return portal;
    }

    public void tick() {
        for (ClientLevel level : worlds.values()) {
            level.tick(() -> true);
        }
    }

    public void setMainWorld(ClientLevel level) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMainWorld'");
    }

    public void tickExtraWorlds() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tickExtraWorlds'");
    }
}
