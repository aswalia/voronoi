/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author asi
 */
public class TestCircularLinkedList {
    private CircularLinkedList t;
    @Before
    public void setUp() throws Exception {
        Point p = new Point(0,5);
        t = new CircularLinkedList(p);
        assertEquals(p,t.get(0));
    }
    
    @After
    public void tearDown() throws Exception {
        t = null;
    }
    
    @Test
    public void testAdd() {
        Point expected = new Point(-1,10); 
        t.add(0, -1, expected);
        assertEquals(expected,t.get(0));
        expected = new Point(1,0);
        t.add(0, -1, expected);
        assertTrue(new Point(-1,10).equals(t.get(0)));
        Point actual = t.get(1);
        assertTrue(new Point(0,5).equals(actual));
        assertEquals(expected,t.get(2));
        expected = new Point(2,5);
        t.add(0, -1, expected);
        actual = t.get(3);
        assertEquals(expected,actual);
        String exp = "(-1.0,10.0)(0.0,5.0)(1.0,0.0)(2.0,5.0)";
        assertEquals(exp,t.toString());
        expected = new Point(-2,5);
        t.add(2, 1, expected);
        exp = "(-2.0,5.0)(1.0,0.0)(2.0,5.0)(-1.0,10.0)(0.0,5.0)";
        assertEquals(exp,t.toString());
        // problem with setHead i add (CirculrList) when head is not part of list after merge
        // (34.0,36.0)(35.0,13.0)(42.0,3.0)(37.0,47.0)(34.0,40.0)
        CircularLinkedList c = new CircularLinkedList(new Point(34,36));
        c.add(0, -1, new Point(35,13));
        c.add(0, -1, new Point(42,3));
        c.add(0, -1, new Point(37,47));
        c.add(0, -1, new Point(34,40));
        // verify that CH is as in failed test case
        exp = "(34.0,36.0)(35.0,13.0)(42.0,3.0)(37.0,47.0)(34.0,40.0)";
        assertEquals(exp,c.toString());       
        // simulate failure as when merging (31,30) with CH
        c.add(1, 4, new Point(31,30));
        exp = "(31.0,30.0)(35.0,13.0)(42.0,3.0)(37.0,47.0)(34.0,40.0)";
        assertEquals(exp,c.toString());
        // test remove of several points when adding a new node
        c = new CircularLinkedList(new Point(7,0));
        c.add(0, -1, new Point(10,0));
        c.add(0, -1, new Point(10,3));
        c.add(0, -1, new Point(10,5));
        c.add(0, -1, new Point(7,5));
        c.add(0, -1, new Point(7,4));
        c.add(0, -1, new Point(7,3));
        c.add(0, -1, new Point(7,2));
        c.add(0, -1, new Point(7,1));
        exp = "(7.0,0.0)(10.0,0.0)(10.0,3.0)(10.0,5.0)(7.0,5.0)(7.0,4.0)(7.0,3.0)(7.0,2.0)(7.0,1.0)";
        assertEquals(exp,c.toString());       
        c.add(0, 4, new Point(0,3));
        exp = "(0.0,3.0)(7.0,0.0)(10.0,0.0)(10.0,3.0)(10.0,5.0)(7.0,5.0)";
        assertEquals(exp,c.toString());
    }
    
    @Test
    public void testRemove() {
        t.add(0, -1, new Point(-1,10));
        t.add(0, -1, new Point(1,0));
        t.add(0, -1, new Point(2,5));
        String expected = "(-1.0,10.0)(0.0,5.0)(1.0,0.0)(2.0,5.0)";
        assertEquals(expected,t.toString());
        t.remove(2);
        expected = "(-1.0,10.0)(0.0,5.0)(2.0,5.0)";
        assertEquals(expected,t.toString());
        t.add(2, 1, new Point(0,0));
        expected = "(-1.0,10.0)(0.0,5.0)(0.0,0.0)(2.0,5.0)";
        assertEquals(expected,t.toString());
        t.remove(0);
        expected = "(0.0,0.0)(2.0,5.0)(0.0,5.0)";
        assertEquals(expected,t.toString());
    }
    
    @Test
    public void testCopy() {
        t.add(0, -1, new Point(-1,10));
        t.add(0, -1, new Point(1,0));
        t.add(0, -1, new Point(2,5));
        String expected = "(-1.0,10.0)(0.0,5.0)(1.0,0.0)(2.0,5.0)";
        assertEquals(expected,t.toString());
        CircularLinkedList c = t.copy();
        assertEquals(expected,c.toString());
        
    }
    
    @Test
    public void testLength() {
        assertTrue("actual: "+t.length(),1 == t.length());
        t.add(0, -1, new Point(-1,10));
        t.add(0, -1, new Point(1,0));
        t.add(0, -1, new Point(2,5));
        int actual = t.length();
        assertTrue("actual: "+actual,4 == actual);
        t.remove(0);
        t.remove(0);
        actual = t.length();
        assertTrue("actual: "+actual,2 == actual);
    }
    
}
