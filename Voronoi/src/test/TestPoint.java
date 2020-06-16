/* Generated by Together */

package asi.voronoi;

import asi.voronoi.Point;
import junit.framework.*;
import junitx.framework.*;

/**
 * @stereotype test 
 * @testedclass asi.voronoi.Point
 */
public class TestPoint extends PrivateTestCase {
    /**
     * constructor.
     * @param    aName     a test name
     */
    public TestPoint(String aName) {
        super(aName);
    }    /** tests the method asi.voronoi.Point.x() */
    public void testX() {
        System.out.println("The \"testX\" is started");
        Point p = new Point(1,2);
        assertTrue("x=1", (p.x() == 1));
        System.out.println("The \"testX\" is finished");
    }    /** tests the method asi.voronoi.Point.y() */
    public void testY() {
        System.out.println("The \"testY\" is started");
        Point p = new Point(1,2);
        assertTrue("y=2", (p.y() == 2));
        System.out.println("The \"testY\" is finished");
    }    /** tests the method asi.voronoi.Point.add() */
    public void testAdd() {
        System.out.println("The \"testAdd\" is started");
        Point p1 = new Point(1,2);
        Point p2 = new Point(2,4);
        assertTrue("(1,1)+(2,2)=(3,6)", p1.add(p2).equals(new Point(3,6)));
        System.out.println("The \"testAdd\" is finished");
    }    /** tests the method asi.voronoi.Point.negate() */
    public void testNegate() {
        System.out.println("The \"testNegate\" is started");
        Point p = new Point(1,3);
        assertTrue("-(1,1)=(-1,-3)", p.negate().equals(new Point(-1,-3)));
        System.out.println("The \"testNegate\" is finished");
    }    /** tests the method asi.voronoi.Point.mult() */
    public void testMult() {
        System.out.println("The \"testMult\" is started");
        Point p = new Point(2,3);
        assertTrue("3*(2,3)=(6,9)", p.mult(3).equals(new Point(6,9)));
        System.out.println("The \"testMult\" is finished");
    }    /** tests the method asi.voronoi.Point.determinant() */
    public void testDeterminant() {
        System.out.println("The \"testDeterminant\" is started");
        Point p1 = new Point(3,2);
        Point p2 = new Point(1,3);
        assertTrue("(3,2)*(1,3)=7", Point.determinant(p1,p2)==7);
        System.out.println("The \"testDeterminant\" is finished");
    }    /** tests the method asi.voronoi.Point.average() */
    public void testAverage() {
        System.out.println("The \"testAverage\" is started");
        Point p1 = new Point(3,7);
        Point p2 = new Point(2,5);
        assertTrue("average((3,7),(2,5))=(2.5,6)", (Point.average(p1,p2)).equals(new Point(2.5,6)));
        System.out.println("The \"testAverage\" is finished");
    }    /** tests the method asi.voronoi.Point.area() */
    public void testArea() {
        System.out.println("The \"testArea\" is started");
        Point p1 = new Point(1,1);
        Point p2 = new Point(4,2);
        Point p3 = new Point(3,7);
        assertTrue("positiv", Point.area(p1,p2,p3)>0);
        assertTrue("negativ", Point.area(p1,p3,p2)<0);
        assertTrue("zero", Point.area(p1,new Point(Point.average(p1,p2)),p2)==0);
        System.out.println("The \"testArea\" is finished");
    }    /** tests the method asi.voronoi.Point.direction() */
    public void testDirection() {
        System.out.println("The \"testDirection\" is started");
        assertTrue("not implemented", false);
        System.out.println("The \"testDirection\" is finished");
    }    /** tests the method asi.voronoi.Point.transpose() */
    public void testTranspose() {
        System.out.println("The \"testTranspose\" is started");
        Point p1 = new Point(0,0);
        Point p2 = new Point(1,0);
        assertTrue("transpose(1,0) = (0,1)", Point.transpose(p1,p2).equals(new Point(0,1)));
        System.out.println("The \"testTranspose\" is finished");
    }    /** tests the method asi.voronoi.Point.equals() */
    public void testEquals() {
        System.out.println("The \"testEquals\" is started");
        Point p1 = new Point(3,4);
        Point p2 = new Point(3,4);
        Point p3 = new Point(4,3);
        assertTrue("(3,4) = (3,4)", p1.equals(p2));
        assertTrue("(3,4) != (4,3)", !p1.equals(p3));
        System.out.println("The \"testEquals\" is finished");
    }    /** tests the method asi.voronoi.Point.isLess() */
    public void testIsLess() {
        System.out.println("The \"testIsLess\" is started");
        Point p = new Point(3,4);
        Point t1 = new Point(3,3);
        Point t2 = new Point(3,4);
        Point t3 = new Point(3,5);
        assertTrue("is less", p.isLess(t1));
        assertTrue("equal", !p.isLess(t2));
        assertTrue("greater", !p.isLess(t3));
        System.out.println("The \"testIsLess\" is finished");
    }    /** tests the method asi.voronoi.Point.toString() */
    public void testToString() {
        System.out.println("The \"testToString\" is started");
        Point p = new Point(3,5);
        assertTrue("toString", p.toString().equals("(3.0,5.0)"));
        System.out.println("The \"testToString\" is finished");
    }    /** tests the method asi.voronoi.Point.coordinat() */
    public void testCoordinat() {
        System.out.println("The \"testCoordinat\" is started");
        Point p = new Point(2,3);
        Point d = new Point(4,5);
        assertTrue("(2,3)+1.5*(4,5) = (8,10.5)", Point.coordinat(d,p,1.5).equals(new Point(8,10.5)));
        System.out.println("The \"testCoordinat\" is finished");
    }/**
 * @link
 * @shapeType PatternLink
 * @pattern <{TestCase}>
 * @clientRole tests
 * @supplierRole tested
 * @hidden 
 */
/*# private Point _point; */}
