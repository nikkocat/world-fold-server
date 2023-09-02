package tools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ToolsMain implements ModInitializer {

    public static final String MOD_ID = "WorldFold Tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loading " + MOD_ID);

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> Commands.registerCommands(dispatcher, Commands.getPackets()));
    }
}
