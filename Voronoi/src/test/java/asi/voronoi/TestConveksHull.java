/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;


/**
 *
 * @author asi
 */
public class TestConveksHull {
    ConveksHull c1, c2, c3, c4;
    
    @Before
    public void setUp() throws Exception {
        c1 = new ConveksHull(new Point(0,5), new Point(1,0));
        c2 = new ConveksHull(new Point(2,4), new Point(2,7));
        c3 = new ConveksHull(new Point(5,0), new Point(5,4));
        c4 = new ConveksHull(new Point(6,7), new Point(7,3));
    }
    
    @After
    public void tearDown() throws Exception {
        c1 = c2 = c3 = c4 = null;
    }
    
    @Test
    public void testMerge_Point_Line() {
        // point on line with existing CH - point to the left
        c1.merge(new Point(-1,10));
        Point expected = new Point(-1,10);
        Point actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(1,0);
        actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));        
        String act = c1.toString();
        String exp = "(-1.0,10.0)(0.0,5.0)(1.0,0.0)";
        assertTrue(act,act.contains(exp));
        // point on line with existing CH - point to the right
        c4.merge(new Point(8,-1));
        expected = new Point(8,-1);
        actual = c4.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c4.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(6,7);
        actual = c4.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c4.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        act = c4.toString();
        exp = "(6.0,7.0)(7.0,3.0)(8.0,-1.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        // point on line with existing CH - between existing CH
        c1.merge(new Point(0.5,2.5));
        expected = new Point(-1,10);
        actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(1,0);
        actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        act = c1.toString();
        exp = "(-1.0,10.0)(0.0,5.0)(0.5,2.5)(1.0,0.0)";
        assertTrue("actual: "+act,act.contains(exp));
    }

    @Test
    public void testMerge_Point_Nonlinear() {
        // point left of existing CH
        c4.merge(new Point(5,0));
        Point expected = new Point(5,0);
        Point actual = c4.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c4.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(7,3);
        actual = c4.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(6,7);
        actual = c4.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        String act = c4.toString();
        String exp = "(5.0,0.0)(7.0,3.0)(6.0,7.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        // point right of existing CH
        c1.merge(new Point(2,3));
        expected = new Point(0,5);
        actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(2,3);
        actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(1,0);
        actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        act = c1.toString();
        exp = "(0.0,5.0)(1.0,0.0)(2.0,3.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        // point in between
        ConveksHull t = new ConveksHull(new Point(5,0),new Point(7,3));
        t.merge(new Point(6,7));
        expected = new Point(5,0);
        actual = t.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(6,7);
        actual = t.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,0);
        actual = t.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(7,3);
        actual = t.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        // Special case where point is on line wth some of the points of the CH
        // but the CH is non-linear, i.e. one or more points are not in line
        ConveksHull ch1 = new ConveksHull(new Point(22,9), new Point(22,17));
        ch1.merge(new Point(26,32));
        act = ch1.toString();
        exp = "(22.0,9.0)(26.0,32.0)(22.0,17.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        // try to merge a point in line with 2 of the points
        ch1.merge(new Point(22,45));
        act = ch1.toString();
        exp = "(22.0,9.0)(26.0,32.0)(22.0,45.0)(22.0,17.0)";
        assertTrue(act,act.contains(new StringBuffer(exp)));
        // one more case
        ch1 = new ConveksHull(new Point(37,47), new Point(42,3));
        ch1.merge(new Point(37,39));
        act = ch1.toString();
        exp = "(37.0,39.0)(42.0,3.0)(37.0,47.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        ch1.merge(new Point(37,11));
        act = ch1.toString();
        exp = "(37.0,11.0)(42.0,3.0)(37.0,47.0)(37.0,39.0)";
        assertTrue(act,act.contains(new StringBuffer(exp)));
        // one more case
        ch1 = new ConveksHull(new Point(37,11), new Point(42,3));
        ch1.merge(new Point(37,47));
        act = ch1.toString();
        exp = "(37.0,11.0)(42.0,3.0)(37.0,47.0)";
        assertTrue(act.contains(new StringBuffer(exp)));
        ch1.merge(new Point(37,39));
        act = ch1.toString();
        exp = "(37.0,11.0)(42.0,3.0)(37.0,47.0)(37.0,39.0)";
        assertTrue(act.contains(new StringBuffer(exp)));        
    }
    
    @Test
    public void testMerge_ConveksHull_Line() {
        // subCH make a line with CH - horizontal
        ConveksHull ch1 = new ConveksHull(new Point(0,4),new Point(1,4));
        ConveksHull ch2 = new ConveksHull(new Point(2,4),new Point(5,4));
        assertTrue(ch1.getLft().equals(new Point(0,4)));
        assertTrue(ch1.getRgt().equals(new Point(1,4)));
        assertTrue(ch2.getLft().equals(new Point(2,4)));
        assertTrue(ch2.getRgt().equals(new Point(5,4)));
        ch1.merge(ch2);
        assertTrue(ch1.getLft().equals(new Point(0,4)));
        assertTrue(ch1.getRgt().equals(new Point(5,4)));
        String act = ch1.toString();
        String exp = "(0.0,4.0)(5.0,4.0)(2.0,4.0)(1.0,4.0)";
        assertTrue(act.contains(new StringBuffer(exp)));        
        // subCH make a line with CH - vertical
        ch1 = new ConveksHull(new Point(5,7),new Point(5,10));
        ch2 = new ConveksHull(new Point(5,0),new Point(5,4));
        assertTrue(ch1.getLft().equals(new Point(5,7)));
        assertTrue(ch1.getRgt().equals(new Point(5,10)));
        assertTrue(ch2.getLft().equals(new Point(5,0)));
        assertTrue(ch2.getRgt().equals(new Point(5,4)));
        ch1.merge(ch2);
        assertTrue(ch1.getLft().equals(new Point(5,0)));
        assertTrue(ch1.getRgt().equals(new Point(5,10)));
        act = ch1.toString();
        exp = "(5.0,0.0)(5.0,10.0)(5.0,7.0)(5.0,4.0)";
        assertTrue(act.contains(new StringBuffer(exp)));        
        // subCH make a line with CH - line with an angle
        ch1 = new ConveksHull(new Point(0,10),new Point(2,7));
        ch2 = new ConveksHull(new Point(4,4),new Point(6,1));
        assertTrue(ch1.getLft().equals(new Point(0,10)));
        assertTrue(ch1.getRgt().equals(new Point(2,7)));
        assertTrue(ch2.getLft().equals(new Point(4,4)));
        assertTrue(ch2.getRgt().equals(new Point(6,1)));
        ch2.merge(ch1);
        assertTrue(ch2.getLft().equals(new Point(0,10)));
        assertTrue(ch2.getRgt().equals(new Point(6,1)));
        act = ch2.toString();
        exp = "(0.0,10.0)(6.0,1.0)(4.0,4.0)(2.0,7.0)";
        assertTrue(act.contains(new StringBuffer(exp)));        
    }
    
    @Test
    public void testMerge_ConveksHull() {
        // subCH is to the right
        c1.merge(c2);
        Point expected = new Point(0,5);
        Point actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(1,0);
        actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(2,7);
        actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        String exp = "(0.0,5.0)(1.0,0.0)(2.0,4.0)(2.0,7.0)";
        String act = c1.toString();
        assertTrue(act.contains(new StringBuffer(exp)));
        assertEquals(c2.getHead(),null);
        // subCH is to the left
        c4.merge(c3);
        expected = new Point(5,0);
        actual = c4.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(6,7);
        actual = c4.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(7,3);
        actual = c4.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,0);
        actual = c4.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        exp = "(5.0,0.0)(7.0,3.0)(6.0,7.0)(5.0,4.0)";
        act = c4.toString();
        assertTrue("Expected: "+exp+" actual: "+act,act.contains(new StringBuffer(exp)));
        assertEquals(c3.getHead(),null);
        // merge of two large CH
        c1.merge(c4);
        expected = new Point(0,5);
        actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(2,7);
        actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(7,3);
        actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(1,0);
        actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        exp = "(0.0,5.0)(1.0,0.0)(5.0,0.0)(7.0,3.0)(6.0,7.0)(2.0,7.0)";
        act = c1.toString();
        assertTrue("Expected: "+exp+" actual: "+act,act.contains(new StringBuffer(exp)));
        assertEquals(c4.getHead(),null);
        // problem with setHead in add (CirculrList) when head is not part of list after merge
        ConveksHull ch = new ConveksHull(new Point(34,36), new Point(34,40));
        ch.merge(new Point(35,13));
        ch.merge(new Point(42,3));
        ch.merge(new Point(37,47));
        ch.merge(new Point(31,30));
        act = ch.toString();
        exp = "(31.0,30.0)(35.0,13.0)(42.0,3.0)(37.0,47.0)(34.0,40.0)";        
        assertTrue(act,act.contains(new StringBuffer(exp)));
    }
    
    private boolean checkBoundry(ConveksHull ch, 
                                 Point l, Point r, Point u, Point d) {
        return ch.getLft().equals(l) && 
               ch.getRgt().equals(r) && 
               ch.getUp().equals(u) && 
               ch.getDown().equals(d);
    }
    
    
    @Test
    public void testConvekshullScenarios_nonlinear() {
        ConveksHull ch = new ConveksHull(new Point(0,5), new Point(4,0));
        ch.merge(new Point(6,8));
        // setup: CH = (0,5)->(4,0)->(6,8)
        String exp = "(0.0,5.0)(4.0,0.0)(6.0,8.0)";
        String act = ch.toString();        
        assertTrue(act,act.contains(exp));
        // Scenario 1
        ConveksHull test = new ConveksHull(ch);
        test.merge(new Point(2,9));
        exp = "(0.0,5.0)(4.0,0.0)(6.0,8.0)(2.0,9.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(2,9), new Point(4,0)));
        // Scenario 2
        test = new ConveksHull(ch);
        test.merge(new Point(7,3));
        exp = "(0.0,5.0)(4.0,0.0)(7.0,3.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(7,3), new Point(6,8), new Point(4,0)));
        // Scenario 3
        test = new ConveksHull(ch);
        test.merge(new Point(5,4));
        exp = "(0.0,5.0)(4.0,0.0)(5.0,4.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 4
        test = new ConveksHull(ch);
        test.merge(new Point(2,2.5));
        exp = "(0.0,5.0)(2.0,2.5)(4.0,0.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 5
        ConveksHull ch1 = new ConveksHull(ch);
        ch1.merge(new Point(4.5,2));
        ch1.merge(new Point(5.5,6));
        test = new ConveksHull(ch1);
        test.merge(new Point(5,4));
        exp = "(0.0,5.0)(4.0,0.0)(4.5,2.0)(5.0,4.0)(5.5,6.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 6
        ConveksHull ch2 = new ConveksHull(ch);
        ch2.merge(new Point(1,3.75));
        ch2.merge(new Point(3,1.25));
        test = new ConveksHull(ch2);
        test.merge(new Point(2,2.5));
        exp = "(0.0,5.0)(1.0,3.75)(2.0,2.5)(3.0,1.25)(4.0,0.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 6a
        test = new ConveksHull(ch2);
        test.merge(new Point(2,1.5));
        exp = "(0.0,5.0)(2.0,1.5)(4.0,0.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 7
        test = new ConveksHull(ch);
        test.merge(new Point(-2,7.5));
        exp = "(-2.0,7.5)(0.0,5.0)(4.0,0.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(-2,7.5), new Point(6,8), new Point(6,8), new Point(4,0)));
        // Scenario 8
        test = new ConveksHull(ch);
        test.merge(new Point(7,12));
        exp = "(0.0,5.0)(4.0,0.0)(6.0,8.0)(7.0,12.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(7,12), new Point(7,12), new Point(4,0)));
        // Scenario 9
        test = new ConveksHull(ch);
        test.merge(new Point(5,-1.25));
        exp = "(0.0,5.0)(4.0,0.0)(5.0,-1.25)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(5,-1.25)));
        // Scenario 10
        test = new ConveksHull(ch);
        test.merge(new Point(3,-4));
        exp = "(0.0,5.0)(3.0,-4.0)(4.0,0.0)(6.0,8.0)";
        act = test.toString();
        assertTrue(act,act.contains(exp));        
        assertTrue(checkBoundry(test, new Point(0,5), new Point(6,8), new Point(6,8), new Point(3,-4)));
    }

    @Test
    public void testToString() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        String expected = "ConveksHull:\n"+
                          "(0.0,5.0)(1.0,0.0)(5.0,0.0)(7.0,3.0)(6.0,7.0)(2.0,7.0)\n"+
                          "lft:(0.0,5.0) rgt:(7.0,3.0) up:(2.0,7.0) down:(1.0,0.0)\n";
        assertEquals(expected, c1.toString());
    }
    
    @Test
    public void testSize() {
        // Empty CH
        ConveksHull c = new ConveksHull();
        int expected = 0;
        int actual = c.size();
        assertEquals(expected,actual);
        // CH of one point
        c = new ConveksHull(new Point(5,0));
        expected = 1;
        actual = c.size();
        assertEquals(expected,actual);
        // CH of two points
        expected = 2;
        actual = c1.size();
        assertEquals(expected,actual);
        actual = c2.size();
        assertEquals(expected,actual);
        actual = c3.size();
        assertEquals(expected,actual);
        actual = c4.size();
        assertEquals(expected,actual);
        // CH merge of 2 2-point CH - total of 4 points
        c1.merge(c2);
        expected = 4;
        actual = c1.size();
        assertEquals(expected,actual);
        c3.merge(c4);
        actual = c3.size();
        assertEquals(expected,actual);        
        // merge of 2 4-points set becoming a 6-point set
        c1.merge(c3);
        expected = 6;
        actual = c1.size();
        assertEquals(expected,actual);
    }

    @Test
    public void testGetLft() {
        Point expected = new Point(0,5);
        Point actual = c1.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,0);
        actual = c3.getLft();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }

    @Test
    public void testGetRgt() {
        Point expected = new Point(1,0);
        Point actual = c1.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,4);
        actual = c3.getRgt();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }

    @Test
    public void testGetUp() {
        Point expected = new Point(0,5);
        Point actual = c1.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,4);
        actual = c3.getUp();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }

    @Test
    public void testGetDown() {
        Point expected = new Point(1,0);
        Point actual = c1.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
        expected = new Point(5,0);
        actual = c3.getDown();
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }

    @Test
    public void testMaxX() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        assertTrue("max X should be 7, but was: "+c1.maxX(), c1.maxX()==7);
    }

    @Test
    public void testMinX() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        assertTrue("min X should be 0, but was: "+c1.minX(), c1.minX()==0);
    }

    @Test
    public void testMaxY() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        assertTrue("max Y should be 7, but was: "+c1.maxY(), c1.maxY()==7);
    }

    @Test
    public void testMinY() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        assertTrue("min Y should be 0, but was: "+c1.minY(), c1.minY()==0);
    }

    @Test
    public void testGetHead() {
        c1.merge(c2);
        c3.merge(c4);
        c1.merge(c3);
        Point expected = new Point(0,5);
        Point actual = c1.getHead().get(0);
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }

    @Test
    public void testAdd() {
        ConveksHull c = new ConveksHull(new Point(2,7));
        c = c.add(new Point(0,5));
        c = c.add(new Point(1,0));
        c = c.add(new Point(10,10));
        c = c.add(new Point(7,3));
        c = c.add(new Point(5,0));
        String act = c.toString();        
        String exp = "(0.0,5.0)(1.0,0.0)(5.0,0.0)(7.0,3.0)(10.0,10.0)(2.0,7.0)";
        assertTrue(act,act.contains(new StringBuffer(exp)));
    }
    
    @Test
    public void testDefect_Failed_1() {
        ConveksHull c = new ConveksHull(new Point(33, 53));
        c = c.add(new Point(new Point(34, 3)));
        c = c.add(new Point(new Point(41, 28)));
        c = c.add(new Point(new Point(36, 88)));
        String act = c.toString();        
        String exp = "(33.0,53.0)(34.0,3.0)(41.0,28.0)(36.0,88.0)";
        assertTrue(act,act.contains(new StringBuffer(exp)));        
        c = c.add(new Point(new Point(39, 52)));
        act = c.toString();        
        exp = "(33.0,53.0)(34.0,3.0)(41.0,28.0)(39.0,52.0)(36.0,88.0)";
        assertTrue(act.contains(new StringBuffer(exp)));        
    }
}
