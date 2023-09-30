package worldfold.mixins.generation;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Unique;
import worldfold.ChunkUtils;
import worldfold.access.ChunkGeneratorAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorAccess {
    @Unique
    ServerWorld world;

    @Override
    public void world_fold$setWorld(ServerWorld world) {
        this.world = world;
    }

    @Override
    public ServerWorld world_fold$getWorld() {
        return this.world;
    }

    @Inject(method = "generateFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;isOutsideGenerationArea(Lnet/minecraft/util/math/ChunkPos;)Z"), cancellable = true)
    private void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci, @Local ChunkPos chunkPos) {
        world_fold$setWorld(world.toServerWorld());

        if (!ChunkUtils.chunkInRange(world.toServerWorld(), chunkPos)) {
            ci.cancel();
        }
    }
}
