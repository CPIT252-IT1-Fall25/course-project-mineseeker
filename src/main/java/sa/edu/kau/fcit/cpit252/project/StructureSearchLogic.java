package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import sa.edu.kau.fcit.cpit252.project.search.SearchStrategy;
import sa.edu.kau.fcit.cpit252.project.search.strategies.RadialSearchStrategy;
import sa.edu.kau.fcit.cpit252.project.util.ComponentUtils;

import java.util.*;
import java.util.stream.Collectors;

public final class StructureSearchLogic {

    private static final int MIN_SAMPLES_PER_RING = MineseekerLogic.STRUCTURE_MIN_SAMPLES_PER_RING;
    private static final int MAX_SAMPLES_PER_RING = MineseekerLogic.STRUCTURE_MAX_SAMPLES_PER_RING;
    private static final int RING_SIZE = MineseekerLogic.STRUCTURE_RING_SIZE;
    private static final int MAX_EMPTY_RINGS = MineseekerLogic.STRUCTURE_MAX_EMPTY_RINGS;
    private static final int MAX_TOTAL_ITERATIONS = MineseekerLogic.STRUCTURE_MAX_TOTAL_ITERATIONS;
    private static final int CANDIDATE_MULTIPLIER = MineseekerLogic.STRUCTURE_CANDIDATE_MULTIPLIER;

