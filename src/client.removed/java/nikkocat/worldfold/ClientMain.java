package nikkocat.worldfold;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMain implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("WF:Client");
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("World Fold detected and running on client");
	}
}