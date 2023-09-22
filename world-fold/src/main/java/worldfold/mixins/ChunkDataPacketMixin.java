package worldfold.mixins;

import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.*;
import worldfold.WFMain;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin (ChunkDataS2CPacket.class)
public abstract class ChunkDataPacketMixin {
    @Mutable
    @Shadow @Final private ChunkData chunkData;
    @Mutable
    @Shadow @Final private LightData lightData;
    @Unique
    int range = WFMain.range;
    @Unique
    int fakeChunkX;
    @Unique
    int fakeChunkZ;
    @Unique
    MinecraftServer server;
    @Unique
    long start;

    @Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V")
    private void sendFakeChunk(WorldChunk chunk, LightingProvider lightProvider, BitSet skyBits, BitSet blockBits, CallbackInfo ci) {
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        boolean dewIt = false;
        if (x > range) {
            dewIt = true;
            x -= (range << 1) + 1;
        }
        if (x < -range) {
            dewIt = true;
            x += (range << 1) + 1;
        }
        if (z > range) {
            dewIt = true;
            z -= (range << 1) + 1;
        }
        if (z < -range) {
            dewIt = true;
            z += (range << 1) + 1;
        }

        fakeChunkX = x;
        fakeChunkZ = z;

        if (dewIt) {

            server = chunk.getWorld().getServer();

            if (server == null) {
                WFMain.LOGGER.error("SERVER NULL?");
                return;
            }

            start = System.currentTimeMillis();

            // this works, it greatly gets chunks which it has to but
            // there's a problem if it takes too long then this packet is sent but empty
            // which causes strange empty chunky holes in the world on client
            // (and also 'join()' method on these completable futures just crashes)
            // what we can do is:
            // TODO if chunk is coords are out of the range limit
            //  then, just don't send this packet instead we will inject to different class
            //  where we will follow the player and send them faked chunks in runtime

            server.submit(() -> {

                ServerWorld world = (ServerWorld) chunk.getWorld();
                ServerChunkManager chunkManager = world.getChunkManager();
                ChunkPos chunkPos = new ChunkPos(fakeChunkX, fakeChunkZ);

                if (!chunk.getWorld().getChunkManager().isChunkLoaded(fakeChunkX, fakeChunkZ)) {
                    chunkManager.addTicket(ChunkTicketType.FORCED, chunkPos, 0, chunkPos);

                    // tick the chunk manager to force the ChunkHolders to be created
                    chunkManager.tick(() -> false, true);
                }

                ThreadedAnvilChunkStorage chunkStorage = chunkManager.threadedAnvilChunkStorage;

                ChunkHolder holder = chunkStorage.getChunkHolder(chunkPos.toLong());
                if (holder == null) {
                    WFMain.LOGGER.warn("Added ticket for chunk but it was not added! ({}; {})", fakeChunkX, fakeChunkZ);
                    removeTicket(chunkManager, chunkPos);
                    return;
                }

                holder.getChunkAt(ChunkStatus.FULL, chunkStorage).whenComplete((result, throwable) -> {
                    if (throwable == null && result.left().isPresent()) {

                        WorldChunk fakeChunk = (WorldChunk) result.left().get();
                        chunkData = new ChunkData(fakeChunk);
                        lightData = new LightData(fakeChunk.getPos(), lightProvider, skyBits, blockBits);

                        WFMain.LOGGER.info("Success! Loaded ({}; {}) - took {}ms", fakeChunkX, fakeChunkZ, System.currentTimeMillis() - start);
                    } else {
                        WFMain.LOGGER.error("Encountered unexpected error while generating chunk", throwable);
                    }

                    removeTicket(chunkManager, chunkPos);
                    }
                );
            });
        }
    }

    @Unique
    private void removeTicket(ServerChunkManager chunkManager, ChunkPos chunkPos) {
        server.submit(() -> chunkManager.removeTicket(ChunkTicketType.FORCED, chunkPos, 0, chunkPos));
    }
}
