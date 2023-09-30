package worldfold.mixins.generation;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Unique;
import worldfold.ChunkUtils;
import worldfold.access.ConfiguredCarverAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ConfiguredCarver.class)
public class ConfiguredCarverMixin implements ConfiguredCarverAccess {
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

    @Inject(method = "carve", at = @At(value = "HEAD"), cancellable = true)
    private void carveMixin(CarverContext context, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, net.minecraft.util.math.random.Random random, AquiferSampler aquiferSampler, ChunkPos pos, CarvingMask mask, CallbackInfoReturnable<Boolean> cir) {
        if (!ChunkUtils.chunkInRange(world_fold$getWorld(), pos)) {
            cir.setReturnValue(false);
        }
    }
}
