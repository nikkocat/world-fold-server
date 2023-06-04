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
import nikkocat.worldfold.ServerMain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTicketManager.class)
public abstract class WFChunkTicketManagerMixin {
    @Shadow @Final private SimulationDistanceLevelPropagator simulationDistanceTracker;
    @Shadow protected abstract int getPlayerSimulationLevel();
    @Shadow @Final private ChunkTicketManager.NearbyChunkTicketUpdater nearbyChunkTicketUpdater;
    @Shadow @Final private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;
    @Shadow @Final private ChunkTicketManager.DistanceFromNearestPlayerTracker distanceFromNearestPlayerTracker;

    // Modify arg
    // Modify var
    // Modify const

    private int range = ServerMain.range;
    private void handleOffsetEnter(int x, int z, ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();
        // +1 on the radius to fix border loading, idk why it works
        int radius = 33 - getPlayerSimulationLevel() + 1;
        int bound = range - radius;
        int offset = (range << 1) + 1;
        int offsetX = x;
        int offsetZ = z;
        boolean crossX = false;
        boolean crossZ = false;
        if (x > bound) {
            crossX = true;
            offsetX -= offset;
            ticketAdder(world, radius, offsetX, offsetZ);
        }
        if (x < -bound) {
            crossX = true;
            offsetX += offset;
            ticketAdder(world, radius, offsetX, offsetZ);
        }
        if (z > bound) {
            crossZ = true;
            offsetZ -= offset;
            ticketAdder(world, radius, offsetX, offsetZ);
        }
        if (z < -bound) {
            crossZ = true;
            offsetZ += offset;
            ticketAdder(world, radius, offsetX, offsetZ);
        }
        if (crossX && crossZ) {
            ticketAdder(world, radius, offsetX, offsetZ);
        }
    }

    private static void ticketAdder(ServerWorld world, int radius, int offsetX, int offsetZ) {
        ChunkPos offsetPos = new ChunkPos(offsetX, offsetZ);
        world.getChunkManager().addTicket(ChunkTicketType.PLAYER, offsetPos, radius, offsetPos);
        System.out.println("ADD: ["+offsetX+", "+offsetZ+"]");
    }

    private void handleOffsetLeave(int x, int z, ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();
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
            ticketRemover(world, radius, offsetX, offsetZ);
        }
        if (x < -bound) {
            crossX = true;
            offsetX += offset;
            ticketRemover(world, radius, offsetX, offsetZ);
        }
        if (z > bound) {
            crossZ = true;
            offsetZ -= offset;
            ticketRemover(world, radius, offsetX, offsetZ);
        }
        if (z < -bound) {
            crossZ = true;
            offsetZ += offset;
            ticketRemover(world, radius, offsetX, offsetZ);
        }
        if (crossX && crossZ) {
            ticketRemover(world, radius, offsetX, offsetZ);
        }
    }

    private static void ticketRemover(ServerWorld world, int radius, int offsetX, int offsetZ) {
        ChunkPos offsetPos = new ChunkPos(offsetX, offsetZ);
        world.getChunkManager().removeTicket(ChunkTicketType.PLAYER, offsetPos, radius, offsetPos);
        System.out.println("REMOVE: ["+offsetX+", "+offsetZ+"]");
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