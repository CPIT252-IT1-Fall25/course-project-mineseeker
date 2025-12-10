package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
    /**
    * DESIGN PATTERN: Builder Pattern
     *
    * This class uses the Builder Pattern via Brigadier's LiteralArgumentBuilder.
    * Each step of the command is chained:
    * - literal("mineseeker")       - command name
    * - requires(...)               - permission requirements
    * - then(...)                   - arguments added incrementally
    * - executes(...)               - final execution logic
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



        var dispatcher = e.getDispatcher();
        //
        // This is the Builder Pattern part.
        //

        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("mineseeker")
                        .requires(src -> src.hasPermission(2))

                        .then(buildStructureSubcommand())
                        .then(buildBiomeSubcommand())
        );


        }

        // --------------------------
        // STRUCTURE MODE
        // --------------------------
        private static LiteralArgumentBuilder<CommandSourceStack> buildStructureSubcommand() {

            return Commands.literal("structure") // <--- 3. suggestions for autocompletin

                    //
                    // This connects to the other class for suggestions
                    //
                    .then(
                            Commands.argument("target", StringArgumentType.word()) // <-- 3a. get suggestions for the structures if selected
                                    .suggests(MineseekerSuggestions.STRUCTURE_SUGGESTIONS)

                                    .then(
                                            Commands.argument("count", IntegerArgumentType.integer(1, 50)) // <--- 4. n times of how many stractures to find
                                                    //
                                                    // This connects to the logic class
                                                    //
                                                    .executes(MineseekerLogic::runWithDefaultRadius) // <--- 5. call runWithDefaultRadius defaulted at 12000 blocks for range if not selected

                                                    .then(
                                                            Commands.argument("radiusBlocks",
                                                                            IntegerArgumentType.integer(512, 64000)) // 6. else user can specify the range of maximum 64000
                                                                    .executes(MineseekerLogic::runWithCustomRadius) // <--- 7. call runWithCustomRadius
                                                    )
                                    )
                    );

        }

        // --------------------------
        // BIOME MODE
        // --------------------------
        private static LiteralArgumentBuilder<CommandSourceStack> buildBiomeSubcommand() {
            return Commands.literal("biome")

                    .then(
                            Commands.argument("target", StringArgumentType.word())
                                    .suggests(MineseekerSuggestions.BIOME_SUGGESTIONS)

                                    .then(
                                            Commands.argument("count", IntegerArgumentType.integer(1, 50))
                                                    .executes(ctx -> MineseekerLogic.runBiomeSearch(ctx, 12000))

                                                    .then(
                                                            Commands.argument("radiusBlocks"
                                                                            , IntegerArgumentType.integer(512, 64000))
                                                                    .executes(ctx -> MineseekerLogic.runBiomeSearch(
                                                                            ctx,
                                                                            IntegerArgumentType.getInteger(ctx, "radiusBlocks")
                                                                    ))
                                                    )
                                    )
                    );

        }
}