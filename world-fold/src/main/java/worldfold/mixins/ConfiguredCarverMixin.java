package worldfold.mixins;

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
import worldfold.ChunkUtils;
import worldfold.access.ConfiguredCarverAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ConfiguredCarver.class)
public class ConfiguredCarverMixin implements ConfiguredCarverAccess {
    ServerWorld serverWorld;

    @Override
    public void setWorld(ServerWorld world) {
        this.serverWorld = world;
    }

    @Override
    public ServerWorld getWorld() {
        return this.serverWorld;
    }

    @Inject(method = "carve", at = @At(value = "HEAD"), cancellable = true)
    private void carveMixin(CarverContext context, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, net.minecraft.util.math.random.Random random, AquiferSampler aquiferSampler, ChunkPos pos, CarvingMask mask, CallbackInfoReturnable<Boolean> cir) {
        if (!ChunkUtils.chunkInRange(getWorld(), pos)) {
            cir.setReturnValue(false);
        }
    }
}
