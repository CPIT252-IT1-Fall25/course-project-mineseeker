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
     * DESIGN PATTERN: Command Pattern
     * This class represents the concrete command definition and registration.
     * Each literal/subcommand encapsulates an action that can be executed by Minecraft's command dispatcher.
     *
     * RESPONSIBILITY:
     * - Register the /mineseeker command
     * - Define arguments and execution logic binding
     *
     * and also contain a builder design pattern.
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
                LiteralArgumentBuilder.<CommandSourceStack>literal("LocatePlus") // <--- 1. Create the builder and command name
                        .requires(src -> src.hasPermission(2)) // <--- 2. Has to enable cheats for using the command
                        .then(Commands.argument("structure", StringArgumentType.word()) // <--- 3. suggestions for autocompletin
                                .suggests(MineseekerSuggestions.STRUCTURE_SUGGESTIONS) // <-- 3a. get suggestions
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50)) // <--- 4. n times of how many stractures to find
                                .then(Commands.argument("radiusBlocks", IntegerArgumentType.integer(512, 64000)) // 5. the user can specify the range of maximum 64000
                                                .executes(MineseekerLogic::run) // <--- 6. Attach another action
                                        ))));

    }
}