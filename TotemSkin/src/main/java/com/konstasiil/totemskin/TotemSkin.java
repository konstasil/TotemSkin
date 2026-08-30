package com.konstasiil.totemskin;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TotemSkin.MOD_ID)
public class TotemSkin {
    public static final String MOD_ID = "totemskin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TotemSkin(IEventBus modEventBus) {
        LOGGER.info("TotemSkin initialized");
        TotemTextureManager.init();
    }
}
