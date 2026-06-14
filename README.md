# TotemSkin

A Fabric client-side mod for Minecraft that dynamically retextures the Totem of Undying to match the skin of the player holding it.

## Features

- **Dynamic Totem Textures**: When a player holds a Totem of Undying, the totem texture automatically changes to a mini version of that player's skin.
- **Per-Player Rendering**: Each player sees a totem rendered with their own skin. When looking at another player holding a totem, you see their skin on the totem instead.
- **Inventory Support**: The custom totem texture appears both when held in hand and when displayed in the inventory.
- **Automatic Skin Download**: Player skins are downloaded automatically from Mojang servers and cached for performance.
- **Fallback Skins**: If a skin cannot be downloaded, a default Steve-like skin is used as a fallback.

## How It Works

The mod intercepts the totem rendering pipeline using Fabric Mixins. When a totem of undying is about to be rendered for a player entity:

1. The player's skin texture is downloaded (or retrieved from cache).
2. The `TotemTextureGenerator` extracts the face, torso, arms, and legs from the skin and composes them into a 16x16 totem texture.
3. The generated texture is uploaded directly into the item texture atlas, replacing the vanilla totem pixels at the GPU level.
4. After rendering, the original vanilla texture is restored.

This approach ensures that only the specific totem being rendered is affected — ground items and other non-player contexts always display the vanilla totem texture.

## Requirements

- Minecraft 26.1.2
- Fabric Loader >= 0.15.0
- Java >= 21

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) for Minecraft 26.1.2.
2. Download the latest TotemSkin JAR from [Modrinth](https://modrinth.com/project/totemskin).
3. Place the JAR file in your `mods` folder.
4. Launch Minecraft with the Fabric profile.

## Building from Source

```bash
./gradlew build
```

The built JAR will be located in `TotemSkin/build/libs/`.

## Localization

TotemSkin is localized into the following languages:

- English (en_us)
- Russian (ru_ru)
- Polish (pl_pl)
- German (de_de)
- French (fr_fr)
- Spanish (es_es)
- Portuguese (pt_br)

## License

MIT License
