package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;


//  This class holds all the SUGGESTION LOGIC for the command.
//  Its only responsibility is to provide auto-complete suggestions.

public final class MineseekerSuggestions {

    public static final SuggestionProvider<CommandSourceStack> STRUCTURE_SUGGESTIONS = (ctx, builder) -> {
        ServerLevel level = ctx.getSource().getLevel();
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        // Suggest both namespaced ids and plain paths
        reg.holders().forEach(reference -> reference.unwrapKey().ifPresent(key -> {
            String full = key.location().toString();          // example minecraft:village
            String path = key.location().getPath();           // example village
            if (full.startsWith(builder.getRemainingLowerCase())) builder.suggest(full);
            if (path.startsWith(builder.getRemainingLowerCase())) builder.suggest(path);
        }));

        return builder.buildFuture();
    };
}