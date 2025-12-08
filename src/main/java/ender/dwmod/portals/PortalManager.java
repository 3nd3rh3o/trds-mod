package ender.dwmod.portals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3d;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// Container for portals (ServerSide)
// Manage chunk over-loading.
public final class PortalManager {
    private final MinecraftServer server;
    private final Map<UUID, Portal> portals = new HashMap<>();

    private final Map<ResourceKey<Level>, Object2IntMap<ChunkPos>> forcedCounts = new HashMap<>();

    public PortalManager(MinecraftServer server)
    {
        this.server = server;
    }

    public Portal createPortal(ResourceKey<Level> sourceDim, BlockPos sourcePos, ResourceKey<Level> targetDim, BlockPos targetPos, Quaternionf rotation)
    {
        Portal portal = new Portal(sourceDim, sourcePos, targetDim, targetPos, rotation, 1.f);
        portals.put(portal.id, portal);
        return portal;
    }

    public void removePortal(UUID id)
    {
        portals.remove(id);
    }

    public Optional<Portal> getPortal(UUID id)
    {
        return Optional.ofNullable(portals.getOrDefault(id, null));
    }

    public Collection<Portal> getPortalsInDimension(ResourceKey<Level> dim)
    {
        Collection<Portal> res = new ArrayList<>();
        for (Portal p : portals.values())
        {
            if (p.sourceDim == dim)
                res.add(p);
        }
        return res;
    }


    //TODO - in range + in frustum?
    public List<Portal> getVisiblePortals(ServerPlayer player)
    {
        ResourceKey<Level> dim = player.level().dimension();
        Vec3 eyePos = player.getCamera().getEyePosition(1.0f);
        // range
        double maxDistSQ = 64.0 * 64.0;
        List<Portal> candidates = getPortalsInDimension(dim)
            .stream()
                .filter(p -> p.active)
                .filter(p ->  p.sourcePos.distToCenterSqr(eyePos) <= maxDistSQ)
            .toList();


        // simplified frustum filter
        Vec3 look = player.getViewVector(1.0f);

        List<Portal> visible = new ArrayList<>();
        for (Portal p : candidates)
        {
            Vec3 toPortal = Vec3.atCenterOf(p.sourcePos).subtract(eyePos).normalize();
            double dot = look.dot(toPortal);
            if (dot > 0.1)
                visible.add(p);
        }
        return visible;
    }

    // TODO
    public void setPortalActive(UUID id)
    {

    }

    // tick integration.
    public void tick()
    {

    }


    private void addForcedChunksForPortal(Portal portal)
    {
        ServerLevel level = server.getLevel(portal.targetDim);
        if (level == null)
            return;
        ChunkPos center = new ChunkPos(portal.targetPos);
        Object2IntMap<ChunkPos> map = forcedCounts.computeIfAbsent(portal.targetDim, dim -> new Object2IntOpenHashMap<>());

        for (int dx = -portal.chunkRadius; dx <= portal.chunkRadius; dx++)
        {
            for (int dz = -portal.chunkRadius; dz <= portal.chunkRadius; dz++)
            {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                int count = map.getInt(pos);
                if (count == 0)
                {
                    level.getChunkSource().updateChunkForced(pos, true);
                } else {
                    map.put(pos, count + 1);
                }
            }
        }
    }

    private void removeForcedChunksForPortals(Portal portal)
    {
        ServerLevel level = server.getLevel(portal.targetDim);
        if (level == null)
            return;
        ChunkPos center = new ChunkPos(portal.targetPos);
        Object2IntMap<ChunkPos> map = forcedCounts.computeIfAbsent(portal.targetDim, dim -> new Object2IntOpenHashMap<>());

        for (int dx = -portal.chunkRadius; dx <= portal.chunkRadius; dx++)
        {
            for (int dz = -portal.chunkRadius; dz <= portal.chunkRadius; dz++)
            {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                int count = map.getInt(pos);
                if (count <= 1)
                {
                    map.removeInt(pos);
                    level.getChunkSource().updateChunkForced(pos, false);
                } else {
                    map.put(pos, count - 1);
                }
            }
        }
    }
}
