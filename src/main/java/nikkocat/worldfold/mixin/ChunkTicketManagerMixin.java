package nikkocat.worldfold.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import nikkocat.worldfold.WFMain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTicketManager.class)
public abstract class ChunkTicketManagerMixin {
    @Shadow @Final private SimulationDistanceLevelPropagator simulationDistanceTracker;
    @Shadow protected abstract int getPlayerSimulationLevel();
    @Shadow @Final private ChunkTicketManager.NearbyChunkTicketUpdater nearbyChunkTicketUpdater;
    @Shadow @Final private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;
    @Shadow @Final private ChunkTicketManager.DistanceFromNearestPlayerTracker distanceFromNearestPlayerTracker;

    // Modify arg
    // Modify var
    // Modify const

    private final int range = WFMain.range;

    private static void ticketAdder(ServerWorld world, int radius, int offsetX, int offsetZ) {
        ChunkPos offsetPos = new ChunkPos(offsetX, offsetZ);
        world.getChunkManager().addTicket(ChunkTicketType.PLAYER, offsetPos, radius, offsetPos);
        WFMain.LOGGER.info("ADD: ["+offsetX+", "+offsetZ+"]");
    }

    private void handleOffsetEnter(int x, int z, ServerPlayerEntity player) {
        handleOffset(x, z, player, true);
    }

    private void handleOffsetLeave(int x, int z, ServerPlayerEntity player) {
        handleOffset(x, z, player, false);
    }

    private void handleOffset(int x, int z, ServerPlayerEntity player, boolean enter) {
        ServerWorld world = (ServerWorld) player.getWorld();
        int radius = 33 - getPlayerSimulationLevel();
        int bound = range - radius;
        int offset = (range << 1) + 1;
        int offsetX = x;
        int offsetZ = z;
        boolean crossX = false;
        boolean crossZ = false;

        if (x > bound) {
            crossX = true;
            offsetX -= offset;
        } else if (x < -bound) {
            crossX = true;
            offsetX += offset;
        }

        if (z > bound) {
            crossZ = true;
            offsetZ -= offset;
        } else if (z < -bound) {
            crossZ = true;
            offsetZ += offset;
        }

        if (crossX || crossZ) {
            if (enter) {
                ticketAdder(world, radius + 1, offsetX, offsetZ);
            } else {
                ticketRemover(world, radius, offsetX, offsetZ);
            }
        }
    }


    private static void ticketRemover(ServerWorld world, int radius, int offsetX, int offsetZ) {
        ChunkPos offsetPos = new ChunkPos(offsetX, offsetZ);
        world.getChunkManager().removeTicket(ChunkTicketType.PLAYER, offsetPos, radius, offsetPos);
        WFMain.LOGGER.info("REMOVE: ["+offsetX+", "+offsetZ+"]");
    }

    @Inject(at = @At("HEAD"), method = "handleChunkEnter")
    private void chunkEnteredTail(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo info) {
        ChunkPos oldPos = pos.toChunkPos();
        handleOffsetEnter(oldPos.x, oldPos.z, player);
    }
    @Inject(at = @At("HEAD"), method = "handleChunkLeave")
    private void chunkLeftTail(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo info) {
        ChunkPos oldPos = pos.toChunkPos();
        handleOffsetLeave(oldPos.x, oldPos.z, player);
    }
}