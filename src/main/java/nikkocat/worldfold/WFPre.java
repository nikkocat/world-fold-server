package nikkocat.worldfold;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class WFPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
    }
}
