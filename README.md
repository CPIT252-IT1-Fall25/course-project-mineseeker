# Mineseeker - Minecraft Structure & Biome Finder

A Minecraft Forge mod that helps players locate structures and biomes efficiently using optimized search algorithms.

## Features

- **Structure Search**: Find any Minecraft structure (villages, temples, strongholds, etc.)
- **Biome Search**: Locate specific biomes quickly
- **Click-to-Teleport**: Interactive coordinates with teleport functionality
- **Adaptive Search**: Automatically optimizes search speed based on radius
- **Smart Filtering**: Prevents duplicate results from the same chunk

## Design Patterns Used

### 1. Builder Pattern
- **Location**: `MineseekerCommand.java`
- **Purpose**: Uses Brigadier's `LiteralArgumentBuilder` to construct complex commands incrementally
- **Example**: `.literal("mineseeker").then(...).executes(...)`

### 2. Command Pattern
- **Location**: `MineseekerCommand.java` + `MineseekerLogic.java`
- **Purpose**: Decouples command invocation from execution
- **Components**:
    - **Invoker**: LiteralArgumentBuilder
    - **Commands**: `runWithDefaultRadius()`, `runWithCustomRadius()`
    - **Receiver**: StructureSearchLogic, BiomeSearchLogic

### 3. Facade Pattern
- **Location**: `MineseekerLogic.java`
- **Purpose**: Provides simple interface hiding complex search operations
- **Benefit**: Commands don't need to know about search algorithms

### 4. Strategy Pattern
- **Location**: `search/SearchStrategy.java` and implementations
- **Purpose**: Allows different search algorithms (Radial, Spiral)
- **Benefit**: Easy to add new search patterns without modifying existing code

## Installation

1. Download the latest `.jar` from [Releases](releases/)
2. Place in your Minecraft `mods/` folder
3. Requires Minecraft Forge [version]

## Usage

### Structure Search

/mineseeker structure <structure_name> <count> [radius]

**Examples:**

```/mineseeker structure village 5``` 

```/mineseeker structure stronghold 1 30000```

```/mineseeker structure desert_pyramid 3 15000```

### Biome Search
/mineseeker biome <biome_name> <count> [radius]

**Examples:**

```/mineseeker biome mushroom_fields 1```

```/mineseeker biome ice_spikes 2 20000```

### Parameters
- `structure_name` / `biome_name`: Use tab completion for valid names
- `count`: Number of locations to find (1-50)
- `radius`: Search radius in blocks (512-64000, default: 12000)

## Building from Source
```bash
git clone [your-repo-url]
cd mineseeker
./gradlew build
```

Output `.jar` will be in `build/libs/`


## LLM Usage Disclosure

We used ChatGPT/Claude to assist with:
- Search algorithm optimization (ring-based search pattern)
- Design pattern implementation guidance
- Code refactoring suggestions
- Documentation generation

All code was reviewed, tested, and modified by team members.

## Project Structure
```
src/main/java/.../project/
├── Mineseeker.java              # Main mod class
├── Config.java                  # Configuration
├── MineseekerCommand.java       # Command registration (Builder + Command patterns)
├── MineseekerLogic.java         # Facade for search operations
├── MineseekerSuggestions.java   # Tab completion suggestions
├── StructureSearchLogic.java    # Structure search implementation
├── BiomeSearchLogic.java        # Biome search implementation
├── search/
│   ├── SearchStrategy.java      # Strategy interface
│   └── strategies/
│       ├── RadialSearchStrategy.java
│       └── SpiralSearchStrategy.java
└── util/
└── ComponentUtils.java      # Reusable utilities
```

## License

This project is licensed under the MIT License.

## Credits

Developed as a final project for CPIT252 - Software Design Patterns course at King Abdulaziz University.