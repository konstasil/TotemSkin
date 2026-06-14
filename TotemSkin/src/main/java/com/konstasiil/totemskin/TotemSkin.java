package com.konstasiil.totemskin;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemSkin implements ClientModInitializer {
    public static final String MOD_ID = "totemskin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("TotemSkin initialized");
        TotemTextureManager.init();
    }
}
