package com.konstasiil.totemskin;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TotemSkin.MOD_ID)
public class TotemSkin {
    public static final String MOD_ID = "totemskin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TotemSkin(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("TotemSkin initialized");
        TotemTextureManager.init();

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> TotemSkinConfigScreen.createScreen(parent));
    }
}
