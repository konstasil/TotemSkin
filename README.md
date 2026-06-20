# TotemSkin

Retextures the Totem of Undying to match the skin of the player holding it. When a player holds a totem, the texture dynamically changes to show a mini version of that player's skin.

## Features

- **Dynamic skin-based totem texture** - the totem shows a mini version of the holder's skin
- **Works for all players** - both premium and non-premium accounts
- **Per-player config** - set custom or vanilla totem mode for individual players via ModMenu + Cloth Config
- **Global & mob settings** - configure totem display mode globally and for mobs
- **Smart slot management** - up to 8 players get custom totems simultaneously, with automatic eviction of the farthest player when all slots are full
- **Multi-language**  -  English, Russian, Polish, German, French, Spanish, Portuguese

## Requirements

- [Cloth Config](https://modrinth.com/mod/cloth-config) (optional, for config screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for config screen)

## Download

- [Modrinth](https://modrinth.com/project/totemskin)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/totemskin)

## Installation

1. Download the latest TotemSkin JAR from [Modrinth](https://modrinth.com/project/totemskin)
2. Place the JAR in your `mods` folder
3. (Optional) Install Cloth Config and Mod Menu for the config screen

## Configuration

Open Mod Menu=>TotemSkin=>Config to access settings:

- **Global Totem Mode** - `CUSTOM` (skin-based) or `VANILLA` (default texture) for all players
- **Mobs Totem Mode** - `CUSTOM` or `VANILLA` for mobs holding totems
- **Per-player overrides** - configure each player individually (list auto-populates from server)

Config file: `config/totemskin.json`

## How It Works

The mod intercepts the totem rendering pipeline using mixins. When a player holds a Totem of Undying:

1. The player's skin texture is extracted (from DynamicTexture cache or resource pack)
2. A 16x16 totem texture is generated mapping skin pixels to totem regions (head, body, arms, legs, overlay)
3. The texture is written to a free slot in the item atlas
4. The totem's UV coordinates are remapped to the custom texture slot

Each player gets their own slot in the atlas (up to 8). When a player disconnects or moves far away, their slot is freed for reuse.

## License

MIT
