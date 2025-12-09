package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
    /**
    * DESIGN PATTERN: Builder Pattern
     *
    * This class uses the Builder Pattern via Brigadier's LiteralArgumentBuilder.
    * Each step of the command is chained:
    * - literal("mineseeker")       <- command name
    * - requires(...)               <- permission requirements
    * - then(...)                   <- arguments added incrementally
    * - executes(...)               <- final execution logic
    *
    * The pattern allows building a complex command in a readable, step-by-step manner.
    * ----------------------------------------------------------------------------------
    * DESIGN PATTERN: Command Pattern
    * This part uses the Command Pattern: the LiteralArgumentBuilder is the invoker,
    * the executes(...) method references are concrete commands,
    * and MineseekerLogic methods are the receiver that perform the actual action.
    */
public final class MineseekerCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        //
        // This is the Builder Pattern part.
        //
        e.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("mineseeker") // <--- 1. Create the builder and command name e.g. /mineseeker in minecraft chat
                        .requires(src -> src.hasPermission(2)) // <--- 2. Has to enable cheats for using the command
                        .then(Commands.argument("structure", StringArgumentType.word()) // <--- 3. suggestions for autocompletin
                                //
                                // This connects to the other class for suggestions
                                //
                                .suggests(MineseekerSuggestions.STRUCTURE_SUGGESTIONS) // <-- 3a. get suggestions for the structures
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50)) // <--- 4. n times of how many stractures to find
                                        //
                                        // This connects to the logic class
                                        //
                                        .executes(MineseekerLogic::runWithDefaultRadius) // <--- 5. call runWithDefaultRadius defaulted at 12000 blocks for range if not selected
                                        .then(Commands.argument("radiusBlocks", IntegerArgumentType.integer(512, 64000)) // 6. else user can specify the range of maximum 64000
                                                 .executes(MineseekerLogic::runWithCustomRadius) // <--- 7. call runWithCustomRadius
                                        ))));
    }
}