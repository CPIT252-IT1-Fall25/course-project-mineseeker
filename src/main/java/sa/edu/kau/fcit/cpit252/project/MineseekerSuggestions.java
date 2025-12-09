package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
    /**
    * DESIGN PATTERN: Singleton / Static Utility
    *
    * This class exposes a single static SuggestionProvider.
    * The responsibility is centralized: generate structure name suggestions.
    * It is stateless and globally accessible without instantiation, resembling Singleton behavior.
    */
public final class MineseekerSuggestions {

    public static final SuggestionProvider<CommandSourceStack> STRUCTURE_SUGGESTIONS = (ctx, builder) -> {
        ServerLevel level = ctx.getSource().getLevel();
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        reg.holders().forEach(reference -> reference.unwrapKey().ifPresent(key -> {
            String path = key.location().getPath();           // example village
            if (path.startsWith(builder.getRemainingLowerCase())) builder.suggest(path);
        }));

        return builder.buildFuture();
    };
}