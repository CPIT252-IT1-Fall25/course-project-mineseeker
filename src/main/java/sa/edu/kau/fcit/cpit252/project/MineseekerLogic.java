package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * DESIGN PATTERN: Facade
 * <p>
 * This class encapsulates all the execution logic for the mineseeker command.
 * It provides a simplified interface:
 * - runWithDefaultRadius()
 * - runWithCustomRadius()
 * <p>
 * Internally handles complex operations: searching structures in rings, distance sorting,
 * formatting output, click-to-teleport components.
 * <p>
 * The class is stateless and globally accessible, like a utility facade for command execution.
 */
public final class MineseekerLogic {

    // Optimization constants
    public static final int STRUCTURE_MIN_SAMPLES_PER_RING = 12;
    public static final int STRUCTURE_MAX_SAMPLES_PER_RING = 32;
    public static final int STRUCTURE_RING_SIZE = 512; // blocks per ring
    public static final int STRUCTURE_MAX_EMPTY_RINGS = 3; // Stop after N consecutive empty rings
    public static final int STRUCTURE_MAX_TOTAL_ITERATIONS = 200; // Safety limit to prevent infinite loops
    public static final int STRUCTURE_CANDIDATE_MULTIPLIER = 3; // Find 3x requested count for better selection


    // -------------------------
    // BIOME SEARCH CONSTANTS
    // -------------------------
    public static final int BIOME_MIN_SAMPLES = 24;   // DIFFERENT VALUE
    public static final int BIOME_RING_SIZE = 256;    // DIFFERENT VALUE
    public static final int BIOME_CANDIDATE_MULTIPLIER = 3;
    public static final int BIOME_MAX_ITER = 200;     // SAFTEY LIMIT


    /**
     * Runs the command with the default radius of 12000 blocks.
     * Called when the user doesn't specify a radius.
     */
    public static int runWithDefaultRadius(CommandContext<CommandSourceStack> commandContext) {
        return runStractureSearch(commandContext, 12000); // Calls the main logic with the default
    }

    /**
     * Runs the command with a custom radius specified by the user.
     * Called when the user provides a radiusBlocks argument.
     */
    public static int runWithCustomRadius(CommandContext<CommandSourceStack> commandContext) {
        int radius = IntegerArgumentType.getInteger(commandContext, "radiusBlocks");
        return runStractureSearch(commandContext, radius);
    }

    /**
     * Main search algorithm - optimized for faster searches.
     * Uses a ring-based search pattern that expands outward from the player.
     *
     * @param ctx          The command context
     * @param radiusBlocks The search radius in blocks
     * @return The number of structures found and reported
     * <p>
     * Now moved to StractureSearchLogic
     */
    private static int runStractureSearch(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {
        return StructureSearchLogic.runStructureSearch(ctx, radiusBlocks);
    }

    /**
     * BIOME SEARCH LOGIC
     * Works like the structure search, but for biomes.
     * Now moved to BiomeSearchLogic
     */
    public static int runBiomeSearch(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {
        return BiomeSearchLogic.runBiomeSearch(ctx, radiusBlocks);

    }


}