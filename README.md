[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/Fv869B0L)
# Mineseeker


## Description
Mineseeker is a Minecraft Forge mod that enables players to locate multiple structures/biomes simultaneously. Users can specify the type of structure, the quantity to find, and the search radius, making it a powerful tool for exploration, fixing the core issue of the vanilla Locate command.

This mod was developed by Yousef Wajeeh, Faris Al-Harthi, and Anas Ibrahim
as part of the CPIT252 course project.


## Features
-   Find multiple instances of any in-game structure (e.g., villages, shipwrecks, ancient cities).
-   Specify the number of locations to find.
-   Set a custom search radius in blocks.
-   Provides auto-complete suggestions for structure names to make the command easier to use.

## Usage

Duo to some potential unfair environment in a multiplayer mod you must enable cheats in the settings, in this case the player will not be able to use this mod unless they turns it on, this step is essential to make sure that the player can't access the command without a permissionin.

The mod provides a single command with a few arguments.

```
/mineseeker <structure_name> <count> [radius]
```

-   `<structure_name>`: The name of the structure you want to find (e.g., `village`, `ancient_city`).
-   `<count>`: The number of structures to locate (1-50).
-   `[radius]`: (Optional) The search radius in blocks (512-64000). If not specified, it defaults to 12,000 blocks.

## Screenshots

<img width="617" height="133" alt="image" src="https://github.com/user-attachments/assets/2d4c6550-507d-477c-8346-af723d180d1e" />

As of now, the command confirms the parameters, but the full search logic is a work in progress.

## Building from Source

This project uses the Gradle build system. To build the mod and test it in the Minecraft client, you can run the following command from the project's root directory:

```bash
./gradlew runClient
```


## License

This project is licensed under the MIT License.