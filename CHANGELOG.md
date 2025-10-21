# v4-beta.3
###### Oct 21, 2025
- 1.21.9/10 support
- potentially fixed forge load crash?
- fixed shrubs
- fixed wind getting too strong at high altitudes (again)
- tweaked default config values

# v4-beta.2
###### Oct 03, 2025
- fixed crash when loading resource packs with invisible weather textures
- fixed custom tints saving incorrectly and invalidating the config
- fixed resource pack animated splash particles not loading correctly
- fixed ripples not respecting config options
- removed spam log line

# v4-beta.1
###### Sep 30, 2025
- New features:
  - added subtle block hit noises when blocks have rain falling on them
  - added subtle random note sounds to note blocks when rain falls on them
  - re-added streak particles
  - re-added mist particles (formerly ground fog)
- Config:
  - added block spawn types to spawn particles from block faces
  - added surface spawn type
  - added volume sliders for individual weather sounds
  - added dropdowns to list entries
  - disabled some buttons conditionally to make it clearer when values aren't being used
  - re-added ability to manually set spawn height limit instead of only using cloud height
  - re-added missing particle options
- Bugs fixed:
  - fixed particles becoming invisible when resource packs use very translucent textures
  - fixed vanilla water drip/splash particles not using the same tint as rain
  - fixed wind blowing too strongly at high altitudes
  - fixed maximum particle count breaking after closing config screen
  - fixed particles spawning inside of and falling through ceilings made of non-full blocks
  - fixed blended particles (mist) corrupting particle rendering
  - fixed rain sounds appearing above weather height limit
- Mod Compatibility:
  - added workaround for a bug where RNG stops working near valkyrian skies ships with a eureka helm

# v4-alpha.3
###### Jul 24, 2025
This release fixes various issues with alpha 2
- fixed haze/fog effects scaling incorrectly
- fixed config creation not creating new configs when it should
- fixed flickering at the edge of the particle render distance
- fixed culling on forge
- re-added ability to disable fabric registry sync
- re-added particle rotation animation
- tweaked rain collision animation to only scale vertically
- added config button to mod list on neo/forge
- updated russian translation (ty mpustovoi)

# v4-alpha.2
###### Jul 7, 2025
- New command:
  - /particlerain to quickly get to the config screen
- Mod support:
  - support for minecraft 1.20.1, 1.21.1, 1.21.4, 1.21.5, 1.21.6, 1.21.7
  - support for neoforge in addition to fabric
  - support for forge (1.20.1 only)
  - weather effects are less destructively replaced, sort of, allowing for better mod compatibility, in theory, sort of, perhaps.
- Custom particles:
  - customise any particle
  - create new particles
  - whitelist or blacklist biomes and blocks for particle spawning
  - assign a custom tint or use the fog, water, or map color
  - choose which textures are used
  - choose whether the particle is translucent, opaque, or blended
  - choose how the particle rotates
- Effect improvements:
  - fog effect, now called haze, has been made translucent and less dense, and is enabled by default
  - rain is now more realistic, angled relative to your movement, appearing to point towards you when moving at high speeds
  - particles scale down when hitting a block to reduce clipping through surfaces and ceilings
  - particles fade by distance instead of a set animation, looking more consistent when moving around
  - wind now varies by coordinates and shifts over time
- Spawning improvements:
  - particle density scales by speed to be more visually constant while moving quickly
  - particles now only spawn above the player unless moving quickly (eg: falling or flying)
  - cloud height particle spawn limit is fetched automatically
  - splash particles now use the particle render distance

# v4-alpha.1
###### May 14, 2025
- multiloader setup for
  - 1.20.1, 1.21.1, 1.21.4, 1.21.5 fabric & neoforge
  - 1.20.1 forge

# v3.3.6
###### Jul 2, 2025
- minecraft 1.21.6 (ty theendercore)
- update Simplified Chinese translation (ty Sasaki-Akari)

# v3.3.5
###### May 13, 2025
- fix underwater snow
- fix high shrub density
- add polish translation (ty Jaguarundi17)
- disable puddles by default

# v3.3.4
###### Mar 30, 2025
- updated to mc 1.21.5
- improved simplified chinese translations (ty Sasaki-Akari)
- shrub particles now use block texture instead of item texture
- re-added dithered fog for the falling fog effect (at a lower resolution for a slight performance boost)

# v3.3.3
###### Mar 26, 2025
- update simplified chinese (ty Sasaki-Akari)
- add mexican spanish translation (ty TheLegendofSaram)
- fix water rendering with sodium

# v3.3.2
###### Mar 15, 2025
- updated russian translation (ty mpustovoi)
- added missing puddle toggle to the effect toggles config
- fixed particles not appearing with the flashback mod and other cases where particles are cleared
- fixed puddles persisting across worlds

# v3.3.1
###### Mar 15, 2025
- fixed crash on joining world with sodium
- fixed culling issue with subtle effects
- added Seperate render distance for fog particles
- split ground fog spawn height to a minimum and maximum value

# v3.3.0
###### Mar 15, 2025
- added Simplified Chinese translation (ty Sasaki-Akari)
- puddles
- fixed level change not resetting particle count
- fixed shrubs crashing the game when they dont have an item texture, probably
- ground fog is now visible from below

# v3.2.0
###### Feb 19, 2025
- added block tag wiki button to config
- added global weather override option
- fixed floats displaying truncated in config
- updated ru_ru (courtesy of mpustovoi)

# v3.1.0
###### Feb 17, 2025
- Replaced dither fog texture with same texture used for ground fog
- replaced fog render type with one that supports overlapping translucency
- Replaced config. Dust spawn option can now take multiple block tags. requires yet another config library instead of cloth.
- option to disable using heightmap precipitation

