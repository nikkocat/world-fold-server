package nikkocat.worldfold.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import nikkocat.worldfold.ServerMain;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin (ChunkDataS2CPacket.class)
public abstract class WFChunkDataPacketMixin {
    @Mutable
    @Shadow @Final private ChunkData chunkData;
    @Mutable
    @Shadow @Final private LightData lightData;
    int range = ServerMain.range;

    //@Inject(at = @At(value = "INVOKE",
    //        target = "Lnet/minecraft/world/chunk/WorldChunk;getPos()Lnet/minecraft/util/math/ChunkPos;",
    //        shift = At.Shift.AFTER),
    //        shift = At.Shift.BY, by = 2),
    //        method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V")
    @Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V")
    private void initFake1(@NotNull WorldChunk chunk, LightingProvider lightProvider, BitSet skyBits, BitSet blockBits, boolean nonEdge, CallbackInfo ci) {
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
                ServerMain.LOGGER.info("Not loaded!");
                WorldChunk fakeChunk = new EmptyChunk(chunk.getWorld(), chunk.getPos(), null);
                this.chunkData = new ChunkData(fakeChunk);
                this.lightData = new LightData(fakeChunk.getPos(), lightProvider, skyBits, blockBits, nonEdge);
                return;
            }
            ServerMain.LOGGER.info("Loaded!!!");
            WorldChunk fakeChunk = chunk.getWorld().getChunk(x, z);
            this.chunkData = new ChunkData(fakeChunk);
            this.lightData = new LightData(fakeChunk.getPos(), lightProvider, skyBits, blockBits, nonEdge);
        }
    }
}
