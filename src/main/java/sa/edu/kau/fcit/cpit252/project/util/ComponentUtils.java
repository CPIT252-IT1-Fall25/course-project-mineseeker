package sa.edu.kau.fcit.cpit252.project.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

/**
 * Utility class for creating reusable chat components
 */
public final class ComponentUtils {

    private ComponentUtils() {} // Prevent instantiation

    /**
     * Creates a clickable teleport component for coordinates
     * @param pos The position to teleport to
     * @param playerName The player's name
     * @return A formatted, clickable component
     */
    public static Component createTeleportComponent(BlockPos pos, String playerName) {
        return Component.literal(
                String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())
        ).withStyle(style -> style
                .withColor(0x00FF00)
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        String.format("/tp %s %d %d %d",
                                playerName, pos.getX(), pos.getY(), pos.getZ())
                ))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("Click to teleport")
                ))
        );
    }

    /**
     * Calculate 2D distance between positions
     */
    public static double distance2D(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate squared 2D distance (faster for comparisons)
     */
    public static double distance2DSquared(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }
}