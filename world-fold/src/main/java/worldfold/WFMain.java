package worldfold;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WFMain implements ModInitializer {
    public static final String MOD_ID = "WorldFold";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static int range = 32;

    // seed: -1998017228203478098

    @Override
    public void onInitialize() {
//        LOGGER.info("This is World Fold. Key words: worldfold, messages begin with (WF:Module)");
        LOGGER.info("Loading " + MOD_ID);

        CommandRegistrationCallback.EVENT.register(CommandTree::registerCommands);
//        ServerTickEvents.START_WORLD_TICK.register(this::onTickBegin);
        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
//        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }

//    private void onServerStarted(MinecraftServer server) {
//        LOGGER.info("Attempting to disable spawn chunks...");
//        ServerWorld world = server.getWorld(World.OVERWORLD);
//        ChunkPos chunkPos = new ChunkPos(new BlockPos(world.getLevelProperties().getSpawnX(), 0, world.getLevelProperties().getSpawnZ()));
//        world.getChunkManager().removeTicket(ChunkTicketType.START, chunkPos, 11, Unit.INSTANCE);
//    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
//        LOGGER.info("LOAD: " + chunk.getPos().toString() + " " + chunk.getLevelType().name());
    }

    private void onChunkUnload(ServerWorld world, WorldChunk chunk) {
//        LOGGER.info("UNLOAD: " + chunk.getPos().toString() + " " + chunk.getLevelType().name());
    }

//    private void onTickBegin(ServerWorld world) {
//        int blockRange = range << 4;
//
//        for (ServerPlayerEntity entity : world.getPlayers()) {
//            double x = entity.getX();
//            double y = entity.getY();
//            double z = entity.getZ();
//
//            if (x >= blockRange + 16) {
//                tpPlayerMovement(world, entity, x - (blockRange << 1) - 16, y, z);
//            } else if (x <= -blockRange) {
//                tpPlayerMovement(world, entity, x + (blockRange << 1) + 16, y, z);
//            } else if (z >= blockRange + 16) {
//                tpPlayerMovement(world, entity, x, y, z - (blockRange << 1) - 16);
//            } else if (z <= -blockRange) {
//                tpPlayerMovement(world, entity, x, y, z + (blockRange << 1) + 16);
//            }
//        }
//    }
//
//    private void tpPlayerMovement(ServerWorld world, Entity entity, double x, double y, double z) {
//        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) x, (int) y, (int) z));
//        float f = MathHelper.wrapDegrees(entity.getYaw());
//        float g = MathHelper.wrapDegrees(entity.getPitch());
//        Set<PositionFlag> mvFlags = EnumSet.of(
//                PositionFlag.X,
//                PositionFlag.Y,
//                PositionFlag.Z,
//                PositionFlag.X_ROT,
//                PositionFlag.Y_ROT
//        );
//        // fallback TP ticket
//        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, entity.getId());
//        // check if player is riding something
//        if (entity.getVehicle() != null) {
//            entity.getVehicle().requestTeleport(x, y, z);
//        }
//
//        if (entity instanceof ServerPlayerEntity player) {
//            player.networkHandler.requestTeleport(x, y, z, f, g, mvFlags);
//        }
//    }
}
