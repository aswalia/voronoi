/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi.tree;

import asi.voronoi.DCEL;
import asi.voronoi.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author asi
 */
public class TestVTree {

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    @Test
    public void testBuildStructure_ZeroPoints() {
        // VTree for Zero points is null
        BinaryTree b = null;
        VTree c = new VTree();
        c.buildStructure(b);
        assertEquals(null, c.getInfo());
    }
    
    @Test
    public void testBuildStructure_OnePoint() {
        //VTree for two points is a single DCEL with lft equals rgt
        BinaryTree b = new BinaryTree(new Point(0,0));
        VTree v = new VTree();
        v.buildStructure(b);
        assertEquals(new DCEL(new Point(0,0)).toString(),v.getInfo().toString());
    }
    
    @Test
    public void testBuildStructure_TwoPoints() {
        //VTree for two points is a single DCEL with lft and rgt
        BinaryTree b = new BinaryTree(new Point(1,1));
        b.insertNode(new Point(0,0));
        VTree v = new VTree();
        v.buildStructure(b);
        assertEquals(new DCEL(new Point(0,0), new Point(1,1)).toString(),v.getInfo().toString());
        b = new BinaryTree(new Point(0,0));
        b.insertNode(new Point(1,1));
        v = new VTree();
        v.buildStructure(b);
        assertEquals(new DCEL(new Point(0,0), new Point(1,1)).toString(),v.getInfo().toString());
    }
    
    @Test
    public void testBuildStructure_ThreePoints() {
        //VTree for three points is a graph of 3 DCELs
        BinaryTree b = new BinaryTree(new Point(1,1));
        b.insertNode(new Point(0,0));
        b.insertNode(new Point(2,0));        
        VTree v = new VTree();
        v.buildStructure(b);
        String actual = v.toString();
        assertTrue(actual.contains("lft and rgt: (0.0,0.0) (1.0,1.0)"));
        assertTrue(actual.contains("lft and rgt: (1.0,1.0) (2.0,0.0)"));
        assertTrue(actual.contains("lft and rgt: (0.0,0.0) (2.0,0.0)"));
        Integer count = (actual.split("lft and rgt: ").length ) - 1;
        assertEquals(new Integer(3), count);
    }
    
    @Test
    public void testBuildStructure_Point_and_Line() {
        // a 3 point CH, where all point are on a line
        // 1 point to left of line
        BinaryTree b = new BinaryTree(new Point(0, 5));
        b.insertNode(new Point(-1, 10));
        b.insertNode(new Point(1, 0));
        VTree c = new VTree();
        try {
            c.buildStructure(b);
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains " + expected, e.getMessage().contains(new StringBuffer(expected)));
        }
        // 1 point to right of line
        b = new BinaryTree(new Point(7, 3));
        b.insertNode(new Point(6, 7));
        b.insertNode(new Point(9, -5));
        c = new VTree();
        try {
            c.buildStructure(b);
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains " + expected, e.getMessage().contains(new StringBuffer(expected)));
        }
        // lft and rgt edges are on a line
        b = new BinaryTree(new Point(1, 4));
        b.insertNode(new Point(0, 4));
        b.insertNode(new Point(2, 4));
        b.insertNode(new Point(5, 4));
        c = new VTree();
        try {
            c.buildStructure(b);
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains " + e.getMessage(), e.getMessage().contains(new StringBuffer(expected)));
        }
    }

    @Test
    public void testBuildStructure_4_In_Line() {
        // subCH make a line with CH - vertical
        BinaryTree b = new BinaryTree(new Point(5, 4));
        b.insertNode(new Point(5, 0));
        b.insertNode(new Point(5, 7));
        b.insertNode(new Point(5, 10));
        VTree c = new VTree();
        try {
            c.buildStructure(b);
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains " + e.getMessage(), e.getMessage().contains(new StringBuffer(expected)));
        }
        // subCH make a line with CH - line with an angle
        b = new BinaryTree(new Point(2, 7));
        b.insertNode(new Point(0, 10));
        b.insertNode(new Point(4, 4));
        b.insertNode(new Point(6, 1));
        c = new VTree();
        try {
            c.buildStructure(b);
            fail("Exception expected");
        } catch (Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains " + e.getMessage(), e.getMessage().contains(new StringBuffer(expected)));
        }
    }

