/* Generated by Together */

package asi.voronoi.tree;

import asi.voronoi.Point;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestAVLTree {
    @Test
    public void testNewNode() {
        BinaryTree b = new AVLTree(new Point(1,2));
        BinaryTree c = b.newNode(new Point(2,1));
        assertTrue(c.getP().equals(new Point(2,1)) && (c.lft() == null) && (c.rgt() == null));
        
    }    /** tests the method asi.voronoi.BinaryTree.isLeaf() */
    @Test
    public void testBuildBinaryTree() {
        try {
            BinaryTree b = new AVLTree();
            b = b.buildBinaryTree("src/test/resources/test_10.bt");
            String expected = "(3.0,7.0)\n" +
                              "(0.0,9.0) (6.0,1.0)\n" +
                              "[(0.0,2.0)] [(1.0,6.0)] (5.0,9.0) (8.0,1.0)\n" +
                              "[(4.0,2.0)] [(6.0,0.0)] N [(9.0,4.0)]\n";
            assertEquals(expected, b.toString());
        } catch (IOException ex) {
            fail("unexpected exception");
        }
    }
    @Test
    public void testIsLeaf() {
        BinaryTree b = new AVLTree(new Point(2,3));
        assertTrue("isLeaf", b.isLeaf());
        b = b.insertNode(new Point(4,2));
        assertTrue("root no longer a leaf", !b.isLeaf());
        assertTrue("new node is a leaf", b.rgt.isLeaf());
        
    }    /** tests the method asi.voronoi.BinaryTree.getP() */
    @Test
    public void testGetP() {
        BinaryTree b = new AVLTree(new Point(2,3));
        assertTrue("getP root", b.getP().equals(new Point(2,3)));
        b = b.insertNode(new Point(4,2));
        assertTrue("getP node", b.rgt.getP().equals(new Point(4,2)));
    }    /** tests the method asi.voronoi.BinaryTree.lft() */
    @Test
    public void testLft() {
        BinaryTree b = new AVLTree(new Point(2,3));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(1,3));
        assertTrue("lft = (1,3) and rgt() null", b.lft().getP().equals(new Point(1,3)) && (b.rgt()==null));
        b = b.insertNode(new Point(4,2));
        assertTrue("lft = (1,3) and rgt() not null", b.lft().getP().equals(new Point(1,3)) && (b.rgt()!=null));
    }    /** tests the method asi.voronoi.BinaryTree.rgt() */
    @Test
    public void testRgt() {
        BinaryTree b = new AVLTree(new Point(2,3));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(3,3));
        assertTrue("rgt = (3,3) and lft() null", b.rgt().getP().equals(new Point(3,3)) && (b.lft()==null));
        b = b.insertNode(new Point(1,3));
        assertTrue("rgt = (3,3) and lft() not null", b.rgt().getP().equals(new Point(3,3)) && (b.lft()!=null));
    }    /** tests the method asi.voronoi.BinaryTree.inTree() */
    @Test
    public void testInTree() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("find root", b.inTree(new Point(3,4)));
        b = b.insertNode(new Point(5,2));
        b = b.insertNode(new Point(2,2));
        b = b.insertNode(new Point(7,5));
        b = b.insertNode(new Point(4,4));        
        assertTrue("find leave", b.inTree(new Point(5,2)));
        assertTrue("cannot find non existent root", !b.inTree(new Point(1,2)));
        assertTrue("cannot find non existent leave", !b.inTree(new Point(5,4)));
        assertTrue("find leave deep down", b.inTree(new Point(4,4)));
    }    /** tests the method asi.voronoi.BinaryTree.insertNode() */
    @Test
    public void testInsertNode() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right", b.rgt().getP().equals(new Point(4,4)));
        b = b.insertNode(new Point(2,4));
        assertTrue("insert left", b.lft().getP().equals(new Point(2,4)));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert existing point (no change)", b.count()==3);
        b = b.insertNode(new Point(7,5));
        assertTrue("insert deep down", b.rgt().rgt().getP().equals(new Point(7,5)));
        b = b.insertNode(new Point(5,2));
        assertTrue("insert and rebalance tree", b.rgt().getP().equals(new Point(5,2)));
        // test a completely screwed tree where root need balancing
        b = new AVLTree(new Point(3,4));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right", b.rgt().getP().equals(new Point(4,4)));
        b = b.insertNode(new Point(4,1));
        assertTrue("insert and rebalance root", b.getP().equals(new Point(4,1)));
        assertTrue("old root is new lft", b.lft().getP().equals(new Point(3,4)));
        assertTrue("old parent is new rgt", b.rgt().getP().equals(new Point(4,4)));
        // increase coverage
        b = new AVLTree(new Point(3,4));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(2,4));
        b = b.insertNode(new Point(2,7));
        assertTrue("insert and rebalance root", b.getP().equals(new Point(2,7)));
        assertTrue("old parent is new lft", b.lft().getP().equals(new Point(2,4)));
        assertTrue("old root is new rgt", b.rgt().getP().equals(new Point(3,4)));        
        // even more coverage
        b = new AVLTree(new Point(3,4));
        assertTrue("lft and rgt null", (b.lft()==null) && (b.rgt()==null));
        b = b.insertNode(new Point(4,4));
        b = b.insertNode(new Point(5,4));
        assertTrue("insert and rebalance root", b.getP().equals(new Point(4,4)));
        assertTrue("old root is new lft", b.lft().getP().equals(new Point(3,4)));
        assertTrue("new node is still rgt", b.rgt().getP().equals(new Point(5,4)));        
    }    /** tests the method asi.voronoi.BinaryTree.max() */
    @Test
    public void testMax() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.max().equals(new Point(3,4)));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right new max", b.max().equals(new Point(4,4)));
        b = b.insertNode(new Point(2,4));
        assertTrue("insert lft same max", b.max().equals(new Point(4,4)));
        b = b.insertNode(new Point(6,4));
        assertTrue("insert right new max", b.max().equals(new Point(6,4)));
        b = b.insertNode(new Point(5,4));
        assertTrue("insert lft same max", b.max().equals(new Point(6,4)));
    }    /** tests the method asi.voronoi.BinaryTree.min() */
    @Test
    public void testMin() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.min().equals(new Point(3,4)));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right same min", b.min().equals(new Point(3,4)));
        b = b.insertNode(new Point(2,4));
        assertTrue("insert lft new min", b.min().equals(new Point(2,4)));
        b = b.insertNode(new Point(2,5));
        assertTrue("insert right same min", b.min().equals(new Point(2,4)));
        b = b.insertNode(new Point(1,4));
        assertTrue("insert lft new min", b.min().equals(new Point(1,4)));
    }    /** tests the method asi.voronoi.BinaryTree.minY() */
    @Test
    public void testMinY() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.minY()==4);
        b = b.insertNode(new Point(2,5));
        assertTrue("same min", b.minY()==4);
        b = b.insertNode(new Point(4,3));
        assertTrue("new min", b.minY()==3);
        b = b.insertNode(new Point(2,6));
        assertTrue("same min", b.minY()==3);
        b = b.insertNode(new Point(4,1));
        assertTrue("new min", b.minY()==1);
    }    /** tests the method asi.voronoi.BinaryTree.maxY() */
    @Test
    public void testMaxY() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.maxY()==4);
        b = b.insertNode(new Point(2,5));
        assertTrue("new max", b.maxY()==5);
        b = b.insertNode(new Point(4,3));
        assertTrue("same max", b.maxY()==5);
        b = b.insertNode(new Point(2,6));
        assertTrue("new max", b.maxY()==6);
        b = b.insertNode(new Point(4,1));
        assertTrue("same max", b.maxY()==6);
    }    /** tests the method asi.voronoi.BinaryTree.minX() */
    @Test
    public void testMinX() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.minX()==3);
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right same min", b.minX()==3);
        b = b.insertNode(new Point(2,4));
        assertTrue("insert lft new min", b.minX()==2);
        b = b.insertNode(new Point(2,5));
        assertTrue("insert right same min", b.minX()==2);
        b = b.insertNode(new Point(1,4));
        assertTrue("insert lft new min", b.minX()==1);
    }    /** tests the method asi.voronoi.BinaryTree.maxX() */
    @Test
    public void testMaxX() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.maxX()==3);
        b = b.insertNode(new Point(4,4));
        assertTrue("insert right new max", b.maxX()==4);
        b = b.insertNode(new Point(2,4));
        assertTrue("insert lft same max", b.maxX()==4);
        b = b.insertNode(new Point(4,2));
        assertTrue("insert right same max", b.maxX()==4);
        b = b.insertNode(new Point(1,4));
        assertTrue("insert lft same max", b.maxX()==4);
    }    /** tests the method asi.voronoi.BinaryTree.maxX() */
    @Test
    public void testToString() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.toString().equals("[(3.0,4.0)]\n"));
        b = b.insertNode(new Point(4,4));
        assertTrue("insert 1",  b.toString().equals(("(3.0,4.0)\n"+"N "+"[(4.0,4.0)]\n")));
        b = b.insertNode(new Point(2,4));
        assertTrue("insert 2", b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"[(4.0,4.0)]\n")));
        b = b.insertNode(new Point(6,4));
        assertTrue("insert 3", b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"(4.0,4.0)\n"+"N "+"[(6.0,4.0)]\n")));
        b = b.insertNode(new Point(5,4));
        assertTrue("insert 4: "+b.toString(), b.toString().equals(("(3.0,4.0)\n"+"[(2.0,4.0)] "+"(5.0,4.0)\n"+"[(4.0,4.0)] "+"[(6.0,4.0)]\n")));
        b = b.insertNode(new Point(1,1));
        assertTrue("insert 5: "+b.toString(), b.toString().equals(("(3.0,4.0)\n"+"(2.0,4.0) "+"(5.0,4.0)\n"+"[(1.0,1.0)] "+"N "+"[(4.0,4.0)] "+"[(6.0,4.0)]\n")));
    }    /** tests the method asi.voronoi.BinaryTree.count() */
    @Test
    public void testCount() {
        BinaryTree b = new AVLTree(new Point(3,4));
        assertTrue("only root", b.count()==1);
        b = b.insertNode(new Point(4,4));
        assertTrue("insert 1", b.count()==2);
        b = b.insertNode(new Point(2,4));
        assertTrue("insert 2", b.count()==3);
        b = b.insertNode(new Point(6,4));
        assertTrue("insert 3", b.count()==4);
        b = b.insertNode(new Point(5,4));
        assertTrue("insert 4", b.count()==5);
    }
    @Test
    public void testExtended() {
        BinaryTree b = new AVLTree(new Point(8,0));
        assertTrue("only root"+b.toString(), b.toString().equals("[(8.0,0.0)]\n"));
        b = b.insertNode(new Point(9,0));
        b = b.insertNode(new Point(10,0));
        assertTrue("RR "+b.toString(), b.toString().equals(("(9.0,0.0)\n"+
                                                            "[(8.0,0.0)] "+"[(10.0,0.0)]\n")));
        b = b.insertNode(new Point(2,0));
        b = b.insertNode(new Point(1,0));
        assertTrue("LL : "+b.toString(), b.toString().equals(("(9.0,0.0)\n"+
                                                              "(2.0,0.0) "+"[(10.0,0.0)]\n"+
                                                              "[(1.0,0.0)] "+"[(8.0,0.0)]\n")));
        b = b.insertNode(new Point(5,0));
        assertTrue("LRa : "+b.toString(), b.toString().equals(("(8.0,0.0)\n"+
                                                               "(2.0,0.0) "+"(9.0,0.0)\n"+
                                                               "[(1.0,0.0)] "+"[(5.0,0.0)] "+"N "+"[(10.0,0.0)]\n")));
        b = b.insertNode(new Point(3,0));
        b = b.insertNode(new Point(6,0));
        b = b.insertNode(new Point(4,0));
        assertTrue("RLbc : "+b.toString(), b.toString().equals(("(8.0,0.0)\n"+
                                                                "(3.0,0.0) "+"(9.0,0.0)\n"+
                                                                "(2.0,0.0) "+"(5.0,0.0) "+"N "+"[(10.0,0.0)]\n"+
                                                                "[(1.0,0.0)] "+"N "+"[(4.0,0.0)] "+"[(6.0,0.0)]\n")));
        b = b.insertNode(new Point(7,0));
        assertTrue("LRbc : "+b.toString(), b.toString().equals(("(5.0,0.0)\n"+
                                                                "(3.0,0.0) "+"(8.0,0.0)\n"+
                                                                "(2.0,0.0) "+"[(4.0,0.0)] "+"(6.0,0.0) "+"(9.0,0.0)\n"+
                                                                "[(1.0,0.0)] "+"N "+"N "+"[(7.0,0.0)] "+"N "+"[(10.0,0.0)]\n")));
        b = b.insertNode(new Point(11,0));
        assertTrue("RR : "+b.toString(), b.toString().equals(("(5.0,0.0)\n"+
                                                              "(3.0,0.0) "+"(8.0,0.0)\n"+
                                                              "(2.0,0.0) "+"[(4.0,0.0)] "+"(6.0,0.0) "+"(10.0,0.0)\n"+
                                                              "[(1.0,0.0)] "+"N "+"N "+"[(7.0,0.0)] "+"[(9.0,0.0)] "+"[(11.0,0.0)]\n")));
        b = b.insertNode(new Point(12,0));
        assertTrue("no change : "+b.toString(), b.toString().equals(("(5.0,0.0)\n"+
                                                                     "(3.0,0.0) "+"(8.0,0.0)\n"+
                                                                     "(2.0,0.0) "+"[(4.0,0.0)] "+"(6.0,0.0) "+"(10.0,0.0)\n"+
                                                                     "[(1.0,0.0)] "+"N "+"N "+"[(7.0,0.0)] "+"[(9.0,0.0)] "+"(11.0,0.0)\n"+
                                                                     "N "+"[(12.0,0.0)]\n")));
    }
    @Test
    public void testRRs() {
        BinaryTree b = new AVLTree(new Point(1,0));
        assertTrue("only root"+b.toString(), b.toString().equals("[(1.0,0.0)]\n"));
        b = b.insertNode(new Point(2,0));
        b = b.insertNode(new Point(3,0));
        assertTrue("1: "+b.toString(), b.toString().equals(("(2.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)]\n")));
        b = b.insertNode(new Point(4,0));
        b = b.insertNode(new Point(5,0));
        assertTrue("2: "+b.toString(), b.toString().equals(("(2.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"(4.0,0.0)\n"+
                                                            "[(3.0,0.0)] "+"[(5.0,0.0)]\n")));
        b = b.insertNode(new Point(6,0));
        assertTrue("3: "+b.toString(), b.toString().equals(("(4.0,0.0)\n"+
                                                            "(2.0,0.0) "+"(5.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)] "+"N "+"[(6.0,0.0)]\n")));
        b = b.insertNode(new Point(7,0));
        assertTrue("4: "+b.toString(), b.toString().equals(("(4.0,0.0)\n"+
                                                            "(2.0,0.0) "+"(6.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)] "+"[(5.0,0.0)] "+"[(7.0,0.0)]\n")));
        b = b.insertNode(new Point(8,0));
        b = b.insertNode(new Point(9,0));
        assertTrue("5: "+b.toString(), b.toString().equals(("(4.0,0.0)\n"+
                                                            "(2.0,0.0) "+"(6.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)] "+"[(5.0,0.0)] "+"(8.0,0.0)\n"+
                                                            "[(7.0,0.0)] "+"[(9.0,0.0)]\n")));
        b = b.insertNode(new Point(10,0));
        assertTrue("6: "+b.toString(), b.toString().equals(("(4.0,0.0)\n"+
                                                            "(2.0,0.0) "+"(8.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)] "+"(6.0,0.0) "+"(9.0,0.0)\n"+
                                                            "[(5.0,0.0)] "+"[(7.0,0.0)] "+"N "+"[(10.0,0.0)]\n")));
        b = b.insertNode(new Point(11,0));
        assertTrue("7: "+b.toString(), b.toString().equals(("(4.0,0.0)\n"+
                                                            "(2.0,0.0) "+"(8.0,0.0)\n"+
                                                            "[(1.0,0.0)] "+"[(3.0,0.0)] "+"(6.0,0.0) "+"(10.0,0.0)\n"+
                                                            "[(5.0,0.0)] "+"[(7.0,0.0)] "+"[(9.0,0.0)] "+"[(11.0,0.0)]\n")));
        b = b.insertNode(new Point(12,0));
        assertTrue("8: : "+b.toString(), b.toString().equals(("(8.0,0.0)\n"+
                                                              "(4.0,0.0) "+"(10.0,0.0)\n"+
                                                              "(2.0,0.0) "+"(6.0,0.0) "+"[(9.0,0.0)] "+"(11.0,0.0)\n"+
                                                              "[(1.0,0.0)] "+"[(3.0,0.0)] "+"[(5.0,0.0)] "+"[(7.0,0.0)] "+"N "+"[(12.0,0.0)]\n")));
    }
}
