package sa.edu.kau.fcit.cpit252.project.search.strategies;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import sa.edu.kau.fcit.cpit252.project.search.SearchStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * DESIGN PATTERN: Strategy Pattern - Concrete Strategy
 * Searches in circular rings around the origin
 */
public class RadialSearchStrategy implements SearchStrategy {

    @Override
    public List<BlockPos> generateSearchPositions(BlockPos origin, int radius, int samples) {
        List<BlockPos> positions = new ArrayList<>();

        for (int i = 0; i < samples; i++) {
            double angle = 2 * Math.PI * i / samples;
            BlockPos probe = origin.offset(
                    Mth.floor(Math.cos(angle) * radius),
                    0,
                    Mth.floor(Math.sin(angle) * radius)
            );
            positions.add(probe);
        }

        return positions;
    }
}