    private String readVoronoiFromFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean compareFile(String expFile, String actFile) {
        boolean ret = false;
        String expected = readVoronoiFromFile(expFile);
        String actual = readVoronoiFromFile(actFile);
        if (expected == null) {
            // skip test when no expected file
            ret = true;
        } else {
            ret = (actual != null)
                    && actual.trim().replaceAll("\\s+", " ").
                            equals(expected.trim().replaceAll("\\s+", " "));
        }
        return ret;
    }

    @Test
    public void testBuildstructure_From_File() {
        try {
            String folderName = "src/test/resources/";
            final File folder = new File(folderName);
            String expFile, actFile, outFile;
            boolean result = false;
            BinaryTree bt = new AVLTree();
            VTree v = new VTree();
            for (final File fileEntry : folder.listFiles()) {
                // build and write actual result files
                String fileName = fileEntry.getName();
                if (fileName.endsWith("bt")) {
                    bt = bt.buildBinaryTree(folderName + fileName);
                    v.buildStructure(bt);
                    outFile = fileName.replace("bt", "act");
                    v.writeTree(folderName + outFile);
                }
            }
            for (final File fileEntry : folder.listFiles()) {
                // identify, read and compare actual and expected result files
                String fileName = fileEntry.getName();
                expFile = actFile = null;
                if (fileName.endsWith("act")) {
                    // find actual file to do comparisation with
                    actFile = folderName + fileName;
                    expFile = folderName + fileName.replace("act", "out");
                    result = compareFile(expFile, actFile);
                    assertTrue("expFile: " + expFile + " not equal actFile", result);
                }
            }
        } catch (IOException ex) {
            fail("unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testEnd2End() throws Exception {
        // balanced binary tree for pointset
        // (0,2), (2,0), (2,4), (3,2), (4,0), (4,4) and (6,2)
        String expected = "(3.0,2.0)\n"
                        + "(2.0,0.0) (4.0,4.0)\n"
                        + "[(0.0,2.0)] [(2.0,4.0)] [(4.0,0.0)] [(6.0,2.0)]\n";
        BinaryTree b = new AVLTree(new Point(0, 2));
        b = b.insertNode(new Point(2, 0));
        b = b.insertNode(new Point(2, 4));
        b = b.insertNode(new Point(3, 2));
        b = b.insertNode(new Point(4, 0));
        b = b.insertNode(new Point(4, 4));
        b = b.insertNode(new Point(6, 2));
        assertEquals(expected, b.toString());
        VTree vt = new VTree();
        vt.buildStructure(b);
        String actual = vt.getInfo().toString();
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(0.0,2.0)" + " " + "(2.0,0.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(0.0,2.0)" + " " + "(3.0,2.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(0.0,2.0)" + " " + "(2.0,4.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(2.0,4.0)" + " " + "(3.0,2.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(2.0,4.0)" + " " + "(4.0,4.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(3.0,2.0)" + " " + "(4.0,4.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(4.0,4.0)" + " " + "(6.0,2.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(3.0,2.0)" + " " + "(6.0,2.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(4.0,0.0)" + " " + "(6.0,2.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(3.0,2.0)" + " " + "(4.0,0.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(2.0,0.0)" + " " + "(4.0,0.0)" + "\n"));
        assertTrue(actual, actual.contains(
                "lft and rgt: " + "(2.0,0.0)" + " " + "(3.0,2.0)" + "\n"));
        // there are no more edges in the DCEL graph, i.e. count equals 12
        Integer count = (actual.split("lft and rgt: ").length ) - 1;
        assertEquals(new Integer(12), count);
     }
}
