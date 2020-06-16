/* Generated by Together */

package asi.voronoi;

import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class TestBinaryTree {
    @Test
    public void testNewNode() {
        System.out.println("The \"testNewNode\" is started");
        BinaryTree b = new BinaryTree(new Point(1,2));
        BinaryTree c = b.newNode(new Point(2,1));
        assertTrue("new node", c.getP().equals(new Point(2,1)) && (c.lft() == null) && (c.rgt() == null));
        System.out.println("The \"testNewNode\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.isLeaf() */
    
    @Test
    public void testIsLeaf() {
        System.out.println("The \"testIsLeaf\" is started");
        BinaryTree b = new BinaryTree(new Point(2,3));
        assertTrue("isLeaf", b.isLeaf());
        b.insertNode(new Point(4,2));
        assertTrue("not isLeaf", !b.isLeaf());
        System.out.println("The \"testIsLeaf\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.getP() */
    
    @Test
    public void testGetP() {
        System.out.println("The \"testGetP\" is started");
        BinaryTree b = new BinaryTree(new Point(2,3));
        assertTrue("getP", b.getP().equals(new Point(2,3)));
        System.out.println("The \"testGetP\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.lft() */
    
    @Test
    public void testLft() {
        System.out.println("The \"testLft\" is started");
        BinaryTree b = new BinaryTree(new Point(2,3));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b.insertNode(new Point(1,3));
        assertTrue("lft = (1,3) and rgt() null", b.lft().getP().equals(new Point(1,3)) && (b.rgt()==null));
        System.out.println("The \"testLft\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.rgt() */
    
    @Test
    public void testRgt() {
        System.out.println("The \"testRgt\" is started");
        BinaryTree b = new BinaryTree(new Point(2,3));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b.insertNode(new Point(3,3));
        assertTrue("rgt = (3,3) and lft() null", b.rgt().getP().equals(new Point(3,3)) && (b.lft()==null));
        System.out.println("The \"testRgt\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.inTree() */
    
    @Test
    public void testInTree() {
        System.out.println("The \"testInTree\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("find root", b.inTree(new Point(3,4)));
        b.insertNode(new Point(5,2));
        assertTrue("find leave", b.inTree(new Point(5,2)));
        assertTrue("cannot find non existent root", !b.inTree(new Point(1,2)));
        assertTrue("cannot find non existent leave", !b.inTree(new Point(5,4)));
        System.out.println("The \"testInTree\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.insertNode() */
    
    @Test
    public void testInsertNode() {
        System.out.println("The \"testInsertNode\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b.insertNode(new Point(4,4));
        assertTrue("insert right", b.rgt().getP().equals(new Point(4,4)));
        b.insertNode(new Point(2,4));
        assertTrue("insert left", b.lft().getP().equals(new Point(2,4)));
        b.insertNode(new Point(4,4));
        assertTrue("insert existing point (no change)", b.count()==3);
        System.out.println("The \"testInsertNode\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.max() */
    
    @Test
    public void testMax() {
        System.out.println("The \"testMax\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.max().equals(new Point(3,4)));
        b.insertNode(new Point(4,4));
        assertTrue("insert right new max", b.max().equals(new Point(4,4)));
        b.insertNode(new Point(2,4));
        assertTrue("insert lft same max", b.max().equals(new Point(4,4)));
        b.insertNode(new Point(6,4));
        assertTrue("insert right new max", b.max().equals(new Point(6,4)));
        b.insertNode(new Point(5,4));
        assertTrue("insert lft same max", b.max().equals(new Point(6,4)));
        System.out.println("The \"testMax\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.min() */
    
    @Test
    public void testMin() {
        System.out.println("The \"testMin\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.min().equals(new Point(3,4)));
        b.insertNode(new Point(4,4));
        assertTrue("insert right same min", b.min().equals(new Point(3,4)));
        b.insertNode(new Point(2,4));
        assertTrue("insert lft new min", b.min().equals(new Point(2,4)));
        b.insertNode(new Point(2,5));
        assertTrue("insert right same min", b.min().equals(new Point(2,4)));
        b.insertNode(new Point(1,4));
        assertTrue("insert lft new min", b.min().equals(new Point(1,4)));
        System.out.println("The \"testMin\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.minY() */
    
    @Test
    public void testMinY() {
        System.out.println("The \"testMinY\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.minY()==4);
        b.insertNode(new Point(2,5));
        assertTrue("same min", b.minY()==4);
        b.insertNode(new Point(4,3));
        assertTrue("new min", b.minY()==3);
        b.insertNode(new Point(2,6));
        assertTrue("same min", b.minY()==3);
        b.insertNode(new Point(4,1));
        assertTrue("new min", b.minY()==1);
        System.out.println("The \"testMinY\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.maxY() */
    
    @Test
    public void testMaxY() {
        System.out.println("The \"testMaxY\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.maxY()==4);
        b.insertNode(new Point(2,5));
        assertTrue("new max", b.maxY()==5);
        b.insertNode(new Point(4,3));
        assertTrue("same max", b.maxY()==5);
        b.insertNode(new Point(2,6));
        assertTrue("new max", b.maxY()==6);
        b.insertNode(new Point(4,1));
        assertTrue("same max", b.maxY()==6);
        System.out.println("The \"testMaxY\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.minX() */
    
    @Test
    public void testMinX() {
        System.out.println("The \"testMinX\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.minX()==3);
        b.insertNode(new Point(4,4));
        assertTrue("insert right same min", b.minX()==3);
        b.insertNode(new Point(2,4));
        assertTrue("insert lft new min", b.minX()==2);
        b.insertNode(new Point(2,5));
        assertTrue("insert right same min", b.minX()==2);
        b.insertNode(new Point(1,4));
        assertTrue("insert lft new min", b.minX()==1);
        System.out.println("The \"testMinX\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.maxX() */
    
    @Test
    public void testMaxX() {
        System.out.println("The \"testMaxX\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.maxX()==3);
        b.insertNode(new Point(4,4));
        assertTrue("insert right new max", b.maxX()==4);
        b.insertNode(new Point(2,4));
        assertTrue("insert lft same max", b.maxX()==4);
        b.insertNode(new Point(6,4));
        assertTrue("insert right new max", b.maxX()==6);
        b.insertNode(new Point(5,4));
        assertTrue("insert lft same max", b.maxX()==6);
        System.out.println("The \"testMaxX\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.toString() */
    
    @Test
    public void testToString() {
        System.out.println("The \"testToString\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.toString().equals("[(3.0,4.0)]\n"));
        b.insertNode(new Point(4,4));
        assertTrue("insert 1",  b.toString().equals(("(3.0,4.0)\n"+"N "+"[(4.0,4.0)]\n")));
        b.insertNode(new Point(2,4));
        assertTrue("insert 2", b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"[(4.0,4.0)]\n")));
        b.insertNode(new Point(6,4));
        assertTrue("insert 3", b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"(4.0,4.0)\n"+"N "+"[(6.0,4.0)]\n")));
        b.insertNode(new Point(5,4));
        assertTrue("insert 4", b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"(4.0,4.0)\n"+"N "+"(6.0,4.0)\n"+"[(5.0,4.0)] "+"N\n")));
        b.insertNode(new Point(1,1));
        assertTrue(b.toString(), b.toString().equals(("(3.0,4.0)\n"+"(2.0,4.0) "+"(4.0,4.0)\n"+"[(1.0,1.0)] N N "+"(6.0,4.0)\n"+"[(5.0,4.0)] "+"N\n")));
        System.out.println("The \"testToString\" is finished");
    }    /** tests the method asi.voronoi.BinaryTree.count() */
    
    @Test
    public void testCount() {
        System.out.println("The \"testCount\" is started");
        BinaryTree b = new BinaryTree(new Point(3,4));
        assertTrue("only root", b.count()==1);
        b.insertNode(new Point(4,4));
        assertTrue("insert 1", b.count()==2);
        b.insertNode(new Point(2,4));
        assertTrue("insert 2", b.count()==3);
        b.insertNode(new Point(6,4));
        assertTrue("insert 3", b.count()==4);
        b.insertNode(new Point(5,4));
        assertTrue("insert 4", b.count()==5);
        System.out.println("The \"testCount\" is finished");
    }
 }