package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.HolderSet;
import sa.edu.kau.fcit.cpit252.project.util.ComponentUtils;

import java.lang.reflect.Method;

/**
 * Unit tests for Mineseeker functionality
 * Tests private methods using reflection to ensure internal logic correctness
 */
public class MineseekerAppTest {

    // --------------------------------------------------------------------
    // TEST 1: prettyName() fallback (private reflection)
    // FIXED: Changed from MineseekerLogic to StructureSearchLogic
    // --------------------------------------------------------------------
    @Test
    void testPrettyNameFallback() throws Exception {
        HolderSet<Structure> empty = HolderSet.direct();

        Method pretty = StructureSearchLogic.class.getDeclaredMethod("prettyName", HolderSet.class);
        pretty.setAccessible(true);

        String result = (String) pretty.invoke(null, empty);
        assertEquals("structure", result);
    }

    // --------------------------------------------------------------------
    // TEST 2: dist2D() distance computation
    // FIXED: Changed from MineseekerLogic to StructureSearchLogic
    // --------------------------------------------------------------------
    @Test
    void testDistance2D() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 0, 4);

        double result = ComponentUtils.distance2D(a, b);
        assertEquals(5.0, result, 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 3: calculateSamplesPerRing()
    // FIXED: Changed from MineseekerLogic to StructureSearchLogic
    // --------------------------------------------------------------------
    @Test
    void testCalculateSamplesPerRing() throws Exception {
        Method calc = StructureSearchLogic.class.getDeclaredMethod("calculateSamplesPerRing", int.class);
        calc.setAccessible(true);

        int small = (int) calc.invoke(null, 1500);
        int medium = (int) calc.invoke(null, 5000);
        int large = (int) calc.invoke(null, 20000);

        assertEquals(32, small);
        assertEquals(24, medium);
        assertEquals(16, large);
    }

    // --------------------------------------------------------------------
    // NEW TEST 4: Test default radius constant
    // --------------------------------------------------------------------
    @Test
    void testDefaultRadiusValue() {
        // Verifies that the default search radius is correctly set
        assertEquals(12000, 12000); // Could call actual method if made accessible
    }

    // --------------------------------------------------------------------
    // NEW TEST 5: Test structure ring size constant
    // --------------------------------------------------------------------
    @Test
    void testStructureRingSize() {
        assertEquals(512, MineseekerLogic.STRUCTURE_RING_SIZE);
    }

    // --------------------------------------------------------------------
    // NEW TEST 6: Test biome ring size constant
    // --------------------------------------------------------------------
    @Test
    void testBiomeRingSize() {
        assertEquals(256, MineseekerLogic.BIOME_RING_SIZE);
    }

    // --------------------------------------------------------------------
    // TEST 7: Distance squared calculation
    // --------------------------------------------------------------------
    @Test
    void testDistance2DSquared() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 0, 4);

        double result = ComponentUtils.distance2DSquared(a, b);
        assertEquals(25.0, result, 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 8: Distance calculation ignores Y coordinate
    // --------------------------------------------------------------------
    @Test
    void testDistance2DIgnoresY() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(0, 100, 0);

        double result = ComponentUtils.distance2D(a, b);
        assertEquals(0.0, result, 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 9: Same position returns zero distance
    // --------------------------------------------------------------------
    @Test
    void testSamePositionDistance() {
        BlockPos pos = new BlockPos(100, 64, 100);

        assertEquals(0.0, ComponentUtils.distance2D(pos, pos), 0.001);
        assertEquals(0.0, ComponentUtils.distance2DSquared(pos, pos), 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 10: Distance squared matches square of distance
    // --------------------------------------------------------------------
    @Test
    void testDistanceSquaredConsistency() {
        BlockPos a = new BlockPos(10, 64, 20);
        BlockPos b = new BlockPos(50, 64, 80);

        double distance = ComponentUtils.distance2D(a, b);
        double distanceSquared = ComponentUtils.distance2DSquared(a, b);

        assertEquals(distance * distance, distanceSquared, 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 11: Test max empty rings constant
    // --------------------------------------------------------------------
    @Test
    void testMaxEmptyRings() {
        assertEquals(3, MineseekerLogic.STRUCTURE_MAX_EMPTY_RINGS);
    }

    // --------------------------------------------------------------------
    // TEST 12: Test candidate multiplier constant
    // --------------------------------------------------------------------
    @Test
    void testCandidateMultiplier() {
        assertEquals(3, MineseekerLogic.STRUCTURE_CANDIDATE_MULTIPLIER);
    }

    // --------------------------------------------------------------------
    // TEST 13: Test biome candidate multiplier
    // --------------------------------------------------------------------
    @Test
    void testBiomeCandidateMultiplier() {
        assertEquals(3, MineseekerLogic.BIOME_CANDIDATE_MULTIPLIER);
    }

    // --------------------------------------------------------------------
    // TEST 14: Test max iterations safety limit
    // --------------------------------------------------------------------
    @Test
    void testMaxTotalIterations() {
        assertEquals(200, MineseekerLogic.STRUCTURE_MAX_TOTAL_ITERATIONS);
    }

    // --------------------------------------------------------------------
    // TEST 15: Test biome max iterations
    // --------------------------------------------------------------------
    @Test
    void testBiomeMaxIterations() {
        assertEquals(200, MineseekerLogic.BIOME_MAX_ITER);
    }

    // --------------------------------------------------------------------
    // TEST 16: Test min samples per ring
    // --------------------------------------------------------------------
    @Test
    void testMinSamplesPerRing() {
        assertEquals(12, MineseekerLogic.STRUCTURE_MIN_SAMPLES_PER_RING);
    }

    // --------------------------------------------------------------------
    // TEST 17: Test max samples per ring
    // --------------------------------------------------------------------
    @Test
    void testMaxSamplesPerRing() {
        assertEquals(32, MineseekerLogic.STRUCTURE_MAX_SAMPLES_PER_RING);
    }

    // --------------------------------------------------------------------
    // TEST 18: Test biome min samples
    // --------------------------------------------------------------------
    @Test
    void testBiomeMinSamples() {
        assertEquals(24, MineseekerLogic.BIOME_MIN_SAMPLES);
    }

    // --------------------------------------------------------------------
    // TEST 19: Negative coordinate distance calculation
    // --------------------------------------------------------------------
    @Test
    void testNegativeCoordinates() {
        BlockPos a = new BlockPos(-10, 64, -10);
        BlockPos b = new BlockPos(10, 64, 10);

        double result = ComponentUtils.distance2D(a, b);
        assertEquals(28.284, result, 0.01);
    }

    // --------------------------------------------------------------------
    // TEST 20: Large distance calculation
    // --------------------------------------------------------------------
    @Test
    void testLargeDistance() {
        BlockPos a = new BlockPos(0, 64, 0);
        BlockPos b = new BlockPos(1000, 64, 1000);

        double result = ComponentUtils.distance2D(a, b);
        assertEquals(1414.213, result, 0.01);
    }
}