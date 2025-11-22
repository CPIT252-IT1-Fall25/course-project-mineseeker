package sa.edu.kau.fcit.cpit252.project;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


//This class holds all the EXECUTION LOGIC for the command.
//Its only responsibility is to *run* the command.

public final class MineseekerLogic {


//   This method runs when the user didn't!! specify a radius.

    public static int runWithDefaultRadius(CommandContext<CommandSourceStack> commandContext) {
        return run(commandContext, 12000); // Calls the main logic with the default
    }


//This method runs when the user does!! specify a radius.

    public static int runWithCustomRadius(CommandContext<CommandSourceStack> commandContext) {
        int radius = IntegerArgumentType.getInteger(commandContext, "radiusBlocks");
        return run(commandContext, radius); // Calls the main logic with the custom radius
    }


// A placeholder "run" method to show the command is working,
// but the main logic is "in progress".
// This proves the Builder Pattern successfully built and registered the command.

    public static int runInProgress(CommandContext<CommandSourceStack> ctx, int radiusBlocks) {

        String raw = StringArgumentType.getString(ctx, "query");

        ctx.getSource().sendSuccess(
                () -> Component.literal("In Progress: " + raw),
                false
        );

        return 1;
    }

    public static int run(CommandContext<CommandSourceStack> ctx, int defaultRadius) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player;
        try {
            player = src.getPlayerOrException();
        } catch (Exception ex) {
            src.sendFailure(Component.literal("Players only."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        String raw = StringArgumentType.getString(ctx, "query");

        SearchRequest request = new SearchBuilder()
                .setDefaultRadius(defaultRadius)
                .parse(raw)
                .build();

        for (SearchTask task : request.tasks()) {
            // locateStructure(level, player, task, src);
        }

        return 1;
    }

    //LATER ADD FULL SEARCH LOGIC
}