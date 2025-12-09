package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.HolderSet;

import java.lang.reflect.Method;

public class MineseekerAppTest {

    // --------------------------------------------------------------------
    // TEST 1: prettyName() fallback (private reflection)
    // --------------------------------------------------------------------
    @Test
    void testPrettyNameFallback() throws Exception {
        HolderSet<Structure> empty = HolderSet.direct();

        Method pretty = MineseekerLogic.class.getDeclaredMethod("prettyName", HolderSet.class);
        pretty.setAccessible(true);

        String result = (String) pretty.invoke(null, empty);
        assertEquals("structure", result);
    }

    // --------------------------------------------------------------------
    // TEST 2: dist2D() distance computation
    // --------------------------------------------------------------------
    @Test
    void testDist2D() throws Exception {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 0, 4);

        Method dist2D = MineseekerLogic.class.getDeclaredMethod("dist2D", BlockPos.class, BlockPos.class);
        dist2D.setAccessible(true);

        double result = (double) dist2D.invoke(null, a, b);
        assertEquals(5.0, result, 0.001);
    }

    // --------------------------------------------------------------------
    // TEST 3: calculateSamplesPerRing()
    // --------------------------------------------------------------------
    @Test
    void testCalculateSamplesPerRing() throws Exception {
        Method calc = MineseekerLogic.class.getDeclaredMethod("calculateSamplesPerRing", int.class);
        calc.setAccessible(true);

        int small = (int) calc.invoke(null, 1500);
        int medium = (int) calc.invoke(null, 5000);
        int large = (int) calc.invoke(null, 20000);

        assertEquals(32, small);
        assertEquals(24, medium);
        assertEquals(16, large);
    }
}
