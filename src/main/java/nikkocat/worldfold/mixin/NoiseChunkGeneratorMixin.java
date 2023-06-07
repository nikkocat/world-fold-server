package nikkocat.worldfold.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import nikkocat.worldfold.GenerationUtils;
import nikkocat.worldfold.access.ChunkGeneratorAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
    private ServerWorld world;

    @Inject(method = "carve", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/ChunkRandom;setCarverSeed(JII)V"))
    private void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep, CallbackInfo ci) {
        this.world = chunkRegion.toServerWorld();
        ((ChunkGeneratorAccess) this).setWorld(this.world);
    }


    @WrapOperation(method = "carve", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/gen/carver/ConfiguredCarver;shouldCarve(Lnet/minecraft/util/math/random/Random;)Z"))
    private boolean carveConditionMixin(ConfiguredCarver<?> instance, Random random, Operation<Boolean> original, ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        if (GenerationUtils.shouldChunkBeGenerated(this.world, chunk.getPos())) {
            return original.call(instance, random);
        } else {
            return false;
        }
    }

    @Inject(method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V", at = @At("HEAD"), cancellable = true)
    private void buildSurfaceMixin(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk, CallbackInfo ci) {
        this.world = region.toServerWorld();

        if (!GenerationUtils.shouldChunkBeGenerated(region.toServerWorld(), chunk.getPos())) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/SharedConstants;isOutsideGenerationArea(Lnet/minecraft/util/math/ChunkPos;)Z"))
    private boolean isOutsideGenerationAreaMixin(ChunkPos chunkPos, Operation<Boolean> original, Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ServerWorld world = ((ChunkGeneratorAccess) this).getWorld();
        if (GenerationUtils.shouldChunkBeGenerated(world, chunkPos)) {
            return original.call(chunkPos);
        } else {
            return true;
        }
    }
}