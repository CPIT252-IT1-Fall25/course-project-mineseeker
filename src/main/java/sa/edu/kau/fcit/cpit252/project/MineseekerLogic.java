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
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
   /**
    * DESIGN PATTERN: Facade
    *
    * This class encapsulates all the execution logic for the mineseeker command.
    * It provides a simplified interface:
    * - runWithDefaultRadius()
    * - runWithCustomRadius()
    *
    * Internally handles complex operations: searching structures in rings, distance sorting,
    * formatting output, click-to-teleport components.
    *
    * The class is stateless and globally accessible, like a utility facade for command execution.
    */
public final class MineseekerLogic {

    // Optimization constants
    private static final int MIN_SAMPLES_PER_RING = 12;
    private static final int MAX_SAMPLES_PER_RING = 32;
    private static final int RING_SIZE = 512; // blocks per ring
    private static final int MAX_EMPTY_RINGS = 3; // Stop after N consecutive empty rings
    private static final int MAX_TOTAL_ITERATIONS = 200; // Safety limit to prevent infinite loops
    private static final int CANDIDATE_MULTIPLIER = 3; // Find 3x requested count for better selection

    /**
     * Runs the command with the default radius of 12000 blocks.
     * Called when the user doesn't specify a radius.
     */
    public static int runWithDefaultRadius(CommandContext<CommandSourceStack> commandContext) {
        return run(commandContext, 12000); // Calls the main logic with the default
    }

    /**
     * Runs the command with a custom radius specified by the user.
     * Called when the user provides a radiusBlocks argument.
     */
    public static int runWithCustomRadius(CommandContext<CommandSourceStack> commandContext) {
        int radius = IntegerArgumentType.getInteger(commandContext, "radiusBlocks");
        return run(commandContext, radius);
    }

