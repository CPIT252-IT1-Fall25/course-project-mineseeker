package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public final class MineseekerCommand {


    /**
     * This class is for the command.
     * It's for making the command step-by-step.
     *
     * .literal("mineseeker")   <- this is the command name
     * .requires(...)           <- this sets who can use it (needs cheats)
     * .then(...)               <- this adds an argument (like 'structure')
     * .then(...)               <- this adds another argument (like 'count')
     * .executes(...)           <- this is what happens when you run the command
     */
    @SubscribeEvent //Observer pattern this is the subscribing event for command events later we're going to use it also for player positions
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("mineseeker")
                        .requires(src -> src.hasPermission(2))
                        .then(
                                Commands.argument("query", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> {
                                            String remaining = builder.getRemaining();
                                            String[] parts = remaining.split("\\s+");
                                            String last = parts[parts.length - 1];

                                            int offset = builder.getStart() + remaining.lastIndexOf(last);
                                            builder = builder.createOffset(offset);

                                            return MineseekerSuggestions.STRUCTURE_SUGGESTIONS.getSuggestions(ctx, builder);
                                        })
                                        .executes(ctx -> MineseekerLogic.runInProgress(ctx, 12000)) //we will replace the "runInProgress" method with the "run" method once we implement the Locate logic
                        )
        );
    }

}