/* Generated by Together */

package asi.voronoi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class TestPoint {
    
    @Test
    public void testX() {
        Point p = new Point(1,2);
        assertEquals(1, p.x(),Double.MIN_VALUE);
    }
    
    @Test
    public void testY() {
        Point p = new Point(1,2);
        assertEquals(2, p.y(),Double.MIN_VALUE);
    }
    
    @Test
    public void testAdd() {
        Point p1 = new Point(1,2);
        Point p2 = new Point(2,4);
        Point expected = new Point(3,6);
        Point actual = p1.add(p2);
        assertTrue("Expected: "+expected+" actual: "+actual,expected.equals(actual));
    }
    
    @Test
    public void testNegate() {
        Point p = new Point(1,3);
        Point expected = new Point(-1,-3);
        Point actual = p.negate();
        assertTrue("Expected: "+expected+" actual: "+actual, expected.equals(actual));
    }
    
    @Test
    public void testMult() {
        Point p = new Point(2,3);
        Point expected = new Point(6,9);
        Point actual = p.mult(3);
        assertTrue("Expected: "+expected+" actual: "+actual, expected.equals(actual));
    }
    
    @Test
    public void testDeterminant() {
        Point p1 = new Point(3,2);
        Point p2 = new Point(1,3);
        assertEquals(7, Point.determinant(p1,p2), Double.MIN_VALUE);
    }
    
    @Test
    public void testAverage() {
        Point p1 = new Point(3,7);
        Point p2 = new Point(2,5);
        Point expected = new Point(2.5,6);
        Point actual = Point.average(p1,p2);
        assertTrue("Expected: "+expected+" actual: "+actual, expected.equals(actual));
    }
    
    @Test
    public void testArea() {
        Point p1 = new Point(1,1);
        Point p2 = new Point(4,2);
        Point p3 = new Point(3,7);
        assertTrue("positiv", Point.area(p1,p2,p3)>0);
        assertTrue("negativ", Point.area(p1,p3,p2)<0);
        assertTrue("zero", Point.area(p1,new Point(Point.average(p1,p2)),p2)==0);
    }
    
    public void testDirection() {
    }
    
    @Test
    public void testAreaDouble() {
        Point p1 = new Point(0,5);
        Point p2 = new Point(2,2.5);
        Point p3 = new Point(4,0);
        double a = Point.areaDouble(p1, p2, p3);
        assertTrue(""+a, a == 0);
        p1 = new Point(0,5);
        p2 = new Point(1,3.75);
        p3 = new Point(2,2.5);
        a = Point.areaDouble(p1, p2, p3);
        assertTrue(""+a, a == 0);
        p1 = new Point(1,3.75);
        p2 = new Point(2,2.5);
        p3 = new Point(3,1.25);
        a = Point.areaDouble(p1, p2, p3);
        assertTrue(""+a, a == 0);
    }
    
    @Test
    public void testTranspose() {
        Point p1 = new Point(0,0);
        Point p2 = new Point(1,0);
        Point expected = new Point(0,1);
        Point actual = Point.transpose(p1,p2);
        assertTrue("Expected: "+expected+" actual: "+actual, expected.equals(actual));
    }
    
    @Test
    public void testEquals() {
        Point p1 = new Point(3,4);
        Point p2 = new Point(3,4);
        Point p3 = new Point(4,3);
        assertTrue("(3,4) = (3,4)", p1.equals(p2));
        assertTrue("(3,4) != (4,3)", !p1.equals(p3));
    }
    
    @Test
    public void testIsLess() {
        Point p = new Point(3,4);
        Point t1 = new Point(3,3);
        Point t2 = new Point(3,4);
        Point t3 = new Point(3,5);
        Point t4 = new Point(4,1);
        Point t5 = new Point(1,5);
        assertTrue("is greater", !p.isLess(t5));
        assertTrue("is greater", !p.isLess(t1));
        assertTrue("equal", !p.isLess(t2));
        assertTrue("is less", p.isLess(t3));
        assertTrue("is less", p.isLess(t4));
    }
    
    @Test
    public void testToString() {
        Point p = new Point(3,5);
        assertTrue("toString", p.toString().equals("(3.0,5.0)"));
    }
    
    @Test
    public void testCoordinat() {
        Point p = new Point(2,3);
        Point d = new Point(4,5);
        assertTrue("(2,3)+1.5*(4,5) = (8,10.5)", Point.coordinat(d,p,1.5).equals(new Point(8,10.5)));
    }
    
}
