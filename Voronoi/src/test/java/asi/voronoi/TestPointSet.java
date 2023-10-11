/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package asi.voronoi;

import java.util.HashSet;
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
        Set<Point> expected = new HashSet<>();
        expected.add(new Point(1, 52));
        expected.add(new Point(0.52, 0));
        expected.add(new Point(-3.1415, 3.1415));
        expected.add(new Point(3.1416, -123));
        Set<Point> res;
        try {
            res = actual.buildPointSet(fileName);
            assertEquals(expected,res);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }
    
    @Test
    public void testPointSetErrorParsing() {
        String fileName = "src/test/resources/pointset_02.test";
        try {
            actual.buildPointSet(fileName);
            fail("Exception expected");
        } catch (Exception ex) {
            assertEquals("Called from: 0 parse error: 0:[",ex.getMessage());
        }
    }
}
