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
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;
import java.util.stream.Collectors;


//This class holds all the EXECUTION LOGIC for the command.
//Its only responsibility is to *run* the command.

public final class MineseekerLogic {


//   This method runs when the user didn't!! specify a radius.

    public static int runWithDefaultRadius(CommandContext<CommandSourceStack> commandContext) {
        return runInProgress(commandContext, 12000); // Calls the main logic with the default
    }


//This method runs when the user does!! specify a radius.

    public static int runWithCustomRadius(CommandContext<CommandSourceStack> commandContext) {
        int radius = IntegerArgumentType.getInteger(commandContext, "radiusBlocks");
        return run(commandContext, radius); // Calls the main logic with the custom radius
    }


// A placeholder "run" method to show the command is working,
// but the main logic is "in progress".
// This proves the Builder Pattern successfully built and registered the command.

    private static int runInProgress(CommandContext<CommandSourceStack> commandContext, int radiusBlocks) {
        // We can still read the arguments to prove they were parsed
        String structure = StringArgumentType.getString(commandContext, "structure");
        int count = IntegerArgumentType.getInteger(commandContext, "count");

        // Send a simple "In Progress" message
        commandContext.getSource().sendSuccess(() ->
                        Component.literal("In Progress: Search for " + count + " '" + structure + "' in " + radiusBlocks + " blocks is pending." +
                                "\n still WIP"),
                false
        );
        return 1; // Return 1 for success
    }
    //LATER ADD FULL SEARCH LOGIC
    private static int run(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player;
        try {
            player = src.getPlayerOrException();
        } catch (Exception ex) {
            src.sendFailure(Component.literal("Players only."));
            return 0;
        }
        ServerLevel level = player.serverLevel();

        String rawIn = StringArgumentType.getString(ctx, "structure");
        int n = IntegerArgumentType.getInteger(ctx, "count");

        HolderSet<Structure> target = resolveTarget(level, rawIn);
        if (target == null) {
            src.sendFailure(Component.literal("Unknown structure: " + rawIn));
            return 0;
        }

        final BlockPos playerPos = player.blockPosition();
        final int samplesPerRing = 24;
        final boolean skipKnown = false;

        record ChunkKey(int cx, int cz) {}
        Set<ChunkKey> seen = new HashSet<>();
        Map<ChunkKey, BlockPos> found = new HashMap<>();

        int maxRings = Math.max(1, radiusBlocks / 512);
        outer:
        for (int ring = 1; ring <= maxRings; ring++) {
            int ringRadius = ring * 512;
            int radiusChunks = Math.max(16, ringRadius / 16);

            for (int i = 0; i < samplesPerRing; i++) {
                double angle = 2 * Math.PI * i / samplesPerRing;
                BlockPos probe = playerPos.offset(
                        Mth.floor(Math.cos(angle) * ringRadius),
                        0,
                        Mth.floor(Math.sin(angle) * ringRadius)
                );

                // Forge 1.20.2 returns Pair or null
                Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
                        .findNearestMapStructure(level, target, probe, radiusChunks, skipKnown);
                if (result == null) continue;

                BlockPos hit = result.getFirst();
                ChunkKey key = new ChunkKey(hit.getX() >> 4, hit.getZ() >> 4);
                if (seen.add(key)) {
                    found.put(key, hit.immutable());
                    if (found.size() >= n * 3) break outer;
                }
            }
        }

        if (found.isEmpty()) {
            src.sendFailure(Component.literal("No matches found within " + radiusBlocks + " blocks."));
            return 0;
        }

        List<BlockPos> best = found.values().stream()
                .sorted(Comparator.comparingDouble(p -> dist2D(p, playerPos)))
                .limit(n)
                .collect(Collectors.toList());
        src.sendSuccess(() -> Component.literal(
                "Nearest " + prettyName(target) + " within " + radiusBlocks + " blocks (" + best.size() + "):"
        ), false);

        for (int i = 0; i < best.size(); i++) {
            final int idx = i; // effectively final for lambda
            BlockPos p = best.get(idx);
            long d = Math.round(dist2D(p, playerPos));
            src.sendSuccess(() -> Component.literal(
                    "  " + (idx + 1) + ". x=" + p.getX() + " y=" + p.getY() + " z=" + p.getZ() + "  (" + d + " blocks)"
            ), false);
        }

        return best.size();
    }

    // --------- target resolution: id, namespaced id, or tag ----------
    private static HolderSet<Structure> resolveTarget(ServerLevel level, String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        // Tag form: #namespace:path
        if (s.startsWith("#")) {
            String tagStr = s.substring(1);
            ResourceLocation tagId = toRL(tagStr);
            if (tagId == null) return null;
            TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, tagId);
            var named = reg.getTag(tag);                 // Optional<HolderSet.Named<Structure>>
            return named.map(x -> (HolderSet<Structure>) x).orElse(null);
        }

        // Exact id form: namespace:path  or plain path → minecraft:path
        ResourceLocation id = toRL(s);
        if (id != null) {
            var holder = reg.getHolder(ResourceKey.create(Registries.STRUCTURE, id));
            if (holder.isPresent()) return HolderSet.direct(holder.get());
        }

        // Loose path match (e.g., "village")
        for (Holder<Structure> h : reg.holders().collect(Collectors.toList())) {
            if (h.unwrapKey().isPresent()) {
                ResourceLocation rl = h.unwrapKey().get().location();
                if (rl.getPath().equalsIgnoreCase(s)) {
                    return HolderSet.direct(h);
                }
            }
        }
        return null;
    }

    private static ResourceLocation toRL(String s) {
        try {
            if (s.contains(":")) return new ResourceLocation(s);
            return new ResourceLocation("minecraft", s);
        } catch (Exception e) {
            return null;
        }
    }

    private static double dist2D(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static String prettyName(HolderSet<Structure> set) {
        return set.stream().findFirst()
                .flatMap(h -> h.unwrapKey())
                .map(k -> k.location().toString())
                .orElse("structure");
    }

}