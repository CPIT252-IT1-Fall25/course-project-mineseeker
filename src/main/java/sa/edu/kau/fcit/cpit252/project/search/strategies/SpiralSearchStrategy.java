package sa.edu.kau.fcit.cpit252.project.search.strategies;

import net.minecraft.core.BlockPos;
import sa.edu.kau.fcit.cpit252.project.search.SearchStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * DESIGN PATTERN: Strategy Pattern - Concrete Strategy
 * Searches in a spiral pattern outward from origin
 */
public class SpiralSearchStrategy implements SearchStrategy {

    @Override
    public List<BlockPos> generateSearchPositions(BlockPos origin, int radius, int samples) {
        List<BlockPos> positions = new ArrayList<>();
        double angleIncrement = 2 * Math.PI / samples;
        double radiusIncrement = (double) radius / samples;

        for (int i = 0; i < samples; i++) {
            double currentAngle = angleIncrement * i;
            double currentRadius = radiusIncrement * i;

            int x = (int) (Math.cos(currentAngle) * currentRadius);
            int z = (int) (Math.sin(currentAngle) * currentRadius);

            positions.add(origin.offset(x, 0, z));
        }

        return positions;
    }
}