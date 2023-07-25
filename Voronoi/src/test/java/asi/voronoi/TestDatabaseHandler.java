/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package asi.voronoi;

import asi.voronoi.tree.AVLTree;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.tree.VTree;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author asi
 */
public class TestDatabaseHandler {
    private final String fileName = "src/test/resources/TestVD.db";
    
    public TestDatabaseHandler() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        DatabaseHandler.createNewDatabase(fileName);
        try {
            DatabaseHandler.createContent();
        } catch (SQLException ex) {
            fail("Exception occured: " + ex.getMessage());
        }
    }
    
    @After
    public void tearDown() {
        DatabaseHandler.dropDatabase(fileName);
    }

    /**
     * Test of dropDatabase method, of class DatabaseHandler.
     */
    @Test
    public void testDropDatabase() {
        boolean expResult = true;
        boolean result = DatabaseHandler.dropDatabase(fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of createNewDatabase method, of class DatabaseHandler.
     */
    @Test
    public void testCreateNewDatabase() {
        String tempFile = "bob.db";
        DatabaseHandler.createNewDatabase(tempFile);
        // let's see if we can delete it to prove it's existance
        if (!DatabaseHandler.dropDatabase(tempFile)) {
            fail("Failed to dropDatabase");
        }
    }

    private void addPoints(int grp, List<Point> expected) throws SQLException {
        List<String> rows = new LinkedList<>();
        for(int i=1; i<=expected.size(); i++) {
            rows.add("" + i + ", " + grp + ", " + expected.get(i-1).x() + ", " + expected.get(i-1).y());
        }
        DatabaseHandler.insertContent("points", rows);        
    }
    
    private void addLinesegments() throws SQLException {
        List<String> rows = new LinkedList<>();
        rows.add("1, 1, 1, 2, null, null");
        rows.add("2, 1, null, null, 3, 4");
        DatabaseHandler.insertContent("linesegments", rows);        
    }
    
    private void addBinaryTree() throws SQLException {
        List<String> rows = new LinkedList<>();
        rows.add("1, 1, null, 2");
        rows.add("2, 1, null, null");
        DatabaseHandler.insertContent("binaryTrees", rows);
        
    }

    /**
     * Test of insertContent method, of class DatabaseHandler.
     */
    @Test
    public void testDatabaseContent() {
        List<String> rows = new LinkedList<>();
        int grp = 1;
        // points table
        try {
            List<Point> expected = new LinkedList<>();
            expected.add(new Point(0.0,0.0));
            expected.add(new Point(2.0,0.0));
            expected.add(new Point(1.0,0.0));
            expected.add(new Point(0.0,2.0));
            addPoints(grp, expected);
            Map<Integer,Point> actual = DatabaseHandler.getPointsByGroup(grp);
            for(int i=1; i<=4; i++) {
                assertEquals(expected.get(i-1),actual.get(i));
            }
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
        // linesegments table
        try {
            addLinesegments();
            // no direct test of linesegments
        } catch(SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
        // binaryTrees table
        try {
            addBinaryTree();
            BinaryTree actual = DatabaseHandler.getBinaryTreeByGroup(1);
            BinaryTree expected = new BinaryTree(new Point(0.0,0.0));
            expected.insertNode(new Point(2.0,0.0));
            assertEquals(expected.toString(),actual.toString());            
        } catch(SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
        // conveksHulls table
        try {
            rows.add("1, 1, 2, 2");
            rows.add("2, 1, 1, 1");
            DatabaseHandler.insertContent("conveksHulls", rows);
            ConveksHull actual = DatabaseHandler.getConveksHullByGroup(1);
            ConveksHull expected = new ConveksHull(new Point(0.0,0.0),new Point(2.0,0.0));
            assertEquals(expected.getHead().toString(),actual.getHead().toString());
        } catch(SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
        // conveksHullsAsLinesegments table
        try {
            rows.clear();
            rows.add("1, 1");
            DatabaseHandler.insertContent("conveksHullsAsLinesegments", rows);
        } catch(SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
        // dcels table
        try {
            rows.clear();
            rows.add("2, 1, 1, 2, 2, 2");
            DatabaseHandler.insertContent("dcels", rows);
            
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getSQLState());
        }
    }

    /**
     * Test of getIndexFromPoint method, of class DatabaseHandler.
     */
    @Test
    public void testGetIndexFromPoint() {
        int grp = 1;
        try {
            List<Point> expected = new LinkedList<>();
            expected.add(new Point(0.0,0.0));
            expected.add(new Point(2.0,0.0));
            addPoints(grp, expected);
            int actual1 = DatabaseHandler.getIndexFromPoint(expected.get(0), grp);
            int actual2 = DatabaseHandler.getIndexFromPoint(expected.get(1), grp);
            assertEquals(1,actual1);
            assertEquals(2,actual2);
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
    }

    /**
     * Test of getIndexOfBinarytTreeRoot method, of class DatabaseHandler.
     */
    @Test
    public void testGetIndexOfBinarytTreeRoot() {
        int grp = 1;
        try {
            List<Point> points = new LinkedList<>();
            points.add(new Point(0.0,0.0));
            points.add(new Point(2.0,0.0));
            addPoints(grp, points);
            addBinaryTree();
            int actual = DatabaseHandler.getIndexOfBinarytTreeRoot(grp);
            assertEquals(1,actual);
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
    }
    
    private BinaryTree buildBinaryTree(int grp) {
        Map<Integer,Point> mp = DatabaseHandler.getPointsByGroup(grp);
        BinaryTree ret = null;
        Set<Integer> sk = mp.keySet();
        for (Integer i : sk) {
            Point p = mp.get(i);
            if (ret == null) {
                // first point in set
                ret = new AVLTree(p);
            } else {
                // rest of the set
                ret = ret.insertNode(new Point(p));
            }
        }
        return ret;
    }
    
    @Test
    public void testStoreDcels() {
        int grp = 10;
        try {
            List<Point> points = new LinkedList<>();
            points.add(new Point(0.0,0.0));
            points.add(new Point(0.0,2.0));
            points.add(new Point(1.0,1.0));
            points.add(new Point(2.0,0.0));
            points.add(new Point(2.0,2.0));
            addPoints(grp, points);
            BinaryTree bt = buildBinaryTree(grp);
            VTree v = new VTree();
            v.buildStructure(bt);
            DCELNode dcn = v.getInfo().getNode();
            List<Properties> r = new LinkedList<>();
            dcn.storeInDatabase(grp, r);
            DCEL dv = DatabaseHandler.getVoronoiDiagramByGroup(grp);
            String exp1 = "lft and rgt: (0.0,0.0) (1.0,1.0)";
            String exp2 = "lft and rgt: (0.0,0.0) (0.0,2.0)";
            String exp3 = "lft and rgt: (0.0,2.0) (1.0,1.0)";
            String exp4 = "lft and rgt: (0.0,2.0) (2.0,2.0)";
            String exp5 = "lft and rgt: (1.0,1.0) (2.0,2.0)";
            String exp6 = "lft and rgt: (2.0,0.0) (2.0,2.0)";
            String exp7 = "lft and rgt: (1.0,1.0) (2.0,2.0)";
            String exp8 = "lft and rgt: (0.0,0.0) (2.0,0.0)";
            String actual = dv.toString();
            assertTrue(actual, actual.contains(exp1));
            assertTrue(actual, actual.contains(exp2));
            assertTrue(actual, actual.contains(exp3));
            assertTrue(actual, actual.contains(exp4));
            assertTrue(actual, actual.contains(exp5));
            assertTrue(actual, actual.contains(exp6));
            assertTrue(actual, actual.contains(exp7));
            assertTrue(actual, actual.contains(exp8));
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());            
        }
    }
}
