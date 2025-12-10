package sa.edu.kau.fcit.cpit252.project;


import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import sa.edu.kau.fcit.cpit252.project.util.ComponentUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComponentUtils
 */
public class ComponentUtilsTest {

    @Test
    public void testDistance2D_SamePosition_ReturnsZero() {
        BlockPos pos = new BlockPos(100, 64, 100);
        assertEquals(0.0, ComponentUtils.distance2D(pos, pos), 0.001);
    }

    @Test
    public void testDistance2D_HorizontalDistance() {
        BlockPos a = new BlockPos(0, 64, 0);
        BlockPos b = new BlockPos(3, 64, 4);
        assertEquals(5.0, ComponentUtils.distance2D(a, b), 0.001);
    }

    @Test
    public void testDistance2DSquared_MatchesSquareOfDistance() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(10, 0, 10);

        double distance = ComponentUtils.distance2D(a, b);
        double distanceSquared = ComponentUtils.distance2DSquared(a, b);

        assertEquals(distance * distance, distanceSquared, 0.001);
    }

    @Test
    public void testDistance2D_IgnoresYCoordinate() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(0, 100, 0);
        assertEquals(0.0, ComponentUtils.distance2D(a, b), 0.001);
    }
}