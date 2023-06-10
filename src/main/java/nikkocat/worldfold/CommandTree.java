package nikkocat.worldfold;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandTree {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("worldfold").requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("WorldFold v1.0.0"), false);
                    return 1;
                })
                .then(literal("add")
                        .then(literal("ticket")
                                .executes(ctx -> {
                                    ctx.getSource().sendMessage(Text.literal("Called /worldfold add ticket"));
                                    //ChunkLoader.loadChunk(32,32);
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .then(literal("ticket")
                                .executes(ctx -> {
                                    ctx.getSource().sendMessage(Text.literal("Called /worldfold remove ticket"));
                                    return 1;
                                })
                        )
                )
                .then(literal("test")
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(Text.literal("Called /worldfold test"));
                            impersonateNewChunk(ctx.getSource(), 1);
                            ctx.getSource().sendMessage(Text.literal("removed chunk"));
                            return 1;
                        })
                )
        );
    }

    public static Chunk impersonateNewChunk(ServerCommandSource source, int radius) {
        ServerWorld serverWorld = source.getWorld();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        serverChunkManager.threadedAnvilChunkStorage.verifyChunkGenerator();
        Vec3d vec3d = source.getPosition();
        ChunkPos targetChunkPos = new ChunkPos(BlockPos.ofFloored(vec3d));

        // remove every block from target chunk and replace with air by serverWorld.setBlockState()
        int i = targetChunkPos.z - radius;
        int j = targetChunkPos.z + radius;
        int k = targetChunkPos.x - radius;
        int l = targetChunkPos.x + radius;
        for (int m = i; m <= j; ++m) {
            for (int n = k; n <= l; ++n) {
                ChunkPos chunkPos2 = new ChunkPos(n, m);
                WorldChunk worldChunk = serverChunkManager.getWorldChunk(n, m, false);
                if (worldChunk == null || worldChunk.usesOldNoise()) continue;
                for (BlockPos blockPos : BlockPos.iterate(chunkPos2.getStartX(), serverWorld.getBottomY(), chunkPos2.getStartZ(), chunkPos2.getEndX(), serverWorld.getTopY() - 1, chunkPos2.getEndZ())) {
                    serverWorld.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
                }
            }
        }

//        TaskExecutor<Runnable> taskExecutor = TaskExecutor.create(Util.getMainWorkerExecutor(), "world-fold-test");

        return null;
    }
}
