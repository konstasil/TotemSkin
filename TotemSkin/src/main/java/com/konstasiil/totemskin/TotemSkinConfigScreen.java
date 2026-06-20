package com.konstasiil.totemskin;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class TotemSkinConfigScreen {

    public static net.minecraft.client.gui.screens.Screen createScreen(net.minecraft.client.gui.screens.Screen parent) {
        TotemSkinConfig config = TotemSkinConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("totemskin.config.title"))
                .setSavingRunnable(() -> TotemSkinConfig.get().save());

        ConfigEntryBuilder entry = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("totemskin.config.category.general"));
        general.addEntry(entry.startEnumSelector(
                        Component.translatable("totemskin.config.globalMode"),
                        TotemSkinConfig.TotemMode.class,
                        config.globalMode)
                .setDefaultValue(TotemSkinConfig.TotemMode.CUSTOM)
                .setTooltip(Component.translatable("totemskin.config.globalMode.tooltip"))
                .setSaveConsumer(config::setGlobalMode)
                .build());

        general.addEntry(entry.startEnumSelector(
                        Component.translatable("totemskin.config.mobsMode"),
                        TotemSkinConfig.TotemMode.class,
                        config.mobsMode)
                .setDefaultValue(TotemSkinConfig.TotemMode.CUSTOM)
                .setTooltip(Component.translatable("totemskin.config.mobsMode.tooltip"))
                .setSaveConsumer(config::setMobsMode)
                .build());

        general.addEntry(entry.startBooleanToggle(
                        Component.translatable("totemskin.config.showInInventory"),
                        config.showInInventory)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("totemskin.config.showInInventory.tooltip"))
                .setSaveConsumer(config::setShowInInventory)
                .build());

        ConfigCategory players = builder.getOrCreateCategory(Component.translatable("totemskin.config.category.players"));

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            for (AbstractClientPlayer player : mc.level.players()) {
                UUID uuid = player.getUUID();
                String name = player.getName().getString();
                TotemSkinConfig.TotemMode current = config.playerOverrides.getOrDefault(
                        uuid.toString(), config.globalMode);

                players.addEntry(entry.startEnumSelector(
                                Component.literal(name),
                                TotemSkinConfig.TotemMode.class,
                                current)
                        .setDefaultValue(config.globalMode)
                        .setTooltip(Component.translatable("totemskin.config.player.tooltip", name))
                        .setSaveConsumer(mode -> config.setPlayerOverride(uuid, mode))
                        .build());
            }

            if (mc.level.players().isEmpty()) {
                players.addEntry(entry.startTextDescription(
                        Component.translatable("totemskin.config.noPlayers"))
                        .build());
            }
        } else {
            players.addEntry(entry.startTextDescription(
                    Component.translatable("totemskin.config.noServer"))
                    .build());
        }

        return builder.build();
    }
}
