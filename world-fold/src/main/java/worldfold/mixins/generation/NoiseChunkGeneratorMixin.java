package worldfold.mixins.generation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Unique;
import worldfold.ChunkUtils;
import worldfold.access.ChunkGeneratorAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldfold.access.ConfiguredCarverAccess;

@Debug(export = true)
@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

    @Unique
    private ServerWorld world;

    @Inject(
            method = "carve",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/random/ChunkRandom;setCarverSeed(JII)V")
    )
    private void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep, CallbackInfo ci, @Local ConfiguredCarver<?> configuredCarver) {
        world = chunkRegion.toServerWorld();

        ((ConfiguredCarverAccess) (Object) configuredCarver).world_fold$setWorld(world);
    }


    @WrapOperation(
            method = "carve",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/gen/carver/ConfiguredCarver;shouldCarve(Lnet/minecraft/util/math/random/Random;)Z")
    )
    private boolean carve(ConfiguredCarver<?> instance, Random random, Operation<Boolean> original, ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

        if (ChunkUtils.chunkInRange(world, chunk.getPos())) {
            return original.call(instance, random);
        } else {
            return false;
        }
    }

    @Inject(
            method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk, CallbackInfo ci) {
        world = region.toServerWorld();

        if (!ChunkUtils.chunkInRange(world, chunk.getPos())) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/SharedConstants;isOutsideGenerationArea(Lnet/minecraft/util/math/ChunkPos;)Z")
    )
    private boolean populateNoise(ChunkPos chunkPos, Operation<Boolean> original, Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {

        ServerWorld world = ((ChunkGeneratorAccess) this).world_fold$getWorld();

        if (!ChunkUtils.chunkInRange(world, chunkPos)) {
            return true;
        }
        return original.call(chunkPos);
    }
}