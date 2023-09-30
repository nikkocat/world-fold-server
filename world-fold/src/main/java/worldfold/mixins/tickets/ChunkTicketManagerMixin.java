package worldfold.mixins.tickets;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.*;
import worldfold.ChunkUtils;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Debug(export = true)
@Mixin(ChunkTicketManager.class)
public abstract class ChunkTicketManagerMixin {
    @Inject(at = @At("HEAD"), method = "handleChunkEnter")
    private void chunkEnteredTail(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo info) {
        ChunkPos centerChunkPos = pos.toChunkPos();
        handleOffsetEnter(centerChunkPos, player);
    }
    @Inject(at = @At("HEAD"), method = "handleChunkLeave")
    private void chunkLeftTail(ChunkSectionPos pos, ServerPlayerEntity player, CallbackInfo info) {
        ChunkPos centerChunkPos = pos.toChunkPos();
        handleOffsetLeave(centerChunkPos, player);
    }

    @Unique
    private static void ticketAdder(ServerPlayerEntity player, ServerWorld world, ChunkPos originalChunkPos) {
        world.getServer().submit(() -> {
            ChunkUtils.mimicChunk(player, world, originalChunkPos);
        });
    }

    @Unique
    private static void ticketRemover(ServerWorld world, ChunkPos chunkPos) {
        world.getServer().submit(() -> {
            world.getChunkManager().removeTicket(ChunkTicketType.FORCED, chunkPos, 0, chunkPos);
        });
    }

    @Unique
    private void handleOffset(ChunkPos centerChunkPos, ServerPlayerEntity player, boolean enter) {
        ServerWorld world = (ServerWorld) player.getWorld();
        List<ChunkPos> chunkPosList = new ArrayList<>();

        int radius = 6;
        int centerX = centerChunkPos.x;
        int centerZ = centerChunkPos.z;

        // TODO change it lol
        // its super bad....
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                if (distance <= radius) {
                    chunkPosList.add(new ChunkPos(x, z));
                }
            }
        }

        if (chunkPosList.isEmpty()) {
            return;
        }

        for (ChunkPos chunkPos : chunkPosList) {
            if (enter) {
                ticketAdder(player, world, chunkPos);
            } else {
//                ticketRemover(world, chunkPos);
            }
        }
    }

    @Unique
    private void handleOffsetEnter(ChunkPos centerChunkPos, ServerPlayerEntity player) {
        handleOffset(centerChunkPos, player, true);
    }

    @Unique
    private void handleOffsetLeave(ChunkPos centerChunkPos, ServerPlayerEntity player) {
        handleOffset(centerChunkPos, player, false);
    }
}