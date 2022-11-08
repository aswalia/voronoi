/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 *
 * @author asi
 */
public class TestDCEL {

    DCEL d1, d2, d3, d4;

    @Before
    public void setUp() {
        d1 = new DCEL(new Point(0, 5), new Point(1, 0));
        d2 = new DCEL(new Point(2, 4), new Point(2, 7));
        d3 = new DCEL(new Point(5, 0), new Point(5, 4));
        d4 = new DCEL(new Point(6, 7), new Point(7, 4));
    }

    @After
    public void tearDown() {
        d1 = d2 = d3 = d4 = null;
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testToFile() throws Exception {
    }

    @Test
    public void testVor2CH() throws Exception {
        ConveksHull ch1 = d1.vor2CH();
        String exp = "(0.0,5.0)(1.0,0.0)";
        String actual = ch1.toString();
        assertTrue(actual, actual.contains(exp));
        ConveksHull ch2 = d2.vor2CH();
        exp = "(2.0,4.0)(2.0,7.0)";
        actual = ch2.toString();
        assertTrue(actual, actual.contains(exp));
        d2.merge(d1);
        ConveksHull ch12 = d2.vor2CH();
        exp = "(0.0,5.0)(1.0,0.0)(2.0,4.0)(2.0,7.0)";
        actual = ch12.toString();
        assertTrue(actual, actual.contains(exp));
        ConveksHull ch3 = d3.vor2CH();
        exp = "(5.0,0.0)(5.0,4.0)";
        actual = ch3.toString();
        assertTrue(actual, actual.contains(exp));
        ConveksHull ch4 = d4.vor2CH();
        exp = "(6.0,7.0)(7.0,4.0)";
        actual = ch4.toString();
        assertTrue(actual, actual.contains(exp));
        d3.merge(d4);
        ConveksHull ch34 = d3.vor2CH();
        exp = "(5.0,0.0)(7.0,4.0)(6.0,7.0)(5.0,4.0)";
        actual = ch34.toString();
        assertTrue(actual, actual.contains(exp));
        d2.merge(d3);
        ConveksHull ch23 = d2.vor2CH();
        exp = "(0.0,5.0)(1.0,0.0)(5.0,0.0)(7.0,4.0)(6.0,7.0)(2.0,7.0)";
        actual = ch23.toString();
        assertTrue(actual, actual.contains(exp));
    }

    @Test
    public void testMerge_Point() throws Exception {
        // point to the left
        d1.merge(new Point(-3, 3));
        String exp1 = "lft and rgt: (0.0,5.0) (1.0,0.0)";
        String exp2 = "lft and rgt: (-3.0,3.0) (0.0,5.0)";
        String exp3 = "lft and rgt: (-3.0,3.0) (1.0,0.0)";
        String actual = d1.toString();
        assertTrue(actual, actual.contains(exp1));
        assertTrue(actual, actual.contains(exp2));
        assertTrue(actual, actual.contains(exp3));
        Point exp = new Point(-3, 3);
        Point act = d1.upLft;
        assertTrue(act.toString(), exp.equals(act));
        act = d1.downLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(0, 5);
        act = d1.upRgt;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(1, 0);
        act = d1.downRgt;
        assertTrue(act.toString(), exp.equals(act));
        // point to the right
        d2.merge(new Point(5, 10));
        exp1 = "lft and rgt: (2.0,7.0) (5.0,10.0)";
        exp2 = "lft and rgt: (2.0,4.0) (2.0,7.0)";
        exp3 = "lft and rgt: (2.0,4.0) (5.0,10.0)";
        actual = d2.toString();
        assertTrue(actual, actual.contains(exp1));
        assertTrue(actual, actual.contains(exp2));
        assertTrue(actual, actual.contains(exp3));
        exp = new Point(2, 7);
        act = d2.upLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(2, 4);
        act = d2.downLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(5, 10);
        act = d2.upRgt;
        assertTrue(act.toString(), exp.equals(act));
        act = d2.downRgt;
        assertTrue(act.toString(), exp.equals(act));
    }

    @Test
    public void testMerge_Point_Parallel() throws Exception {
        // point to the left
        try {
            d1.merge(new Point(-1, 10));
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "3 points on a line";
            assertTrue(e.getMessage(), e.getMessage().contains(expected));
        }
        // point to the right
        try {
            d4.merge(new Point(9, -2));
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "3 points on a line";
            assertTrue(e.getMessage(), e.getMessage().contains(expected));
        }
    }

    @Test
    public void testMerge_DCEL() throws Exception {
        // subVor to the left
        d2.merge(d1);
        String exp1 = "lft and rgt: (0.0,5.0) (2.0,7.0)";
        String exp4 = "lft and rgt: (2.0,4.0) (2.0,7.0)";
        String exp5 = "lft and rgt: (0.0,5.0) (2.0,4.0)";
        String exp2 = "lft and rgt: (0.0,5.0) (1.0,0.0)";
        String exp3 = "lft and rgt: (1.0,0.0) (2.0,4.0)";
        String actual = d2.toString();
        assertTrue(actual, actual.contains(exp1));
        assertTrue(actual, actual.contains(exp4));
        assertTrue(actual, actual.contains(exp5));
        assertTrue(actual, actual.contains(exp2));
        assertTrue(actual, actual.contains(exp3));
        Point exp = new Point(0, 5);
        Point act = d2.upLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(1, 0);
        act = d2.downLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(2, 7);
        act = d2.upRgt;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(2, 4);
        act = d2.downRgt;
        assertTrue(act.toString(), exp.equals(act));
        // subVor to the right
        d3.merge(d4);
        exp1 = "lft and rgt: (5.0,4.0) (6.0,7.0)";
        exp4 = "lft and rgt: (6.0,7.0) (7.0,4.0)";
        exp5 = "lft and rgt: (5.0,4.0) (7.0,4.0)";
        exp2 = "lft and rgt: (5.0,0.0) (5.0,4.0)";
        exp3 = "lft and rgt: (5.0,0.0) (7.0,4.0)";
        actual = d3.toString();
        assertTrue(actual, actual.contains(exp1));
        assertTrue(actual, actual.contains(exp4));
        assertTrue(actual, actual.contains(exp5));
        assertTrue(actual, actual.contains(exp2));
        assertTrue(actual, actual.contains(exp3));
        exp = new Point(5, 4);
        act = d3.upLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(5, 0);
        act = d3.downLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(6, 7);
        act = d3.upRgt;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(7, 4);
        act = d3.downRgt;
        assertTrue(act.toString(), exp.equals(act));
        // mega merge - i.e. make a set from the above merges
        d2.merge(d3);
        exp1 = "lft and rgt: (0.0,5.0) (2.0,7.0)";
        exp4 = "lft and rgt: (2.0,4.0) (2.0,7.0)";
        exp5 = "lft and rgt: (5.0,4.0) (6.0,7.0)";
        exp2 = "lft and rgt: (0.0,5.0) (1.0,0.0)";
        exp3 = "lft and rgt: (1.0,0.0) (2.0,4.0)";
        String exp6 = "lft and rgt: (5.0,4.0) (6.0,7.0)";
        String exp7 = "lft and rgt: (6.0,7.0) (7.0,4.0)";
        String exp8 = "lft and rgt: (5.0,4.0) (7.0,4.0)";
        String exp9 = "lft and rgt: (5.0,0.0) (5.0,4.0)";
        String exp10 = "lft and rgt: (5.0,0.0) (7.0,4.0)";
        String exp11 = "lft and rgt: (2.0,7.0) (6.0,7.0)";
        String exp12 = "lft and rgt: (2.0,7.0) (5.0,4.0)";
        String exp13 = "lft and rgt: (2.0,4.0) (5.0,4.0)";
        String exp14 = "lft and rgt: (2.0,4.0) (5.0,0.0)";
        String exp15 = "lft and rgt: (1.0,0.0) (5.0,0.0)";
        actual = d2.toString();
        assertTrue(actual, actual.contains(exp1));
        assertTrue(actual, actual.contains(exp4));
        assertTrue(actual, actual.contains(exp5));
        assertTrue(actual, actual.contains(exp2));
        assertTrue(actual, actual.contains(exp3));
        assertTrue(actual, actual.contains(exp6));
        assertTrue(actual, actual.contains(exp7));
        assertTrue(actual, actual.contains(exp8));
        assertTrue(actual, actual.contains(exp9));
        assertTrue(actual, actual.contains(exp10));
        assertTrue(actual, actual.contains(exp11));
        assertTrue(actual, actual.contains(exp12));
        assertTrue(actual, actual.contains(exp13));
        assertTrue(actual, actual.contains(exp14));
        assertTrue(actual, actual.contains(exp15));
        exp = new Point(2, 7);
        act = d2.upLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(1, 0);
        act = d2.downLft;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(6, 7);
        act = d2.upRgt;
        assertTrue(act.toString(), exp.equals(act));
        exp = new Point(5, 0);
        act = d2.downRgt;
        assertTrue(act.toString(), exp.equals(act));

    }

    @Test
    public void testMerge_DCEL_failure() throws Exception {
        // DCEL of 4 co-cirlular points
        DCEL dc1 = new DCEL(new Point(0, 0), new Point(0, 2));
        DCEL dc2 = new DCEL(new Point(2, 0), new Point(2, 2));
        try {
            dc1.merge(dc2);
            fail("exception expected");
        } catch (Exception re) {
            assertTrue(re.getMessage(), re.getMessage().contains("Left and Right meet with current"));
        }
        // DCEL of 4 co-cirlular points
        dc1 = new DCEL(new Point(0, 0), new Point(0, 2));
        try {
            dc1.merge(new Point(2, 0));
            dc1.merge(new Point(2, 2));
            fail("exception expected");
        } catch (Exception re) {
            assertTrue(re.getMessage(), re.getMessage().contains("4 points co-circular"));
        }
        // DCEL of 3 parallel point
        dc1 = new DCEL(new Point(0, 0), new Point(0, 2));
        try {
            dc1.merge(new Point(0, 4));
            fail("exception expected");
        } catch (Exception re) {
            assertTrue(re.getMessage(), re.getMessage().contains("3 points on a line"));
        }
        // DCEL of 4 parallel point
        dc1 = new DCEL(new Point(0, 0), new Point(0, 2));
        dc2 = new DCEL(new Point(0, 4), new Point(0, 6));
        try {
            dc1.merge(dc2);
            fail("exception expected");
        } catch (Exception re) {
            assertTrue(re.getMessage(), re.getMessage().contains("4 points on a line"));
        }

    }

    @Test
    public void testFetch() throws Exception {
    }
}
