package packetlogger;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PLMain implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("PacketLogger");

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Packet Logger");
    }
}
