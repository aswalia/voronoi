/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

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
public class TestVoronoiTree {
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testBuildStructure_Line() {
        // CH for a b-tree of 2 points is a line (i.e. the 2 points)
        BinaryTree b = new BinaryTree(new Point(1,3));
        b.insertNode(new Point(2,5));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        String expected = "(1.0,3.0) (2.0,5.0)";
        c.buildStructure();
        String actual = c.toString();
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
    }

    @Test
    public void testBuildStructure_Point_and_Line() {
        // a 3 point CH, where all point are on a line
        // 1 point to left of line
        BinaryTree b = new BinaryTree(new Point(0,5));
        b.insertNode(new Point(-1,10)); b.insertNode(new Point(1,0));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
        }        
        // 1 point to right of line
        b = new BinaryTree(new Point(7,3));
        b.insertNode(new Point(6,7)); b.insertNode(new Point(9,-5));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
        }
        // lft and rgt edges are on a line
        b = new BinaryTree(new Point(1,4));
        b.insertNode(new Point(0,4));
        b.insertNode(new Point(2,4)); b.insertNode(new Point(5,4));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
    }
 
    @Test
    public void testBuildStructure_4_In_Line() {
        // subCH make a line with CH - vertical
        BinaryTree b = new BinaryTree(new Point(5,4)); b.insertNode(new Point(5,0));
        b.insertNode(new Point(5,7)); b.insertNode(new Point(5,10));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
        // subCH make a line with CH - line with an angle
        b = new BinaryTree(new Point(2,7)); b.insertNode(new Point(0,10));
        b.insertNode(new Point(4,4)); b.insertNode(new Point(6,1));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
    }
    
    @Test
    public void testBuildStructure_complete() {
        // lft and rgt CH are multiple points
        BinaryTree b = new BinaryTree(new Point(5,0));
        b.insertNode(new Point(1,0)); b.insertNode(new Point(0,5));
        b.insertNode(new Point(2,4)); b.insertNode(new Point(2,7));
        b.insertNode(new Point(7,3)); b.insertNode(new Point(5,4));
        b.insertNode(new Point(6,7));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        c.buildStructure();
        String actual = c.toString();
        String expected = "(0.0,5.0) (2.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(0.0,5.0) (2.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(0.0,5.0) (1.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(1.0,0.0) (2.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(1.0,0.0) (5.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (5.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,0.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,0.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,4.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(6.0,7.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,4.0) (6.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,7.0) (6.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (2.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,7.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
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
            ret = (actual != null) && 
                    actual.trim().replaceAll("\\s+", " ").
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
            IntervalTree c = new VoronoiTree();
            for (final File fileEntry : folder.listFiles()) {
            // build and write actual result files
                String fileName = fileEntry.getName();
                if (fileName.endsWith("it")) {
                    c.buildTree(folderName+fileName);
                    c.buildStructure();
                    outFile = fileName.replace("it", "act");
                    c.writeTree(folderName + outFile);
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
                    assertTrue("expFile: " + expFile + " not equal actFile",result);
                }
            }            
        } catch (IOException ex) {
            fail("unexpected exception: "+ex.getMessage());
        }
    }
}
