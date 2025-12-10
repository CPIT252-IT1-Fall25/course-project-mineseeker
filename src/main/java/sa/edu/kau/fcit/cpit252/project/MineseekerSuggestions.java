package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
/**
 * RESPONSIBILITY: Centralized Command Suggestions Provider
 *
 * This class centralizes all autocomplete logic used by the mineseeker commands.
 * It exposes reusable, stateless SuggestionProvider instances for Brigadier.
 *
 * Core characteristics:
 * - No internal state: suggestion data is derived directly from the active game registries.
 * - Registry-driven: structure and biome names are read dynamically from Minecraft registries.
 * - Decoupled from commands: command classes depend only on these providers, not on registry logic.
 *
 * The class exists purely to isolate suggestion-generation concerns and prevent
 * duplication of registry traversal code across command definitions.
 */

public final class MineseekerSuggestions {


        private MineseekerSuggestions() {} // Prevent instantiation


        /**
         * Generic suggestion provider generator.
         * Produces suggestions for any registry (structure, biome, etc.)
         */
    public static final SuggestionProvider<CommandSourceStack> STRUCTURE_SUGGESTIONS = (ctx, builder) ->

            providerFor(
                    ctx.getSource()
                            .getLevel()
                            .registryAccess()
                            .registryOrThrow(Registries.STRUCTURE)
            ).getSuggestions(ctx, builder);


        /**
         * New Biome search
         */
        public static final SuggestionProvider<CommandSourceStack> BIOME_SUGGESTIONS = (ctx, builder) ->

            providerFor(
                    ctx.getSource().getLevel()
                            .registryAccess()
                            .registryOrThrow(Registries.BIOME)
            ).getSuggestions(ctx, builder);

        private static <T> SuggestionProvider<CommandSourceStack> providerFor(Registry<T> registry) {
            return (ctx, builder) -> {
                registry.holders().forEach(holder ->
                        holder.unwrapKey().ifPresent(key -> {
                            String path = key.location().getPath();
                            if (path.startsWith(builder.getRemainingLowerCase())) {
                                builder.suggest(path);
                            }
                        })
                );
                return builder.buildFuture();
            };
        }

}