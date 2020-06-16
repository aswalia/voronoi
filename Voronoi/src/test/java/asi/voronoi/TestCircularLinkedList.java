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
        expected = new Point(-2,5);
        t.add(2, 1, expected);
        String exp = "(-2.0,5.0)(1.0,0.0)(2.0,5.0)(-1.0,10.0)(0.0,5.0)";
        assertEquals(exp,t.toString());
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
