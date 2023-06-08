package nikkocat.worldfold.mixin;

import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import nikkocat.worldfold.WFMain;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
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
    int range = WFMain.range;

    //@Inject(at = @At(value = "INVOKE",
    //        target = "Lnet/minecraft/world/chunk/WorldChunk;getPos()Lnet/minecraft/util/math/ChunkPos;",
    //        shift = At.Shift.AFTER),
    //        shift = At.Shift.BY, by = 2),
    //        method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V")
    @Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V")
    private void initFake1(WorldChunk chunk, LightingProvider lightProvider, BitSet skyBits, BitSet blockBits, CallbackInfo ci) {
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
        if (dewIt) {
            if (!chunk.getWorld().getChunkManager().isChunkLoaded(x, z)) {

                // TODO load chunk
                ServerWorld world = (ServerWorld) chunk.getWorld();
                ChunkPos pos = new ChunkPos(x, z);
//                world.getChunkManager().addTicket(ChunkTicketType.FORCED, pos, 1, pos);

                if (!chunk.getWorld().getChunkManager().isChunkLoaded(x, z)) {
//                    ServerMain.LOGGER.info("Not loaded!");
                    WorldChunk fakeChunk = new EmptyChunk(chunk.getWorld(), chunk.getPos(), null);
                    this.chunkData = new ChunkData(fakeChunk);
                    this.lightData = new LightData(fakeChunk.getPos(), lightProvider, skyBits, blockBits);
                    return;
                }
            }
//            ServerMain.LOGGER.info("Loaded!!!");
            WorldChunk fakeChunk = chunk.getWorld().getChunk(x, z);
            this.chunkData = new ChunkData(fakeChunk);
            this.lightData = new LightData(fakeChunk.getPos(), lightProvider, skyBits, blockBits);
        }
    }
}