    /**
     * Main search algorithm - optimized for faster searches.
     * Uses a ring-based search pattern that expands outward from the player.
     *
     * @param ctx The command context
     * @param radiusBlocks The search radius in blocks
     * @return The number of structures found and reported
     */
    private static int run(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {
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

        // Search in expanding rings around the player
        outer:
        for (int ring = 1; ring <= maxRings; ring++) {
            int ringRadius = ring * RING_SIZE;
            int radiusChunks = Math.max(16, ringRadius / 16);
            boolean foundInRing = false;

            for (int i = 0; i < samplesPerRing; i++) {
                totalIterations++;

                // Safety limit to prevent extremely long searches
                if (totalIterations > MAX_TOTAL_ITERATIONS) {
                    break outer;
                }

                // Calculate probe position in a circle around the player
                double angle = 2 * Math.PI * i / samplesPerRing;
                BlockPos probe = playerPos.offset(
                        Mth.floor(Math.cos(angle) * ringRadius),
                        0,
                        Mth.floor(Math.sin(angle) * ringRadius)
                );

                // Search for nearest structure from this probe point
                Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
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
                .sorted(Comparator.comparingDouble(p -> dist2DSquared(p, playerPos)))
                .limit(requestedCount)
                .collect(Collectors.toList());

        // Send header message
        String structureName = prettyName(target);
        src.sendSuccess(() -> Component.literal(
                String.format("Found %d %s within %d blocks:", best.size(), structureName, radiusBlocks)
        ), false);

        // Send individual structure locations
        // Send individual structure locations (click-to-teleport)
        for (int i = 0; i < best.size(); i++) {
            final int idx = i;
            BlockPos p = best.get(idx);
            long distance = Math.round(dist2D(p, playerPos));

            Component coordsComponent = Component.literal(
                    String.format("[%d, %d, %d]", p.getX(), p.getY(), p.getZ())
            ).withStyle(style -> style
                    .withColor(0x00FF00)
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            // run as the clicking player; use src.getPlayerOrException().getName().getString() if needed
                            "/tp " + player.getName().getString()
                                    + " " +
                                    p.getX() + " " + p.getY() + " " + p.getZ()
                    ))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.literal("Click to teleport")
                    ))
            );

            Component line = Component.literal("  " + (idx + 1) + ". ")
                    .append(coordsComponent)
                    .append(Component.literal(String.format(" (%d blocks away)", distance)));

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
     * Calculate squared 2D distance between two block positions (ignoring Y coordinate).
     * Used for distance comparisons to avoid expensive sqrt calculations.
     *
     * @param a First position
     * @param b Second position
     * @return Squared distance
     */
    private static double dist2DSquared(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    /**
     * Calculate 2D distance between two block positions (ignoring Y coordinate).
     * Used for display purposes only.
     *
     * @param a First position
     * @param b Second position
     * @return Distance in blocks
     */
    private static double dist2D(BlockPos a, BlockPos b) {
        return Math.sqrt(dist2DSquared(a, b));
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
        * BIOME SEARCH LOGIC
        * Works like the structure search, but for biomes.
        */
       public static int runBiomeSearch(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {
           CommandSourceStack src = ctx.getSource();
           ServerPlayer player;

           try {
               player = src.getPlayerOrException();
           } catch (Exception e) {
               src.sendFailure(Component.literal("This command can only be used by players."));
               return 0;
           }

           ServerLevel level = player.serverLevel();
           BlockPos origin = player.blockPosition();

           String biomeInput = StringArgumentType.getString(ctx, "target").toLowerCase(Locale.ROOT); //target same as before instead of biome
           int requestedCount = IntegerArgumentType.getInteger(ctx, "count");

           HolderSet<Biome> targets = resolveBiomeTarget(level, biomeInput);
           if (targets == null || targets.size() == 0) {
               src.sendFailure(Component.literal("Unknown biome: '" + biomeInput + "'"));
               return 0;
           }

           // ---- SEARCH PARAMETERS ----
           int samplesPerRing = 24;
           int ringStep = 256;
           int maxRings = Math.max(1, radiusBlocks / ringStep);

           // Track unique biome positions by chunk
           record ChunkKey(int cx, int cz) {}
           Set<ChunkKey> seen = new HashSet<>();
           Map<ChunkKey, BlockPos> found = new HashMap<>();

           // ---- RING SCANNING ----
           for (int ring = 1; ring <= maxRings; ring++) {
               int radius = ring * ringStep;

               for (int i = 0; i < samplesPerRing; i++) {
                   double angle = (2 * Math.PI * i) / samplesPerRing;

                   int x = origin.getX() + (int)(Math.cos(angle) * radius);
                   int z = origin.getZ() + (int)(Math.sin(angle) * radius);

                   BlockPos pos = new BlockPos(x, origin.getY(), z);
                   Holder<Biome> biome = level.getBiome(pos);

                   if (targets.contains(biome)) {
                       ChunkKey key = new ChunkKey(x >> 4, z >> 4);

                       if (seen.add(key))
                           found.put(key, pos);

                       if (found.size() >= requestedCount * 3)
                           break;
                   }
               }
           }

           if (found.isEmpty()) {
               src.sendFailure(Component.literal("No biomes matching '" + biomeInput +
                       "' found within " + radiusBlocks + " blocks."));
               return 0;
           }

           // ---- SORT RESULTS BY DISTANCE ----
           List<BlockPos> best = found.values().stream()
                   .sorted(Comparator.comparingDouble(p -> origin.distSqr(p)))
                   .limit(requestedCount)
                   .collect(Collectors.toList());

           src.sendSuccess(() -> Component.literal(
                   "Found " + best.size() + " biome(s) matching '" + biomeInput +
                           "' within " + radiusBlocks + " blocks:"
           ), false);

           // ---- SHOW RESULTS ----
           for (int i = 0; i < best.size(); i++) {
               BlockPos p = best.get(i);
               long distance = Math.round(Math.sqrt(origin.distSqr(p)));

               Component coordsComponent = Component.literal(
                       String.format("[%d, %d, %d]", p.getX(), p.getY(), p.getZ())
               ).withStyle(style -> style
                       .withColor(0x00FF00)
                       .withClickEvent(new ClickEvent(
                               ClickEvent.Action.RUN_COMMAND,
                               "/tp " + player.getName().getString() +
                                       " " + p.getX() + " " + p.getY() + " " + p.getZ()
                       ))
                       .withHoverEvent(new HoverEvent(
                               HoverEvent.Action.SHOW_TEXT,
                               Component.literal("Click to teleport")
                       ))
               );

               Component line = Component.literal("  " + (i + 1) + ". ")
                       .append(coordsComponent)
                       .append(Component.literal(" (" + distance + " blocks away)"));

               src.sendSuccess(() -> line, false);
           }

           return best.size();
       }

       /**
        * Resolve biome input (tags, ids, fuzzy)
        */
       private static HolderSet<Biome> resolveBiomeTarget(ServerLevel level, String raw) {
           Registry<Biome> reg = level.registryAccess().registryOrThrow(Registries.BIOME);

           if (raw.startsWith("#")) {
               ResourceLocation tagId = new ResourceLocation(raw.substring(1));
               TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagId);
               return reg.getTag(tag).map(x -> (HolderSet<Biome>) x).orElse(null);
           }

           ResourceLocation id = raw.contains(":")
                   ? new ResourceLocation(raw)
                   : new ResourceLocation("minecraft", raw);

           Optional<Holder.Reference<Biome>> direct =
                   reg.getHolder(ResourceKey.create(Registries.BIOME, id));

           if (direct.isPresent()) return HolderSet.direct(direct.get());

           Optional<Holder.Reference<Biome>> fuzzy = reg.holders()
                   .filter(h -> h.unwrapKey().isPresent())
                   .filter(h -> h.unwrapKey().get().location().getPath().equals(raw))
                   .findFirst();

           return fuzzy.map(HolderSet::direct).orElse(null);
       }



   }