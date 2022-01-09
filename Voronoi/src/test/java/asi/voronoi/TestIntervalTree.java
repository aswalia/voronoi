/* Generated by Together */

package asi.voronoi;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class TestIntervalTree {
    @Test
    public void testIsLeaf() throws IOException {
        IntervalTree i1 = new IntervalTree();
        IntervalTree i2 = i1.newNode();
        Assert.assertTrue("lft and rgt are null", (i2.lft()==null) && (i2.rgt()==null));
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("root is not a leaf", !i1.isLeaf());
        Assert.assertTrue("lft subnode is not a leaf", !i1.lft().isLeaf());
        Assert.assertTrue("lft subnode of lft subnode is a leaf", i1.lft().lft().isLeaf());
    }
    @Test
    public void testBuildTreeFromFile() throws IOException {
        IntervalTree i1 = new IntervalTree();
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("1st leaf is (0,0)", ((Point)i1.lft().lft().getInfo()).equals(new Point(0,0)));
        Assert.assertTrue("2nd leaf is (0,1)", ((Point)i1.lft().rgt().getInfo()).equals(new Point(0,1)));
        Assert.assertTrue("3rd leaf is (1,1)", ((Point)i1.rgt().lft().getInfo()).equals(new Point(1,1)));
        Assert.assertTrue("4th leaf is (2,0)", ((Point)i1.rgt().rgt().lft().getInfo()).equals(new Point(2,0)));
        Assert.assertTrue("5th leaf is (2,2)", ((Point)i1.rgt().rgt().rgt().getInfo()).equals(new Point(2,2)));
    }
    @Test
    public void testBuildTreeFromBinaryTree() {
        IntervalTree i1 = new IntervalTree();
        BinaryTree b = new BinaryTree(new Point(2,0));
        b.insertNode(new Point(0,1));  
        b.insertNode(new Point(0,0));  
        b.insertNode(new Point(1,1));  
        b.insertNode(new Point(2,2));  
        i1.buildTree(b);
        Assert.assertTrue("1st leaf is (0,0)", ((Point)i1.lft().lft().lft().getInfo()).equals(new Point(0,0)));
        Assert.assertTrue("2nd leaf is (0,1)", ((Point)i1.lft().lft().rgt().getInfo()).equals(new Point(0,1)));
        Assert.assertTrue("3rd leaf is (1,1)", ((Point)i1.lft().rgt().getInfo()).equals(new Point(1,1)));
        Assert.assertTrue("4th leaf is (2,0)", ((Point)i1.rgt().lft().getInfo()).equals(new Point(2,0)));
        Assert.assertTrue("5th leaf is (2,2)", ((Point)i1.rgt().rgt().getInfo()).equals(new Point(2,2)));
    }
    @Test
    public void testMaxX() throws IOException {
        IntervalTree i1 = new IntervalTree();
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("max X is 2", i1.maxX() == 2);
    }
    @Test
    public void testMaxY() throws IOException {
        IntervalTree i1 = new IntervalTree();
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("max Y is 2", i1.maxY() == 2);
    }
    @Test
    public void testMinX() throws IOException {
        IntervalTree i1 = new IntervalTree();
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("min X is 0", i1.minX() == 0);
    }
    @Test
    public void testMinY() throws IOException {
        IntervalTree i1 = new IntervalTree();
        i1.buildTree("src/test/resources/test1.it");
        Assert.assertTrue("min Y is 0", i1.minY() == 0);
    }
    @Test
    public void testToString() {
        IntervalTree i1 = new IntervalTree();
        BinaryTree b = new BinaryTree(new Point(2,0));
        b.insertNode(new Point(0,1));  
        b.insertNode(new Point(0,0));  
        b.insertNode(new Point(1,1));  
        b.insertNode(new Point(2,2));  
        i1.buildTree(b);
        String expected = "node\n" + "rgt-point:(1.0,1.0)\n" +
                          "node-point:(0.0,0.0) (0.0,1.0)\n" +
                          "node-point:(2.0,0.0) (2.0,2.0)\n";
        Assert.assertEquals(i1.toString(),(expected));
    }
}