package ender.dwmod.portals;

import java.util.UUID;

import org.joml.Quaternionf;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class Portal {
    public final UUID id;

    public final ResourceKey<Level> sourceDim;
    public final BlockPos sourcePos;

    public final ResourceKey<Level> targetDim;
    public final BlockPos targetPos;


    public final Quaternionf rotation;
    public final float scale;

    public final int chunkRadius;
    public boolean active;

    public Portal(ResourceKey<Level> source, BlockPos sourcePos, ResourceKey<Level> target, BlockPos targetPos, Quaternionf rotation, float scale)
    {
        this.id = null;
        sourceDim = source;
        this.sourcePos = sourcePos;
        targetDim = target;
        this.targetPos = targetPos;
        this.rotation = rotation;
        this.scale = 1.f;
        chunkRadius = 4;
        active = true;
    }

}
