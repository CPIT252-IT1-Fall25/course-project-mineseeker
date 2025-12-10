package sa.edu.kau.fcit.cpit252.project;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import sa.edu.kau.fcit.cpit252.project.search.strategies.RadialSearchStrategy;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RadialSearchStrategyTest {

    @Test
    public void testGenerateSearchPositions_ReturnsCorrectCount() {
        RadialSearchStrategy strategy = new RadialSearchStrategy();
        BlockPos origin = new BlockPos(0, 64, 0);

        List<BlockPos> positions = strategy.generateSearchPositions(origin, 100, 8);

        assertEquals(8, positions.size());
    }

    @Test
    public void testGenerateSearchPositions_PositionsAreAroundOrigin() {
        RadialSearchStrategy strategy = new RadialSearchStrategy();
        BlockPos origin = new BlockPos(0, 64, 0);
        int radius = 100;

        List<BlockPos> positions = strategy.generateSearchPositions(origin, radius, 4);

        for (BlockPos pos : positions) {
            double distance = Math.sqrt(
                    Math.pow(pos.getX() - origin.getX(), 2) +
                            Math.pow(pos.getZ() - origin.getZ(), 2)
            );
            assertTrue(Math.abs(distance - radius) < 2,
                    "Position should be approximately at radius distance");
        }
    }
}