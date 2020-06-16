/* Generated by Together */

package asi.voronoi;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestPoint {
    @Test
    public void testX() {
        Point p = new Point(1,2);
        assertTrue("x=1", (p.x() == 1));
    }    /** tests the method asi.voronoi.Point.y() */
    
    @Test
    public void testY() {
        Point p = new Point(1,2);
        assertTrue("y=2", (p.y() == 2));
    }    /** tests the method asi.voronoi.Point.add() */
    
    @Test
    public void testAdd() {
        Point p1 = new Point(1,2);
        Point p2 = new Point(2,4);
        assertTrue("(1,1)+(2,2)=(3,6)", p1.add(p2).equals(new Point(3,6)));
    }    /** tests the method asi.voronoi.Point.negate() */
    
    @Test
    public void testNegate() {
        Point p = new Point(1,3);
        assertTrue("-(1,1)=(-1,-3)", p.negate().equals(new Point(-1,-3)));
    }    /** tests the method asi.voronoi.Point.mult() */
    
    @Test
    public void testMult() {
        Point p = new Point(2,3);
        assertTrue("3*(2,3)=(6,9)", p.mult(3).equals(new Point(6,9)));
    }    /** tests the method asi.voronoi.Point.determinant() */
    
    @Test
    public void testDeterminant() {
        Point p1 = new Point(3,2);
        Point p2 = new Point(1,3);
        assertTrue("(3,2)*(1,3)=7", Point.determinant(p1,p2)==7);
    }    /** tests the method asi.voronoi.Point.average() */
    
    @Test
    public void testAverage() {
        Point p1 = new Point(3,7);
        Point p2 = new Point(2,5);
        assertTrue("average((3,7),(2,5))=(2.5,6)", (Point.average(p1,p2)).equals(new Point(2.5,6)));
    }    /** tests the method asi.voronoi.Point.area() */
    
    @Test
    public void testArea() {
        Point p1 = new Point(1,1);
        Point p2 = new Point(4,2);
        Point p3 = new Point(3,7);
        assertTrue("positiv", Point.area(p1,p2,p3)>0);
        assertTrue("negativ", Point.area(p1,p3,p2)<0);
        assertTrue("zero", Point.area(p1,new Point(Point.average(p1,p2)),p2)==0);
    }    /** tests the method asi.voronoi.Point.direction() */
    
    @Test
    public void testDirection() {
        assertTrue("not implemented", false);
    }    /** tests the method asi.voronoi.Point.transpose() */
    
    @Test
    public void testTranspose() {
        Point p1 = new Point(0,0);
        Point p2 = new Point(1,0);
        assertTrue("transpose(1,0) = (0,1)", Point.transpose(p1,p2).equals(new Point(0,1)));
    }    /** tests the method asi.voronoi.Point.equals() */
    
    @Test
    public void testEquals() {
        Point p1 = new Point(3,4);
        Point p2 = new Point(3,4);
        Point p3 = new Point(4,3);
        assertTrue("(3,4) = (3,4)", p1.equals(p2));
        assertTrue("(3,4) != (4,3)", !p1.equals(p3));
    }    /** tests the method asi.voronoi.Point.isLess() */
    
    @Test
    public void testIsLess() {
        Point p = new Point(3,4);
        Point t1 = new Point(3,3);
        Point t2 = new Point(3,4);
        Point t3 = new Point(3,5);
        assertTrue("is less", p.isLess(t1));
        assertTrue("equal", !p.isLess(t2));
        assertTrue("greater", !p.isLess(t3));
    }    /** tests the method asi.voronoi.Point.toString() */
    
    @Test
    public void testToString() {
        Point p = new Point(3,5);
        assertTrue("toString", p.toString().equals("(3.0,5.0)"));
    }    /** tests the method asi.voronoi.Point.coordinat() */
    
    @Test
    public void testCoordinat() {
        Point p = new Point(2,3);
        Point d = new Point(4,5);
        assertTrue("(2,3)+1.5*(4,5) = (8,10.5)", Point.coordinat(d,p,1.5).equals(new Point(8,10.5)));
    }
}
