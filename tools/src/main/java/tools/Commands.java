package tools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.minecraft.command.CommandException;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Constructor;
import java.util.*;

import static net.minecraft.server.command.CommandManager.*;
import static tools.ToolsMain.*;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, List<Class<?>> packetClasses) {
        dispatcher.register(literal("tools").requires(source -> source.hasPermissionLevel(4))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(MOD_ID), false);
                    return 0;
                })
        );


        for (Class<?> clazz : packetClasses) {
            dispatcher.register(literal("tools").requires(source -> source.hasPermissionLevel(4))
                .then(literal("send")
                    .then(literal(clazz.getSimpleName())
                        .then(
                            argument("args", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                if (player == null) {
                                    return 1;
                                }

                                Object args = ctx.getArgument("args", Object.class);
                                String[] argArray = args.toString().split(" "); // separate args

                                Packet<?> packet = createPacketInstance(clazz, argArray);

                                player.networkHandler.sendPacket(packet);
                                ctx.getSource().sendMessage(Text.literal("Packet " + packet.getClass().getSimpleName() + " sent to " + player.getName().getString() + " with provided arguments: " + Arrays.toString(argArray)).formatted(Formatting.DARK_GREEN));
                                return 0;
                            })
                        )
                    )
                )
            );

        }
    }

    public static Packet<?> createPacketInstance(Class<?> clazz, String... args) {

        try {
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.getParameterCount() == args.length)
                    .findAny()
                    .orElseThrow(() -> new NoSuchMethodException("No constructor found with args " + Arrays.toString(args)));

            Class<?>[] parameterTypes = constructor.getParameterTypes();

            List<Object> correctedArgs = tryToFixParameterTypes(parameterTypes, args);

            if (correctedArgs.isEmpty()) {
                return (Packet<?>) constructor.newInstance();
            }

            return (Packet<?>) constructor.newInstance(correctedArgs.toArray());

        } catch (Exception e) {
//            e.printStackTrace();
            throw new CommandException(Text.literal("Failed to create instance of " + clazz.getSimpleName() + " packet " + e.getMessage().toLowerCase()).formatted(Formatting.RED));
        }
    }

    public static List<Object> tryToFixParameterTypes(Class<?>[] correctParameterTypes, String... passedArgs) {

        List<Object> correctedArgs = new ArrayList<>();

        for (int i = 0; i < passedArgs.length; i++) {
            String passedArg = passedArgs[i];
            Class<?> correctArgType = correctParameterTypes[i];

            // Parse the string to the correct type
            if (correctArgType.equals(Integer.class) || correctArgType.equals(int.class)) {
                correctedArgs.add(Integer.valueOf(passedArg));

            } else if (correctArgType.equals(Long.class) || correctArgType.equals(long.class)) {
                correctedArgs.add(Long.valueOf(passedArg));

            } else if (correctArgType.equals(Double.class) || correctArgType.equals(double.class)) {
                correctedArgs.add(Double.valueOf(passedArg));

            } else if (correctArgType.equals(Boolean.class) || correctArgType.equals(boolean.class)) {
                correctedArgs.add(Boolean.valueOf(passedArg));

            } else if (correctArgType.equals(Text.class)) {
                correctedArgs.add(Text.of(passedArg));
            }
        }

        return correctedArgs;
    }



    public static List<Class<?>> getPackets() {
        String packageName = "net.minecraft.network.packet.s2c";

        List<Class<?>> classes = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .scan()) {

            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                if (!classInfo.getSimpleName().contains("S2CPacket")) continue;
                if (!classInfo.isStandardClass()) continue;
                Class<?> clazz = Class.forName(classInfo.getName());
                classes.add(clazz);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return classes;
    }
}


