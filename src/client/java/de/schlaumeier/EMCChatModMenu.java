package de.schlaumeier;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import me.shedaniel.autoconfig.AutoConfig;
import de.schlaumeier.config.EMCChatConfig;

public class EMCChatModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent ->
            AutoConfig.getConfigScreen(EMCChatConfig.class, parent).get();
    }
}
