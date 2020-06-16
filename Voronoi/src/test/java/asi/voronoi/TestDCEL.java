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
        d1 = new DCEL(new Point(0,5), new Point(1,0));
        d2 = new DCEL(new Point(2,4), new Point(2,7));
        d3 = new DCEL(new Point(5,0), new Point(5,4));
        d4 = new DCEL(new Point(6,7), new Point(7,4));
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
        ConveksHull ch2 = d2.vor2CH();
        d2.merge(d1);
        ConveksHull ch12 = d2.vor2CH();
        ConveksHull ch3 = d3.vor2CH();
        ConveksHull ch4 = d4.vor2CH();
        d3.merge(d4);
        ConveksHull ch34 = d3.vor2CH();
    }

    @Test
    public void testMerge_Point() throws Exception {
        // point to the left
        d1.merge(new Point(-3,3));
        String exp1 = "lft and rgt: (0.0,5.0) (1.0,0.0)";
        String exp2 = "lft and rgt: (-3.0,3.0) (0.0,5.0)";
        String exp3 = "lft and rgt: (-3.0,3.0) (1.0,0.0)";
        String actual = d1.toString();
        assertTrue("contains (0.0,5.0) (1.0,0.0)",actual.contains(new StringBuffer(exp1)));
        assertTrue("contains (-3.0,3.0) (0.0,5.0)",actual.contains(new StringBuffer(exp2)));
        assertTrue("contains (-3.0,3.0) (1.0,0.0)",actual.contains(new StringBuffer(exp3)));
        Point exp = new Point(-3,3);
        Point act = d1.upLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        act = d1.downLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(0,5);
        act = d1.upRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(1,0);
        act = d1.downRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        // point to the right
        d2.merge(new Point(5,10));
        exp1 = "lft and rgt: (2.0,7.0) (5.0,10.0)";
        exp2 = "lft and rgt: (2.0,4.0) (2.0,7.0)";
        exp3 = "lft and rgt: (2.0,4.0) (5.0,10.0)";
        actual = d2.toString();
        assertTrue("contains (2.0,7.0) (5.0,10.0)",actual.contains(new StringBuffer(exp1)));
        assertTrue("contains (2.0,4.0) (2.0,7.0)",actual.contains(new StringBuffer(exp2)));
        assertTrue("contains (2.0,4.0) (5.0,10.0)",actual.contains(new StringBuffer(exp3)));
        exp = new Point(2,7);
        act = d2.upLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(2,4);
        act = d2.downLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(5,10);
        act = d2.upRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        act = d2.downRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
    }

    @Test
    public void testMerge_Point_Parallel() throws Exception {
        // point to the left
        try {
            d1.merge(new Point(-1,10));
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line ";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
        }
        // point to the right
        try {
            d4.merge(new Point(9,-2));
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line ";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
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
        assertTrue("contains (0.0,5.0) (2.0,7.0)",actual.contains(new StringBuffer(exp1)));
        assertTrue("contains (2.0,4.0) (2.0,7.0)",actual.contains(new StringBuffer(exp4)));
        assertTrue("contains (0.0,5.0) (2.0,4.0)",actual.contains(new StringBuffer(exp5)));
        assertTrue("contains (0.0,5.0) (1.0,0.0)",actual.contains(new StringBuffer(exp2)));
        assertTrue("contains (1.0,0.0) (2.0,4.0)",actual.contains(new StringBuffer(exp3)));
        Point exp = new Point(0,5);
        Point act = d2.upLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(1,0);
        act = d2.downLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(2,7);
        act = d2.upRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(2,4);
        act = d2.downRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
         // subVor to the right
        d3.merge(d4);
        exp1 = "lft and rgt: (5.0,4.0) (6.0,7.0)";
        exp4 = "lft and rgt: (6.0,7.0) (7.0,4.0)";
        exp5 = "lft and rgt: (5.0,4.0) (7.0,4.0)";
        exp2 = "lft and rgt: (5.0,0.0) (5.0,4.0)";
        exp3 = "lft and rgt: (5.0,0.0) (7.0,4.0)";
        actual = d3.toString();
        assertTrue("contains (5.0,4.0) (6.0,7.0)",actual.contains(new StringBuffer(exp1)));
        assertTrue("contains (6.0,7.0) (7.0,4.0)",actual.contains(new StringBuffer(exp4)));
        assertTrue("contains (5.0,4.0) (7.0,4.0)",actual.contains(new StringBuffer(exp5)));
        assertTrue("contains (5.0,0.0) (5.0,4.0)",actual.contains(new StringBuffer(exp2)));
        assertTrue("contains (5.0,0.0) (7.0,4.0)",actual.contains(new StringBuffer(exp3)));
        exp = new Point(5,4);
        act = d3.upLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(5,0);
        act = d3.downLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(6,7);
        act = d3.upRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(7,4);
        act = d3.downRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
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
        assertTrue("contains (0.0,5.0) (2.0,7.0)",actual.contains(new StringBuffer(exp1)));
        assertTrue("contains (2.0,4.0) (2.0,7.0)",actual.contains(new StringBuffer(exp4)));
        assertTrue("contains (0.0,5.0) (2.0,4.0)",actual.contains(new StringBuffer(exp5)));
        assertTrue("contains (0.0,5.0) (1.0,0.0)",actual.contains(new StringBuffer(exp2)));
        assertTrue("contains (1.0,0.0) (2.0,4.0)",actual.contains(new StringBuffer(exp3)));
        assertTrue("contains (5.0,4.0) (6.0,7.0)",actual.contains(new StringBuffer(exp6)));
        assertTrue("contains (6.0,7.0) (7.0,4.0)",actual.contains(new StringBuffer(exp7)));
        assertTrue("contains (5.0,4.0) (7.0,4.0)",actual.contains(new StringBuffer(exp8)));
        assertTrue("contains (5.0,0.0) (5.0,4.0)",actual.contains(new StringBuffer(exp9)));
        assertTrue("contains (5.0,0.0) (7.0,4.0)",actual.contains(new StringBuffer(exp10)));
        assertTrue("contains (2.0,7.0) (6.0,7.0)",actual.contains(new StringBuffer(exp11)));
        assertTrue("contains (2.0,7.0) (5.0,4.0)",actual.contains(new StringBuffer(exp12)));
        assertTrue("contains (2.0,4.0) (5.0,4.0)",actual.contains(new StringBuffer(exp13)));
        assertTrue("contains (2.0,4.0) (5.0,0.0)",actual.contains(new StringBuffer(exp14)));
        assertTrue("contains (1.0,0.0) (5.0,0.0)",actual.contains(new StringBuffer(exp15)));
        exp = new Point(2,7);
        act = d2.upLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(1,0);
        act = d2.downLft;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(6,7);
        act = d2.upRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));
        exp = new Point(5,0);
        act = d2.downRgt;
        assertTrue("Expected: "+exp+" actual: "+act,exp.equals(act));

    }

    @Test
    public void testFetch() throws Exception {
    }
    
}