# v3.0.8
###### Jan 13, 2025
- Update ru_ru (courtesy of mpustovoi)

# v3.0.7
###### Jan 6, 2025
- Update zh_tw.json courtesy of yichifauzi
- Add more values to debug command
- expand config objs by default to workaround cloth config bug
- Fix streaks not appearing on glass panes

# v3.0.6 (1.20-1.20.1)
###### Jan 2, 2025
- fix dust particles

# v3.0.6 (1.21.2)
###### Dec 24, 2024
- backport provided by Riflusso

# v3.0.6 (1.21.4)
###### Dec 23, 2024
- update to 1.21.4

# v3.0.5
###### Dec 23, 2024
- fix incorrect ripple resolution

# v3.0.5 backport 1.20
###### Dec 23, 2024
- backport v3.0.5 to 1.19.4 / 1.20 / 1.20.1 versions

# v3.0.4
###### Dec 22, 2024
- fix crash with non-latin alphabets

# v3.0.3
###### Dec 22, 2024
- update uk_ua (thanks '9gv')
- fix dust spawn block option not doing anything
- fix ripples being disabled when splashes are disabled
- added ability to disable rain's smoke impact particles on hot blocks
- fix water streaks appearing underwater
- add options to disable specific weather sounds
- fix wind sound not playing in some situations
- prevent particles from spawning above cloud height (& config options)
- add ripple resolution options

# v3.0.2
###### Dec 19, 2024
- Fixed missing textures when using modernfix mod
- Optimised textures
- Changed mod icon

# v3.0.1
###### Dec 18, 2024
- Add missing access widener entry to fabric mod json, fixing crash for some people.

# v3.0.0
###### Dec 18, 2024
- added biome tinting to particles
- added command /particlerain
- added ripple particles
- added streak particles
- added ground fog particles
- added resourcepack support
- added wind strength options
- added size options
- added rain opacity option
- rain is now angled by its velocity
- reduced wind below ground level
- improved splash spawning
- improved fog rendering
- particles fade out when crossing into a biome with a large temperature difference
- dust spawning now uses the sand tag instead of Ids
- dust uses the map color of the block it spawns above
- tumbling shrubs use the texture of the block they spawn above when spawning above a #minecraft:sword_efficient block
- dust spawns on the ground by default, old behaviour can be restored by disabling this in the settings
- fixed particles not spawning when other mods are hogging the particle count
- fixed particles becoming invisible at certain angles
- updated ko_kr (courtesy of good7777865)
- updated ru_ru (courtesy of mpustovoi)
- removed singular particle types

# v2.1.5-backport for MC1.20
###### Sep 25, 2024
- Backport to 1.20 (thanks Kawatt!)
- Better support for modded biomes and seasons
- configurable maximum amount of particles that can be spawned in at once
- Improved fog dithering by scaling fog particles with distance
- options for changing particle size

# v2.1.4
###### Aug 13, 2024
- Korean Translation ko_kr by @good7777865. Thanks!

# v2.1.3
###### Aug 12, 2024
- Ukrainian translation uk_ua by @Tarteroycc. Thanks!

# v2.1.2
###### Jul 10, 2024
- Translations for Traditional Chinese (courtesy of yichifauzi)
- Compatiblity with Serene Seasons
- Config option for whether to spawn shrubs

# v2.1.1
###### Jun 26, 2024
- Fixes Compatibility with ViaFabricPlus
- Fixes a crash upon exiting LAN worlds

# v2.1.0
###### Jun 25, 2024

It is recommended to reset your config via modmenu for this update.
- Updated to Minecraft 1.21.
- Added additional particles with multiple sprites in one texture, allowing for denser effects without as much of a performance drop.
- Added Dead Bush Particle to sandstorms.
- Added experimental fog particle. Off by default.
- Added config option for constant rain.
- Added Russian translations courtesy of mpustovoi.
- Particles now fade in and out.
- Rain now falls at an angle.
- Fixed issue preventing other clients from connecting when hosting a LAN world with the mod.
- Fixed smoke not appearing on hot blocks.

# v2.0.8
###### Oct 26, 2023
- Added compat for Modern Beta's beta desert and most other modded desert biomes (must be warm enough and have an Id containing the word "desert") (theres no generic tag for this like with badlands) (unless I didn't look hard enough)
- Removed rickroll :)

# v2.0.7
###### Jul 8, 2023
- Fixed particles being discolored when spawned with the particle command

# v2.0.6
###### Jun 30, 2023
- fixed transparency issue with complementary reimagined 2.2 readded missing color config options

# v2.0.5
###### Jun 18, 2023
- update to 1.20.1 (also supports 1.19.4) added configurable rotation and spawn velocity to snow particles

# v2.0.4 for mc1.19.4
###### Jun 17, 2023

# v2.0.4 for mc1.19.4
###### Jun 17, 2023
- updated to 1.19.4
- fixed spatial audio by adding new sound effects

# v2.0.2b
###### Jun 16, 2023
- 1.19.3, thanks shockz

# 2.0.1
###### Jun 23, 2022

# v2.0.0
###### Feb 5, 2022

# v1.5.0
###### Sep 10, 2021
- add config options for changing particle colors

# v1.4.1
###### Aug 14, 2021
- Seperated cloth config
- optimized images (thanks RDKRACZ)

# v1.4.0-1.16.5
###### Aug 9, 2021
- fixed dependencies

# v1.4.0-1.17
###### Jul 28, 2021
- fixed rain appearing in the end