    public static int runStructureSearch(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {

        CommandSourceStack src = ctx.getSource();
        ServerPlayer player;

        try {
            player = src.getPlayerOrException();
        } catch (Exception ex) {
            src.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        String rawIn = StringArgumentType.getString(ctx, "target"); //changed to target for the new suggestions
        int requestedCount = IntegerArgumentType.getInteger(ctx, "count");

        HolderSet<Structure> target = resolveTarget(level, rawIn);
        if (target == null) {
            src.sendFailure(Component.literal("Unknown structure: \"" + rawIn + "\". Use tab completion for valid structures."));
            return 0;
        }

        final BlockPos playerPos = player.blockPosition();

        // Adaptive sampling: fewer samples for larger radii to improve performance
        int samplesPerRing = calculateSamplesPerRing(radiusBlocks);

        // Use chunk coordinates as keys to avoid duplicate structures in the same chunk
        record ChunkKey(int cx, int cz) {}

        Set<ChunkKey> seen = new HashSet<>();
        Map<ChunkKey, BlockPos> found = new HashMap<>();

        int maxRings = Math.max(1, radiusBlocks / RING_SIZE);
        int emptyRings = 0;
        int totalIterations = 0;
        int targetCandidates = requestedCount * CANDIDATE_MULTIPLIER;

        // DESIGN PATTERN: Strategy Pattern
        SearchStrategy strategy = new RadialSearchStrategy();

        // Search in expanding rings around the player
        outer:
        for (int ring = 1; ring <= maxRings; ring++) {
            int ringRadius = ring * RING_SIZE;
            int radiusChunks = Math.max(16, ringRadius / 16);
            boolean foundInRing = false;

            // Generate sample probe points using Strategy Pattern
            List<BlockPos> searchPositions =
                    strategy.generateSearchPositions(playerPos, ringRadius, samplesPerRing);

            for (BlockPos probe : searchPositions) {

                totalIterations++;
                if (totalIterations > MAX_TOTAL_ITERATIONS)
                    break outer;

                // Find nearest structure from probe point
                Pair<BlockPos, Holder<Structure>> result =
                        level.getChunkSource().getGenerator()
                                .findNearestMapStructure(level, target, probe, radiusChunks, false);

                if (result == null) continue;

                foundInRing = true;
                BlockPos hit = result.getFirst();
                // Convert block position to chunk coordinates (divide by 16, using bit shift)
                ChunkKey key = new ChunkKey(hit.getX() >> 4, hit.getZ() >> 4);

                // Only add if we haven't seen this chunk before (avoid duplicates)
                if (seen.add(key)) {
                    found.put(key, hit.immutable());

                    // Early termination: if we found enough candidates, stop searching
                    if (found.size() >= targetCandidates) {
                        break outer;
                    }
                }
            }

            // Optimization: if we haven't found anything in several consecutive rings, stop early
            // Only stop if we haven't found ANY structures yet (not if we found some earlier)
            if (!foundInRing) {
                emptyRings++;
                if (emptyRings >= MAX_EMPTY_RINGS && found.isEmpty()) {
                    break;
                }
            } else {
                emptyRings = 0; // Reset counter if we found something in this ring
            }
        }

        if (found.isEmpty()) {
            src.sendFailure(Component.literal("No " + prettyName(target) + " found within " + radiusBlocks + " blocks."));
            return 0;
        }

        // Sort by distance and take the N closest structures
        List<BlockPos> best = found.values().stream()
                .sorted(Comparator.comparingDouble(p -> ComponentUtils.distance2D(p, playerPos)))
                .limit(requestedCount)
                .collect(Collectors.toList());

        // Send header message
        String structureName = prettyName(target);
        src.sendSuccess(() -> Component.literal(
                String.format("Found %d %s within %d blocks:", best.size(), structureName, radiusBlocks)
        ), false);

        // Send individual structure locations (click-to-teleport)
        for (int i = 0; i < best.size(); i++) {

            BlockPos p = best.get(i);
            long distance = Math.round(ComponentUtils.distance2D(p, playerPos));

            Component coordsComponent =
                    ComponentUtils.createTeleportComponent(p, player.getName().getString());

            Component line = Component.literal("  " + (i + 1) + ". ")
                    .append(coordsComponent)
                    .append(Component.literal(" (" + distance + " blocks away)"));

            src.sendSuccess(() -> line, false);
        }


        return best.size();
    }

    /**
     * Calculate adaptive samples per ring based on radius.
     * Smaller radii get more samples for accuracy, larger radii get fewer for speed.
     * This optimization significantly improves performance for large search areas.
     *
     * @param radiusBlocks The search radius in blocks
     * @return The number of sample points to use per ring
     */
    private static int calculateSamplesPerRing(int radiusBlocks) {
        if (radiusBlocks <= 2000) {
            return MAX_SAMPLES_PER_RING; // 32 samples for small radius (high accuracy)
        } else if (radiusBlocks <= 10000) {
            return 24; // 24 samples for medium radius
        } else if (radiusBlocks <= 30000) {
            return 16; // 16 samples for large radius
        } else {
            return MIN_SAMPLES_PER_RING; // 12 samples for very large radius (prioritize speed)
        }
    }

    /**
     * Convert string to ResourceLocation.
     * Handles both "namespace:path" and plain "path" (defaults to minecraft:path).
     *
     * @param s The string to convert
     * @return ResourceLocation or null if invalid
     */
    private static ResourceLocation toRL(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            if (s.contains(":")) {
                return new ResourceLocation(s);
            }
            return new ResourceLocation("minecraft", s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get a pretty name for the structure set.
     * Extracts the structure name from the first structure in the set.
     *
     * @param set The HolderSet containing structures
     * @return A formatted structure name (e.g., "minecraft:village")
     */
    private static String prettyName(HolderSet<Structure> set) {
        return set.stream()
                .findFirst()
                .flatMap(Holder::unwrapKey)
                .map(k -> k.location().toString())
                .orElse("structure");
    }


    /**
     * Resolve structure target from string input.
     * Supports: tag format (#namespace:path), namespaced ID (namespace:path), or plain path (path).
     */
    private static HolderSet<Structure> resolveTarget(ServerLevel level, String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        // Tag form: #namespace:path
        if (s.startsWith("#")) {
            String tagStr = s.substring(1);
            ResourceLocation tagId = toRL(tagStr);
            if (tagId == null) return null;

            TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, tagId);
            var named = reg.getTag(tag);
            return named.map(x -> (HolderSet<Structure>) x).orElse(null);
        }

        // Exact id form: namespace:path or plain path → minecraft:path
        ResourceLocation id = toRL(s);
        if (id != null) {
            var holder = reg.getHolder(ResourceKey.create(Registries.STRUCTURE, id));
            if (holder.isPresent()) {
                return HolderSet.direct(holder.get());
            }
        }

        // Loose path match (e.g., "village" matches "minecraft:village")
        // Optimized: use stream instead of collecting to list first
        Optional<Holder.Reference<Structure>> match = reg.holders()
                .filter(h -> h.unwrapKey().isPresent())
                .filter(h -> {
                    ResourceLocation rl = h.unwrapKey().get().location();
                    return rl.getPath().equalsIgnoreCase(s);
                })
                .findFirst();

        if (match.isPresent()) {
            return HolderSet.direct(match.get());
        }
        return null;
    }
}
