package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.util.ComponentUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.*;
import java.util.stream.Collectors;

public final class BiomeSearchLogic {

    public static final int BIOME_MIN_SAMPLES = MineseekerLogic.BIOME_MIN_SAMPLES;   // DIFFERENT VALUE
    public static final int BIOME_RING_SIZE = MineseekerLogic.BIOME_RING_SIZE;    // DIFFERENT VALUE
    public static final int BIOME_CANDIDATE_MULTIPLIER = MineseekerLogic.BIOME_CANDIDATE_MULTIPLIER;
    public static final int BIOME_MAX_ITER = MineseekerLogic.BIOME_MAX_ITER;     // optional

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
        int maxRings = Math.max(1, radiusBlocks / BIOME_RING_SIZE);

        // Track unique biome positions by chunk
        record ChunkKey(int cx, int cz) {}
        Set<ChunkKey> seen = new HashSet<>();
        Map<ChunkKey, BlockPos> found = new HashMap<>();

        // ---- RING SCANNING ----

        int iterations = 0;

        for (int ring = 1; ring <= maxRings; ring++) {
            int radius = ring * BIOME_RING_SIZE;

            for (int i = 0; i < BIOME_MIN_SAMPLES; i++) {

                if (++iterations > BIOME_MAX_ITER)
                    break;

                double angle = (2 * Math.PI * i) / BIOME_MIN_SAMPLES;

                int x = origin.getX() + (int)(Math.cos(angle) * radius);
                int z = origin.getZ() + (int)(Math.sin(angle) * radius);

                BlockPos pos = new BlockPos(x, origin.getY(), z);
                Holder<Biome> biome = level.getBiome(pos);

                if (targets.contains(biome)) {
                    ChunkKey key = new ChunkKey(x >> 4, z >> 4);

                    if (seen.add(key))
                        found.put(key, pos);

                    if (found.size() >= requestedCount * BIOME_CANDIDATE_MULTIPLIER)
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

            Component coordsComponent = ComponentUtils.createTeleportComponent(
                    p, player.getName().getString()
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
