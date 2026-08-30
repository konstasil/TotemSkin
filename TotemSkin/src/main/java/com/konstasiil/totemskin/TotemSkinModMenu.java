package com.konstasiil.totemskin;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConfigScreensEvent;

@EventBusSubscriber(modid = TotemSkin.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TotemSkinModMenu {

    @SubscribeEvent
    public static void onRegisterConfigScreens(RegisterConfigScreensEvent event) {
        event.register("client", TotemSkinConfigScreen::createScreen);
    }
}
