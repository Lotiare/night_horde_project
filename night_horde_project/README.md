# Night Horde (Fabric 1.21.11) - Server-side

## Requirements implemented (your checklist)
- Night-start chance: default 2%
- Chance doubles if 2+ qualified players
- Requirements: at least one player online near a lodestone and above Y=70
- Announces in chat in red (configurable) with eerie messages
- Spawns 15-50 overworld hostile mobs (configurable)
- Mix of creepers/skeletons/zombies by default, add more by ID + weight
- Boss:
  - boosted health/damage
  - glowing
  - red name with prefix "Boss <Mob>"
  - Wither-style boss bar appears when you are close (configurable distance)
- Commands:
  - /nighthorde reload
  - /nighthorde test

## Build the jar (on your PC)
This environment can't download Fabric/Minecraft dependencies, so I included the full project source.
On your PC:
1) Install **Java 21**
2) In this folder run:
   - Windows: `gradlew build`
   - Linux/Mac: `./gradlew build`
3) Jar will be in `build/libs/`

## Config
Run server once to generate:
`config/night_horde.json`
