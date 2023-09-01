package tools;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;
import static tools.ToolsMain.*;

public class Commands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("tools").requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(MOD_ID), false);
                    return 0;
                })
                .then(literal("send-packet")
                        .executes(ctx -> {

                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) {
                                return 1;
                            }

                            ServerPlayNetworkHandler serverPlayNetworkHandler = ctx.getSource().getPlayer().networkHandler;
                            serverPlayNetworkHandler.sendPacket(new PlayPingS2CPacket(100));
                            LOGGER.info("Sent ping packet!");

                            return 0;
                        })
                )
        );
    }

}
