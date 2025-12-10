package sa.edu.kau.fcit.cpit252.project.search;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.List;

/**
 * DESIGN PATTERN: Strategy Pattern
 * Defines the interface for different search algorithms.
 */
public interface SearchStrategy {
    /**
     * Generate search positions based on the strategy
     * @param origin Starting position
     * @param radius Search radius
     * @param samples Number of samples per ring
     * @return List of positions to probe
     */
    List<BlockPos> generateSearchPositions(BlockPos origin, int radius, int samples);
}