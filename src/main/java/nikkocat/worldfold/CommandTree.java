package nikkocat.worldfold;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.*;
import net.minecraft.text.Text;

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
        );
    }
}
