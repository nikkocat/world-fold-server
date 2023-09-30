package worldfold.access;

import net.minecraft.server.world.ServerWorld;

public interface ChunkGeneratorAccess {
    void world_fold$setWorld(ServerWorld world);

    ServerWorld world_fold$getWorld();
}
