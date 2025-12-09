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
        //
        // This is the Builder Pattern part.
        //
        e.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("mineseeker") // <--- 1. Create the builder and command name
                        .requires(src -> src.hasPermission(2)) // <--- 2. Has to enable cheats for using the command
                        .then(Commands.argument("structure", StringArgumentType.word()) // <--- 3. suggestions for autocompletin
                                //
                                // This connects to the other class for suggestions
                                //
                                .suggests(MineseekerSuggestions.STRUCTURE_SUGGESTIONS) // <-- 3a. get suggestions
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50)) // <--- 4. n times of how many stractures to find
                                        //
                                        // This connects to the logic class
                                        //
                                        .executes(MineseekerLogic::runWithDefaultRadius) // <--- 5. call runWithDefaultRadius defaulted at 12000 blocks for range if not selected
                                        .then(Commands.argument("radiusBlocks", IntegerArgumentType.integer(512, 64000)) // 6. range of search should be defaulted at 12000 blocks else user can specify the range of maximum 64000
                                                .executes(MineseekerLogic::runWithCustomRadius) // <--- 7. Attach another action
                                        ))));
        // This makes the command, I think .
    }
}