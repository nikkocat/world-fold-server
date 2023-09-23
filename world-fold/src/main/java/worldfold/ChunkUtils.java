package worldfold;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkUtils {

    public static boolean chunkInRange(ServerWorld world, ChunkPos pos) {

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

    public static ChunkPos getMimicChunkPos(ChunkPos chunkPos) {
        int range = WFMain.range;

        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        // Check if x is outside the positive range
        int adjustmentValue = (range * 2) + 1;

        // Check if x is outside the positive range
        if (chunkX > range) {
            chunkX -= adjustmentValue;
        }

        // Check if x is outside the negative range
        if (chunkX < -range) {
            chunkX += adjustmentValue;
        }

        // Check if z is outside the positive range
        if (chunkZ > range) {
            chunkZ -= adjustmentValue;
        }

        // Check if z is outside the negative range
        if (chunkZ < -range) {
            chunkZ += adjustmentValue;
        }

        return new ChunkPos(chunkX, chunkZ);
    }

    public static void mimicChunk(ServerPlayerEntity player, ServerWorld world, ChunkPos originalChunkPos) {

        ServerChunkManager chunkManager = world.getChunkManager();

        long start = System.currentTimeMillis();

        ChunkPos mimicChunkPos = getMimicChunkPos(originalChunkPos);

        if (!chunkManager.isChunkLoaded(mimicChunkPos.x, mimicChunkPos.z)) {
            chunkManager.addTicket(ChunkTicketType.FORCED, mimicChunkPos, 0, mimicChunkPos);

            // tick the chunk manager to force the ChunkHolders to be created
            chunkManager.tick(() -> false, true);
        }

        ThreadedAnvilChunkStorage chunkStorage = chunkManager.threadedAnvilChunkStorage;

        ChunkHolder holder = chunkStorage.getChunkHolder(mimicChunkPos.toLong());
        if (holder == null) {
            WFMain.LOGGER.warn("Added ticket for chunk but it was not added! ({}; {})", mimicChunkPos.x, mimicChunkPos.z);
            chunkManager.removeTicket(ChunkTicketType.FORCED, mimicChunkPos, 0, mimicChunkPos);
            return;
        }

        holder.getChunkAt(ChunkStatus.FULL, chunkStorage).whenComplete((result, throwable) -> {
            if (throwable == null && result.left().isPresent()) {

                WorldChunk mimicChunk = (WorldChunk) result.left().get();
                WFMain.LOGGER.info("Success! Loaded ({}; {}) - took {}ms", mimicChunkPos.x, mimicChunkPos.z, System.currentTimeMillis() - start);

                ServerPlayNetworkHandler networkHandler = player.networkHandler;
                WorldChunk fakeMimicChunk = new WorldChunk(world, originalChunkPos, mimicChunk.getUpgradeData(), mimicChunk.blockTickScheduler, mimicChunk.fluidTickScheduler, mimicChunk.getInhabitedTime(), mimicChunk.getSectionArray(), mimicChunk.entityLoader, mimicChunk.getBlendingData());

                networkHandler.sendPacket(new ChunkDataS2CPacket(fakeMimicChunk, chunkManager.getLightingProvider(), null, null));

                WFMain.LOGGER.info("Sent fake mimic chunk to {}!", player.getEntityName());
            } else {
                WFMain.LOGGER.error("Encountered unexpected error while generating chunk", throwable);
            }

            chunkManager.removeTicket(ChunkTicketType.FORCED, mimicChunkPos, 0, mimicChunkPos);
        });
    }
}

