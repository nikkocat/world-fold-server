package nikkocat.worldfold.access;

import net.minecraft.server.world.ServerWorld;

public interface ChunkGeneratorAccess {
    void setWorld(ServerWorld world);

    ServerWorld getWorld();
}
