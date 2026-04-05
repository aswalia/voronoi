/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package asi.voronoi;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author asi
 */
public class TestPointSet {

    private PointSet actual;

    @Before
    public void setUp() {
        actual = new PointSet();
    }

    @After
    public void tearDown() {
        actual = null;
    }

    @Test
    public void testBuildPointSet() {
        String fileName = "src/test/resources/pointset_01.test";
        File file = new File(fileName);
        Map<Integer, Point> expected = new HashMap<>();
        expected.put(1, new Point(1, 52));
        expected.put(2, new Point(0.52, 0));
        expected.put(3, new Point(-3.1415, 3.1415));
        expected.put(4, new Point(3.1416, -123));
        Map<Integer, Point> res;
        try {
            res = actual.buildPointMap(file);
            assertEquals(expected, res);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testPointSetErrorParsing() {
        String fileName = "src/test/resources/pointset_02.test";
        File file = new File(fileName);
        try {
            actual.buildPointMap(file);
            fail("Exception expected");
        } catch (Exception ex) {
            assertEquals("Parse error - in state 'beginPoint' Got: [", ex.getMessage());
        }
    }
}
