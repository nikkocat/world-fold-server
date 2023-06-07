package nikkocat.worldfold;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public class GenerationUtils {

    /**
     * Returns true if the chunk is inside the world border and should be generated.
     *
     * @param world the server world
     * @param pos   the chunk position
     * @return true if the chunk should be generated
     */
    public static boolean shouldChunkBeGenerated(ServerWorld world, ChunkPos pos) {

        if (world != null) {
            if (world.getRegistryKey() != ServerWorld.OVERWORLD) {
                return true;
            }
        }

        int xBlockPos = pos.getStartX();
        int zBlockPos = pos.getStartZ();

        int xCenterBlockPos = 0;
        int zCenterBlockPos = 0;
        int borderSize = WFMain.range * 16;

        int blockRange = borderSize << 4;

        if (xBlockPos >= blockRange || xBlockPos <= -blockRange ||
                zBlockPos >= blockRange || zBlockPos <= -blockRange) {
            return false;
        }

        return Math.abs(xBlockPos - xCenterBlockPos) <= borderSize &&
                Math.abs(zBlockPos - zCenterBlockPos) <= borderSize;
    }

}

