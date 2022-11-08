/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package asi.voronoi;

import asi.voronoi.tree.BinaryTree;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        System.out.println("dropDatabase");
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
        System.out.println("createNewDatabase");
        DatabaseHandler.createNewDatabase(tempFile);
        // let's see if we can delete it to prove it's existance
        if (!DatabaseHandler.dropDatabase(tempFile)) {
            fail("Failed to dropDatabase");
        }
    }

    /**
     * Test of createContent method, of class DatabaseHandler.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");
        try {
            DatabaseHandler.createContent();
        } catch(SQLException se) {
            fail("Exception occured: " + se.getMessage());
        }
        
    }
    
    private void addPoints(List<Point> expected) throws SQLException {
        List<String> rows = new LinkedList<>();
        for(int i=1; i<=expected.size(); i++) {
            rows.add("" + i + ", 1, " + expected.get(i-1).x() + ", " + expected.get(i-1).y());
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
        System.out.println("Database Content");
        try {
            DatabaseHandler.createContent();
        } catch (SQLException ex) {
            fail("Exception occured: " + ex.getMessage());
        }
        List<String> rows = new LinkedList<>();
        // points table
        try {
            List<Point> expected = new LinkedList<>();
            expected.add(new Point(0.0,0.0));
            expected.add(new Point(2.0,0.0));
            expected.add(new Point(1.0,0.0));
            expected.add(new Point(0.0,2.0));
            addPoints(expected);
            Map<Integer,Point> actual = DatabaseHandler.getPointsByGroup(1);
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
            rows.add("2, 1, 1, 2");
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
        System.out.println("getIndexFromPoint");
        try {
            DatabaseHandler.createContent();
        } catch (SQLException ex) {
            fail("Exception occured: " + ex.getMessage());
        }
        try {
            List<Point> expected = new LinkedList<>();
            expected.add(new Point(0.0,0.0));
            expected.add(new Point(2.0,0.0));
            addPoints(expected);
            int actual1 = DatabaseHandler.getIndexFromPoint(expected.get(0), 1);
            int actual2 = DatabaseHandler.getIndexFromPoint(expected.get(1), 1);
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
        System.out.println("getIndexOfBinarytTreeRoot");
        try {
            DatabaseHandler.createContent();
        } catch (SQLException ex) {
            fail("Exception occured: " + ex.getMessage());
        }
        try {
            List<Point> points = new LinkedList<>();
            points.add(new Point(0.0,0.0));
            points.add(new Point(2.0,0.0));
            addPoints(points);
            addBinaryTree();
            int actual = DatabaseHandler.getIndexOfBinarytTreeRoot(1);
            assertEquals(1,actual);
        } catch (SQLException ex) {
            fail("SQLException occured:" + ex.getMessage());
        }
    }
}
