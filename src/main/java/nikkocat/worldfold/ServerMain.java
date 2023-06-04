package nikkocat.worldfold;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;

public class ServerMain implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("WF:Server");
    public static int range = 16;
    // seed: -1998017228203478098

    @Override
    public void onInitialize() {
        LOGGER.info("This is World Fold. Key words: worldfold, messages begin with (WF:Module)");
        CommandRegistrationCallback.EVENT.register(CommandTree::registerCommands);
        ServerWorldEvents.LOAD.register(this::onWorldLoad);
        ServerTickEvents.START_WORLD_TICK.register(this::onTickBegin);
        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Attempting to disable spawn chunks...");
        ServerWorld world = server.getWorld(World.OVERWORLD);
        ChunkPos chunkPos = new ChunkPos(new BlockPos(world.getLevelProperties().getSpawnX(), 0, world.getLevelProperties().getSpawnZ()));
        world.getChunkManager().removeTicket(ChunkTicketType.START, chunkPos, 11, Unit.INSTANCE);
    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        LOGGER.info("LOAD: " + chunk.getPos().toString() + " " + chunk.getLevelType().name());
    }

    private void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        LOGGER.info("UNLOAD: " + chunk.getPos().toString() + " " + chunk.getLevelType().name());
    }

    private void onTickBegin(ServerWorld world) {

        List<ServerPlayerEntity> playerList = world.getPlayers();
        for (int i = 0; i < playerList.size(); i++) {
            ServerPlayerEntity player = playerList.get(i);

            int blockRange = range << 4;
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            if (x >= blockRange + 16) {
                tpPlayerMovement(world, player, x - (blockRange << 1) - 16, y, z);
            } else if (x <= -blockRange) {
                tpPlayerMovement(world, player, x + (blockRange << 1) + 16, y, z);
            } else if (z >= blockRange + 16) {
                tpPlayerMovement(world, player, x, y, z - (blockRange << 1) - 16);
            } else if (z <= -blockRange) {
                tpPlayerMovement(world, player, x, y, z + (blockRange << 1) + 16);
            }
        }
    }

    private void onWorldLoad(MinecraftServer server, ServerWorld world) {
        LOGGER.info("World loaded: " + world.getRegistryKey().getValue().toString());
    }

    private void tpPlayerMovement(ServerWorld world, ServerPlayerEntity player, double x, double y, double z) {
        ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
        float f = MathHelper.wrapDegrees(player.getYaw());
        float g = MathHelper.wrapDegrees(player.getPitch());
        Set<PlayerPositionLookS2CPacket.Flag> mvFlags = EnumSet.of(
                PlayerPositionLookS2CPacket.Flag.X,
                PlayerPositionLookS2CPacket.Flag.Y,
                PlayerPositionLookS2CPacket.Flag.Z,
                PlayerPositionLookS2CPacket.Flag.X_ROT,
                PlayerPositionLookS2CPacket.Flag.Y_ROT
        );
        // fallback TP ticket
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
        player.networkHandler.requestTeleport(x, y, z, f, g, mvFlags);
        player.setHeadYaw(f);
        //world.getServer().getPlayerManager().getSimulationDistance();
    }
}
