package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;


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
        return runInProgress(commandContext, radius); // Calls the main logic with the custom radius
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
}