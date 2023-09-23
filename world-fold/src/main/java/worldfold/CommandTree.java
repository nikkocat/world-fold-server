package worldfold;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

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
                .then(literal("mimic-corresponding-chunk")
                        .executes(ctx -> {
                            ctx.getSource().getServer().submit(() -> {
                                mimicChunk(ctx.getSource());
                            });
                            return 1;
                        })
                )
        );
    }

    private static void mimicChunk(ServerCommandSource source ) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendMessage(Text.literal("This command can be executed only by player"));
            return;
        }

        ServerWorld world = source.getWorld();

        Vec3d vec3d = source.getPosition();
        ChunkPos originalChunkPos = new ChunkPos(BlockPos.ofFloored(vec3d));

        if (ChunkUtils.chunkInRange(world, originalChunkPos)) {
            source.sendMessage(Text.literal("You are still inside the fold range!"));
            return;
        }

        ChunkUtils.mimicChunk(player, world, originalChunkPos);
    }
}
